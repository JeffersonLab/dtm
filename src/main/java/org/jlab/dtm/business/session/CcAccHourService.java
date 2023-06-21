package org.jlab.dtm.business.session;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jlab.dtm.persistence.model.BeamSummaryTotals;
import org.jlab.smoothness.business.util.DateIterator;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.util.JPAUtil;

/**
 *
 * @author ryans
 */
@Stateless
public class CcAccHourService extends AbstractFacade<Object> {

    private final static Logger logger = Logger.getLogger(CcAccHourService.class.getName());

    @PersistenceContext(unitName = "dtmPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CcAccHourService() {
        super(Object.class);
    }

    @PermitAll
    public BeamSummaryTotals reportTotals(Date start, Date end) {
        Query q = em.createNativeQuery(
                "select sum(up_seconds), sum(sad_seconds), sum(down_seconds), sum(studies_seconds), sum(restore_seconds), sum(acc_seconds) "
                + "from ("
                + "select up_seconds, sad_seconds, down_seconds, studies_seconds, restore_seconds, acc_seconds from dtm_owner.cc_acc_hour "
                + "where day_and_hour >= :start and day_and_hour < :end "
                + "union all select 0, 0, 0, 0, 0, 0 from dual)");

        q.setParameter("start", start);
        q.setParameter("end", end);

        List<BeamSummaryTotals> totalsList = JPAUtil.getResultList(q, BeamSummaryTotals.class);

        BeamSummaryTotals totals = null;

        if (totalsList != null && !totalsList.isEmpty()) {
            totals = totalsList.get(0);
        }

        return totals;
    }

    @PermitAll
    public List<MonthTotals> monthTotals(Date start, Date end) {
        List<MonthTotals> monthTotals = new ArrayList<>();

        Date startMonthDayOne = TimeUtil.startOfMonth(start, Calendar.getInstance());
        Date endMonthDayOne = TimeUtil.startOfMonth(end, Calendar.getInstance());

        // We have use an open end of interval so don't include final month if day one
        if(endMonthDayOne.equals(end)) {
            endMonthDayOne = TimeUtil.addMonths(end, -1);
        }
        
        DateIterator iterator = new DateIterator(startMonthDayOne, endMonthDayOne, Calendar.MONTH);

        while (iterator.hasNext()) {
            Date month = iterator.next();

            Date startOfMonth = TimeUtil.startOfMonth(month, Calendar.getInstance());
            Date startOfNextMonth = TimeUtil.startOfNextMonth(month, Calendar.getInstance());

            //System.out.println("Start of Month: " + formatter.format(startOfMonth));
            //System.out.println("End (start of next Month): " + formatter.format(startOfNextMonth));
            Date realStart = (startMonthDayOne.getTime() == month.getTime()) ? start : startOfMonth;
            Date realEnd = iterator.hasNext() ? startOfNextMonth : end;

            BeamSummaryTotals totals = this.reportTotals(realStart, realEnd);
            MonthTotals mt = new MonthTotals();
            mt.month = startOfMonth;
            mt.totals = totals;
            monthTotals.add(mt);
        }

        return monthTotals;
    }

    public class MonthTotals {

        Date month;
        BeamSummaryTotals totals;

        public Date getMonth() {
            return month;
        }

        public BeamSummaryTotals getTotals() {
            return totals;
        }
    }
}
