/*
 * Copyright (c) 2022 Redlink GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.redlink.nlp.model.temporal;

import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT;

import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * Date/Time values provide the {@link Date} as well as the precision ({@link Grain}).
 * 
 * @author Rupert Westenthaler
 *
 */
public class Temporal {
    
    /**
     * Time grains. Higher ordinal means longer duration
     * @author Rupert Westenthaler
     *
     */
    @SuppressWarnings("java:S115")
    public enum Grain {
        millisecond(ChronoUnit.MILLIS, Calendar.MILLISECOND),
        second(ChronoUnit.SECONDS, Calendar.SECOND),
        minute(ChronoUnit.MINUTES, Calendar.MINUTE),
        hour(ChronoUnit.HOURS, Calendar.HOUR_OF_DAY),
        day(ChronoUnit.DAYS, Calendar.DAY_OF_MONTH),
        week(ChronoUnit.WEEKS, Calendar.WEEK_OF_YEAR),
        month(ChronoUnit.MONTHS, Calendar.MONTH),
        //quarter(null),
        year(ChronoUnit.YEARS, Calendar.YEAR);
        
        private final ChronoUnit chronoUnit;
        private final int dateField;

        Grain(ChronoUnit chronoUnit, int dateField){
            this.chronoUnit = chronoUnit;
            this.dateField = dateField;
        }
        
        public ChronoUnit getChronoUnit() {
            return chronoUnit;
        }

        public int getDateField() {
            return dateField;
        }
    }
    
    private Date date;
    
    private Grain grain = Grain.second;

    public Temporal() {
    }

    public Temporal(Date date) {
        this(date, Grain.second);
    }

    public Temporal(Date date, Grain grain) {
        this.date = date;
        this.grain = grain;
    }

    public final Date getDate() {
        return date;
    }
    public final void setDate(Date date) {
        this.date = date;
    }
    public final Grain getGrain() {
        return grain;
    }
    public final void setGrain(Grain grain) {
        this.grain = grain;
    }
    
    @Override
    public String toString() {
        return ISO_DATETIME_TIME_ZONE_FORMAT.format(date) + "(grain=" + grain + ")";
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((grain == null) ? 0 : grain.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Temporal other = (Temporal) obj;
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!date.equals(other.date))
            return false;
        return grain == other.grain;
    }
    
    
    
}
