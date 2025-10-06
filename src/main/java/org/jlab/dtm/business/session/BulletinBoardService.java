package org.jlab.dtm.business.session;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.dtm.persistence.enumeration.BulletinBoardCategory;
import org.jlab.dtm.persistence.enumeration.BulletinBoardPriority;

/**
 * @author ryans
 */
@Stateless
public class BulletinBoardService {

  private static final long MAX_EXECUTE_MILLIS = 10000;

  private static final Logger logger = Logger.getLogger(BulletinBoardService.class.getName());

  private String execPath;
  private String category;

  private boolean bbEnabled = false;

  @PostConstruct
  private void init() {
    execPath = System.getenv("DTM_BBMSG_PATH");

    if (execPath == null || execPath.isEmpty()) {
      logger.log(Level.INFO, "BB Disabled; DTM_BBMSG_PATH empty");
      return;
    }

    logger.log(Level.FINE, "DTM_BBMSG_PATH: {0}", execPath);

    File exec = new File(execPath);

    if (!exec.exists()) {
      logger.log(Level.INFO, "BB Disabled; executable bbmsg does not exist");
      return;
    }

    category = System.getenv("DTM_BBMSG_CATEGORY");

    if (category == null || category.isEmpty()) {
      logger.log(Level.INFO, "BB Disabled; DTM_BBMSG_CATEGORY empty");
      return;
    }

    // Throws IllegalArgumentException if category isn't in enum list
    BulletinBoardCategory.valueOf(category);

    logger.log(Level.FINE, "DTM_BBMSG_CATEGORY: {0}", category);

    bbEnabled = true;
  }

  @PermitAll
  public void postMessage(String message, BulletinBoardPriority priority) throws IOException {

    if (!bbEnabled) {
      return;
    }

    List<String> command = new ArrayList<String>();
    command.add(execPath);
    command.add("-c" + category);
    command.add("-p" + priority.getPriorityNumber());
    command.add(message);

    ProcessBuilder builder = new ProcessBuilder(command);

    builder.redirectErrorStream(true);

    Timer timer = new Timer();
    timer.schedule(new InterruptTimerTask(Thread.currentThread()), MAX_EXECUTE_MILLIS);
    Process p = builder.start();
    Thread t = new Thread(new StreamGobbler(p.getInputStream()));
    t.start();

    try {
      int status = p.waitFor();

      if (status != 0) {
        throw new IOException("Unexpected status from bbmsg process: " + status);
      }
    } catch (InterruptedException e) {
      p.destroy();
      throw new IOException("Interrupted while waiting for bbmsg", e);
    } finally {
      // If task completes without interruption we must cancel the
      // interrupt task to prevent interrupt later on!
      timer.cancel();

      // Clear interrupted flag for two cases:
      // (1) task completed but timer task sets interrupt flag before
      // we can cancel it
      // (2) task isn't completed and is interrupted by timer task; note
      // that most things in Java clear the interrupt flag before throwing
      // and exception, but Process.waitFor does not;
      // see http://bugs.sun.com/view_bug.do?bug_id=6420270
      Thread.interrupted();
    }
  }

  private class StreamGobbler implements Runnable {

    private final InputStream in;

    StreamGobbler(InputStream in) {
      this.in = in;
    }

    @Override
    public void run() {
      try {
        InputStreamReader reader = new InputStreamReader(in);
        BufferedReader buffer = new BufferedReader(reader);
        String line;
        while ((line = buffer.readLine()) != null) {
          logger.log(Level.FINE, line);
        }
      } catch (IOException ioe) {
        logger.log(Level.SEVERE, "Unable to gobble stream", ioe);
      }
    }
  }

  private class InterruptTimerTask extends TimerTask {

    private final Thread thread;

    public InterruptTimerTask(Thread t) {
      this.thread = t;
    }

    @Override
    public void run() {
      thread.interrupt();
    }
  }
}
