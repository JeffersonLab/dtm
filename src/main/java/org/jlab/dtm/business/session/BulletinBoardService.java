package org.jlab.dtm.business.session;

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
import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.jlab.dtm.persistence.enumeration.BulletinBoardCategory;
import org.jlab.dtm.persistence.entity.DtmSettings;
import org.jlab.dtm.persistence.enumeration.BulletinBoardPriority;

/**
 *
 * @author ryans
 */
@Stateless
public class BulletinBoardService {

    private static final long MAX_EXECUTE_MILLIS = 10000;

    private static final Logger logger = Logger.getLogger(
            BulletinBoardService.class.getName());

    @EJB
    private DtmSettingsFacade settingsFacade;
    
    private String execPath;
    private String category;
    
    @PostConstruct
    private void init() {
        DtmSettings settings = settingsFacade.findSettings();

        execPath = settings.getBulletinBoardPath();

        if (execPath == null || execPath.isEmpty()) {
            execPath = System.getenv("bbmsg");

            if (execPath == null || execPath.isEmpty()) {
                throw new RuntimeException("Path to bbmsg executable must be specified in an environment variable 'bbmsg' or in the DTM_SETTINGS database table");
            }
        }

        logger.log(Level.FINE, "execPath: {0}", execPath);

        File exec = new File(execPath);

        if (!exec.exists()) {
            throw new RuntimeException("Executable bbmsg does not exist");
        }
        
        category = settings.getBulletinBoardCategory();
        
        // Throws IllegalArgumentException if category isn't in enum list
        BulletinBoardCategory.valueOf(category);
    }

    @PermitAll
    public void postMessage(String message, BulletinBoardPriority priority) throws IOException {

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
