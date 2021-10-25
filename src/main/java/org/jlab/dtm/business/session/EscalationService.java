package org.jlab.dtm.business.session;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import org.jlab.dtm.persistence.entity.Event;
import org.jlab.dtm.persistence.enumeration.EscalationLevel;
import org.jlab.dtm.persistence.enumeration.BulletinBoardPriority;
import org.jlab.dtm.persistence.model.EscalationInfo;

/**
 *
 * @author ryans
 */
@Singleton
//@Startup
public class EscalationService {

    private static final Logger logger = Logger.getLogger(
            EscalationService.class.getName());

    public final static long TWO_HOUR_MILLIS = 7200000;

    @Resource
    TimerService timerService;

    @EJB
    BulletinBoardService bbService;

    @PermitAll
    public void startEscalationTimer(BigInteger eventId, String eventTitle, EscalationLevel level, long durationMillis) {
        logger.log(Level.FINE, "Starting Escalation Timer for eventId: {0}, with level: {1}, and durationMillis: {2}", new Object[]{eventId, level, durationMillis});
        if(eventId == null) {
            throw new IllegalArgumentException("EventId must not be null");
        }        
        EscalationInfo info = new EscalationInfo(eventId, eventTitle, level);
        Timer timer = timerService.createSingleActionTimer(durationMillis, new TimerConfig(info, true));
    }

    @PermitAll
    public void cancelEscalationTimer(Event event) {
        logger.log(Level.FINE, "Canceling Escalation Timer for eventId: {0}", event.getEventId());

        Collection<Timer> timerCollection = timerService.getAllTimers();

        for (Timer timer : timerCollection) {
            EscalationInfo info = (EscalationInfo) timer.getInfo();

            if (info != null && event.getEventId() != null && event.getEventId().equals(info.getEventId())) {
                timer.cancel();
                break;
            }
        }
    }

    @Timeout
    @PermitAll
    public void escalate(Timer timer) {
        EscalationInfo info = (EscalationInfo) timer.getInfo();

        try {
            switch (info.getEscalationLevel()) {
                case TWO_HOUR:
                    logger.log(Level.FINE, "TWO_HOUR Escalation Triggered: {0}", info);
                    startEscalationTimer(info.getEventId(), info.getEventTitle(), EscalationLevel.FOUR_HOUR, TWO_HOUR_MILLIS);
                    bbService.postMessage("Downtime two hour escalation needed for event: " + info.getEventTitle(), BulletinBoardPriority.MEDIUM);
                    break;
                case FOUR_HOUR:
                    logger.log(Level.FINE, "FOUR_HOUR Escalation Triggered: {0}", info);
                    bbService.postMessage("Downtime four hour escalation needed for event: " + info.getEventTitle(), BulletinBoardPriority.HIGH);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown escalation level: " + info.getEscalationLevel());
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Unable to send escalation message to bulletin board", e);
        }
    }

    @PermitAll
    public void resetEscalation(Event event) {
        logger.log(Level.FINEST, "resetEscalation - eventId: {0}", event.getEventId());
        cancelEscalationTimer(event);
        startEscalationTimer(event);
    }

    @PermitAll
    public void startEscalationTimer(Event event) {
        logger.log(Level.FINEST, "startEscalationTimer - eventId = {0}", event.getEventId());
        long durationMillis;

        long elapsedMillis = event.getElapsedMillis();

        if(event.isClosed()) {
            logger.log(Level.FINE, "Skipping event {0} escalation timer because event is closed", event.getEventId());
        } else if (elapsedMillis < TWO_HOUR_MILLIS) {
            logger.log(Level.FINEST, "Using TWO_HOUR escalation level for eventId: {0}", event.getEventId());
            durationMillis = TWO_HOUR_MILLIS - elapsedMillis;

            startEscalationTimer(event.getEventId(), event.getTitle(), EscalationLevel.TWO_HOUR, durationMillis);
        } else if (elapsedMillis < (TWO_HOUR_MILLIS * 2)) {
            logger.log(Level.FINEST, "Using FOUR_HOUR escalation level for eventId: {0}", event.getEventId());
            durationMillis = (TWO_HOUR_MILLIS * 2) - elapsedMillis;

            startEscalationTimer(event.getEventId(), event.getTitle(), EscalationLevel.FOUR_HOUR, durationMillis);
        } else {
            logger.log(Level.FINE, "Skippping event {0} escalation timer since escalation times have already passed", event.getEventId());
        }
    }

    @PermitAll
    public int getEscalationLevel(Event event) {
        int level = 0;
        long elapsed = event.getElapsedMillis();
        if(elapsed >= EscalationService.TWO_HOUR_MILLIS * 2) {
            level = 2;
        } else if(elapsed >= EscalationService.TWO_HOUR_MILLIS) {
            level = 1;
        }
        
        return level;
    }
}
