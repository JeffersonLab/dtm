package org.jlab.dtm.persistence.model;

/**
 *
 * @author ryans
 */
public class BeamSummaryTotals {
    private final long upSeconds;
    private final long sadSeconds;
    private final long downSeconds;
    private final long studiesSeconds;
    private final long restoreSeconds;
    private final long accSeconds;

    public BeamSummaryTotals(Number upSeconds, Number sadSeconds, Number downSeconds,
            Number studiesSeconds, Number restoreSeconds, Number accSeconds) {
        this.upSeconds = upSeconds.longValue();
        this.sadSeconds = sadSeconds.longValue();
        this.downSeconds = downSeconds.longValue();
        this.studiesSeconds = studiesSeconds.longValue();
        this.restoreSeconds = restoreSeconds.longValue();
        this.accSeconds = accSeconds.longValue();
    }
    
    public long getUpSeconds() {
        return upSeconds;
    }

    public long getSadSeconds() {
        return sadSeconds;
    }

    public long getDownSeconds() {
        return downSeconds;
    }

    public long getStudiesSeconds() {
        return studiesSeconds;
    }

    public long getRestoreSeconds() {
        return restoreSeconds;
    }

    public long getAccSeconds() {
        return accSeconds;
    }
    
    public long calculateProgramSeconds() {
        return accSeconds + restoreSeconds + studiesSeconds + downSeconds + upSeconds;
    }
}
