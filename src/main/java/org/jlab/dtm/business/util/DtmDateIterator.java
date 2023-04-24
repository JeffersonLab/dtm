package org.jlab.dtm.business.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class DtmDateIterator implements Iterator<Date>, Iterable<Date> {
        private final Calendar end;
        private final Calendar current;
        private final int field;
        private final int amount;

        public DtmDateIterator(Date start, Date end, int field, int amount) {
            this.end = Calendar.getInstance();
            this.current = Calendar.getInstance();
            this.field = field;
            this.amount = amount;
            this.end.setTime(end);
            this.end.add(field, -1 * amount);
            this.current.setTime(start);
            this.current.add(field, -1 * amount);
        }

        public int getField() {
            return this.field;
        }

        public boolean hasNext() {
            return this.current.before(this.end);
        }

        public Date next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            } else {
                this.current.add(this.field, amount);
                return this.current.getTime();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException("Cannot Remove");
        }

        public Iterator<Date> iterator() {
            return this;
        }
}
