/*******************************************************************************
 * Copyright (c) 2022 Redlink GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package io.redlink.nlp.time;

import io.redlink.nlp.model.temporal.Temporal;
import io.redlink.nlp.model.temporal.Temporal.Grain;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtils {

    private static Logger log = LoggerFactory.getLogger(DateUtils.class);

    private static ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX", Locale.GERMAN);
        }
    };

    /**
     * This method allows to convert an eclusive end date to an inclusive by considering the
     * the {@link Grain} of the parsed Temporal.
     * <p>
     * Intervals are typical represented with an exclusive end. So the text
     * <pre>1.3-3.3.2016</pre> will result in an interval
     * with <code>2016-03-01T00:00:00.000</code> and <code>2016-03-04T00:00:00.000</code>
     * both with {@link Grain#day}. The inclusive end of the interval would be
     * <code>2016-03-03T00:00:00.000</code>
     *
     * @param exclusive the exclusive end
     * @return the inclusive end based on the {@link Temporal#getGrain()} of the parsed
     * exclusive end
     */
    public static Temporal getInclusiveEnd(Temporal exclusive) {
        if (exclusive.getGrain() == null) {
            return exclusive;
        } else {
            Instant exlInst = exclusive.getDate().toInstant();
            Date inclDate = null;
            ChronoUnit cUnit = exclusive.getGrain().getChronoUnit();
            switch (cUnit) {
                case MONTHS: //month do have different amounts of days :(
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTime(exclusive.getDate());
                    cal.add(Calendar.MONTH, -1);
                    inclDate = cal.getTime();
                    break;
                default:
                    inclDate = Date.from(exlInst.minus(cUnit.getDuration()));
                    break;
            }
            return new Temporal(inclDate, exclusive.getGrain());
        }
    }

    /**
     * Returns the earliest/latest {@link Date} based on the {@link Temporal#getDate()}
     * and {@link Temporal#getGrain()}.
     *
     * @param dateValue the date value
     * @return the earliest possible start in {@link Pair#getLeft()} and the latest
     * possible end in {@link Pair#getRight()}. If the parsed {@link Temporal} does
     * not have a {@link Temporal#getGrain()} the left and right date will be the
     * value of {@link Temporal#getDate()}
     * @throws NullPointerException if the parsed {@link Temporal} or
     *                              {@link Temporal#getDate()} are <code>null</code>
     */
    public static Pair<Date, Date> getDateRange(Temporal dateValue) {
        if (dateValue.getGrain() == null) {
            return new ImmutablePair<>(dateValue.getDate(), dateValue.getDate());
        } else {
            Instant inst = dateValue.getDate().toInstant();
            if (dateValue.getGrain().ordinal() >= Grain.day.ordinal()) {
                //we can only use instant values for grins <= day
                //if we want to deal with weeks, months or years we
                //need to use LocalDateTime and this means to deal with time zones
                LocalTime lt = LocalTime.of(0, 0);
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(dateValue.getDate());
                LocalDateTime ldt = lt.atDate(LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH)));
                LocalDateTime startLdt;
                switch (dateValue.getGrain().getChronoUnit()) {
                    case MONTHS:
                        startLdt = ldt.truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1);
                        break;
                    case WEEKS:
                        startLdt = ldt.truncatedTo(ChronoUnit.DAYS).with(ChronoField.DAY_OF_WEEK, 1);
                        break;
                    case YEARS:
                        startLdt = ldt.truncatedTo(ChronoUnit.DAYS).withDayOfYear(1);
                        break;
                    case CENTURIES:
                    case DECADES:
                    case MILLENNIA:
                        log.warn("{} not supported for truncation of dates (instant: {})", dateValue.getGrain().getChronoUnit(), inst);
                    default:
                        startLdt = ldt; //not supported
                }
                LocalDateTime endLdt = startLdt.plus(1, dateValue.getGrain().getChronoUnit());
                cal.set(Calendar.YEAR, startLdt.getYear());
                cal.set(Calendar.MONTH, startLdt.getMonthValue());
                cal.set(Calendar.DAY_OF_MONTH, startLdt.getDayOfMonth());
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                Date start = cal.getTime();
                cal.set(Calendar.YEAR, endLdt.getYear());
                cal.set(Calendar.MONTH, endLdt.getMonthValue());
                cal.set(Calendar.DAY_OF_MONTH, endLdt.getDayOfMonth());
                Date end = cal.getTime();
                return new ImmutablePair<Date, Date>(start, end);
            } else {
                Instant startInst = inst.truncatedTo(dateValue.getGrain().getChronoUnit());
                Instant endInst = startInst.plus(1, dateValue.getGrain().getChronoUnit());
                return new ImmutablePair<>(Date.from(startInst), Date.from(endInst));
            }
        }
    }

    public static Date getDate(Temporal dateValue, LocalTime defaultTime) {
        Grain grain = dateValue.getGrain();
        if (grain != null && grain.ordinal() >= Grain.day.ordinal()) {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(dateValue.getDate());
            cal.set(Calendar.HOUR_OF_DAY, defaultTime.getHour());
            cal.set(Calendar.MINUTE, defaultTime.getMinute());
            cal.set(Calendar.SECOND, defaultTime.getSecond());
            cal.set(Calendar.MILLISECOND, defaultTime.getSecond());
            return cal.getTime();
        } else {
            return dateValue.getDate();
        }
    }

    /**
     * Tries to convert the parsed value to a Date. Directly supports
     * {@link Temporal}, {@link Date}, {@link Instant} and {@link Calendar}.
     * For other types it tries to parse the Date from the string value of
     * the parsed object.
     *
     * @param value the value
     * @return the {@link Date} or <code>null</code> if <code>null</code> was
     * parsed or the parsed object could not be parsed.
     */
    public static Date toDate(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Temporal) {
            return ((Temporal) value).getDate();
        } else if (value instanceof Date) {
            return (Date) value;
        } else if (value instanceof Calendar) {
            return ((Calendar) value).getTime();
        } else if (value instanceof Instant) {
            return Date.from((Instant) value);
        } else {
            try {
                return dateFormat.get().parse(String.valueOf(value));
            } catch (ParseException e) {
                log.warn("Could not parse date '{}' from token: {}", value, e.getMessage());
                return null;
            }
        }
    }

//    public static void main(String[] args) {
//        DateValue dv = new DateValue();
//        dv.setDate(new Date());
//        dv.setGrain(Grain.hour);
//        Pair<Date,Date> range = getDateRange(dv);
//        System.out.println(new SimpleDateFormat().format(range.getLeft()));
//        System.out.println(new SimpleDateFormat().format(range.getRight()));
//    }

}
