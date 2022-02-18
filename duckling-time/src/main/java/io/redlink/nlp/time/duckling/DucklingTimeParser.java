/*
 * Copyright (c) 2016-2022 Redlink GmbH.
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
package io.redlink.nlp.time.duckling;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.LazySeq;
import clojure.lang.PersistentArrayMap;
import io.redlink.nlp.model.temporal.Temporal;
import io.redlink.nlp.model.temporal.Temporal.Grain;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DucklingTimeParser {

    private static final Logger LOG = LoggerFactory.getLogger(DucklingTimeParser.class);

    // This is format how duckling sends the result
    protected static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        }
    };

    private static final Pattern TIMEZONE_PATTERN = Pattern.compile("(Z|[+-]\\d\\d:?\\d\\d)$");

    /**
     * Compares {@link DateToken} similar as the {@link DateToken#IDX_START_END_COMPARATOR}
     */
    public static final Comparator<DateToken> DATE_TOKEN_COMP = DateToken.IDX_START_END_COMPARATOR;

    private boolean includeLatent = false;
    private boolean initialized = false;

    // References to functions
    private IFn parse;
    private IFn createTime;

    private Object dims;

    private Object keyRefTime, keyMin, keyMax, keyDim, keyValue, keyStart, keyEnd, keyGrain, keyLatent, keyType, keyFrom, keyTo;

//    private Object valDateMin, valDateMax;

    public DucklingTimeParser() {
        parse = null;
    }

    public void setIncludeLatent(boolean includeLatent) {
        this.includeLatent = includeLatent;
    }

    public boolean isIncludeLatent() {
        return includeLatent;
    }

    /**
     * Initialize the parser. Calling this is optional.
     */
    public void init() {
        init(getClass().getClassLoader());
    }

    /**
     * Initialize the parser. Calling this is optional.
     *
     * @param classLoader the class-loader duckling should use to load language-resources.
     *                    Provide {@code null} to use the default of duckling.
     */
    public synchronized void init(ClassLoader classLoader) {
        if (initialized) return;
        LOG.debug("Initializing duckling time");
        final long initStart = System.currentTimeMillis();
        final Runnable clojureLoader = () -> {
            final IFn require = Clojure.var("clojure.core", "require");
            require.invoke(Clojure.read("duckling.core"));

            final IFn load = Clojure.var("duckling.core", "load!");
            load.invoke();

            createTime = Clojure.var("duckling.time.obj", "t");
            parse = Clojure.var("duckling.core", "parse");

            keyRefTime = Clojure.read(":reference-time");
            keyMin = Clojure.read(":min");
            keyMax = Clojure.read(":max");
            dims = Clojure.read("[:time]");

            keyDim = Clojure.read(":dim");
            keyValue = Clojure.read(":value");
            keyStart = Clojure.read(":start");
            keyEnd = Clojure.read(":end");
            keyGrain = Clojure.read(":grain");
            keyLatent = Clojure.read(":latent");

            keyType = Clojure.read(":type");
            keyFrom = Clojure.read(":from");
            keyTo = Clojure.read(":to");

            // valDateMin = createTime.invoke(0, 2000);
            // valDateMax = createTime.invoke(0, 2100);
        };

        final Thread clojureLoaderThread = new Thread(clojureLoader);

        if (classLoader != null) {
            LOG.debug("Using ClassLoader {} for context", classLoader);
            clojureLoaderThread.setContextClassLoader(classLoader);
        }
        clojureLoaderThread.start();
        try {
            clojureLoaderThread.join();
            LOG.debug("DucklingTime loaded in {}ms", System.currentTimeMillis() - initStart);
            initialized = true;
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for DucklingTime to initialize", e);
            Thread.currentThread().interrupt();
        }
    }

    public void shutdown() {
        // TODO: Something to do here?
        parse = null;
    }

    /**
     * Check if the language is supported
     *
     * @param lang the language
     * @return {@code true} if time-parsing for the provided language is supported
     */
    public boolean isLanguageSupported(String lang) {
        return getClass().getClassLoader().getResource(String.format("languages/%s/rules/time.clj", lang)) != null;
    }

    /**
     * Check if the language is supported
     *
     * @param locale the language
     * @return {@code true} if time-parsing for the provided language is supported
     */
    public boolean isLanguageSupported(Locale locale) {
        return isLanguageSupported(locale.getLanguage());
    }

    /**
     * Parse a message for DateTokens.
     *
     * @param message the message to parse
     * @return a List of extracted {@link DateToken}s
     * @deprecated use {@link #parse(String, String)}
     */
    @Deprecated
    public List<DateToken> parse(String message) {
        return parse(message, "de", new Date());
    }

    /**
     * Parse a message for DateTokens.
     *
     * @param message the message to parse
     * @return a List of extracted {@link DateToken}s
     * @deprecated use {@link #parse(String, String, Date)}
     */
    @Deprecated
    public List<DateToken> parse(String message, Date referenceDate) {
        return parse(message, "de", referenceDate);
    }

    /**
     * Parse a message for DateTokens.
     *
     * @param message  the message to parse
     * @param language the language of the message
     * @return a List of extracted {@link DateToken}s
     */
    public List<DateToken> parse(String message, Locale language) {
        return parse(message, language.getLanguage(), new Date());
    }

    /**
     * Parse a message for DateTokens.
     *
     * @param message  the message to parse
     * @param language the language of the message
     * @return a List of extracted {@link DateToken}s
     */
    public List<DateToken> parse(String message, String language) {
        return parse(message, language, new Date());
    }

    /**
     * Parse a message for DateTokens.
     *
     * @param message       the message to parse
     * @param language      the language of the message
     * @param referenceDate the context/reference date for relative times/dates (e.g. "next week")
     * @return a List of extracted {@link DateToken}s
     */
    public List<DateToken> parse(String message, Locale language, Date referenceDate) {
        return parse(message, language.getLanguage(), referenceDate);
    }

    /**
     * Parse a message for DateTokens.
     *
     * @param message       the message to parse
     * @param language      the language of the message
     * @param referenceDate the context/reference date for relative times/dates (e.g. "next week")
     * @param includeLatent if low confidence results should be considered
     * @return a List of extracted {@link DateToken}s
     */
    public List<DateToken> parse(String message, final String language, Date referenceDate) {
        return parse(message, language, referenceDate, null);
    }

    /**
     * Parse a message for DateTokens.
     *
     * @param message       the message to parse
     * @param language      the language of the message
     * @param referenceDate the context/reference date for relative times/dates (e.g. "next week")
     * @param includeLatent if low confidence results should be considered. <code>null</code> will
     *                      use the default set for the parser instance.
     * @return a List of extracted {@link DateToken}s
     */
    public List<DateToken> parse(String message, final String language, Date referenceDate, Boolean includeLatent) {
        init();
        LOG.debug("Analyzing ({}) {}", language, message);
        // Reference-Time
        Date context = referenceDate;
        Grain contextGrain = Grain.second;
        int offset = 0;
        List<DateToken> contextualizedTokens = new LinkedList<>();
        List<DateToken> tokens = extractTokens(offset, message, language, context, includeLatent);
        while (!tokens.isEmpty()) {
            DateToken token = tokens.get(0);
            final Temporal value = ObjectUtils.firstNonNull(token.getStart(), token.getEnd());
            if (!contextualizedTokens.isEmpty()) {
                if (contextGrain.ordinal() > value.getGrain().ordinal() &&
                        saveTruncatedCompareTo(context, value.getDate(), contextGrain) == 0) {
                    //the current token has the same time as the context but with a finer grain
                    //so we want to replace the current context with the new one
                    contextualizedTokens.remove(contextualizedTokens.size() - 1);
                } else if (token.isOpenInterval() && token.getStart() == null) {
                    //check if we can combine this open interval with the last contextualized token
                    DateToken last = contextualizedTokens.get(contextualizedTokens.size() - 1);
                    if (last.isInstant() || last.isOpenInterval() && last.getEnd() == null &&
                            token.getEnd().getDate().after(last.getStart().getDate())) {
                        contextualizedTokens.remove(contextualizedTokens.size() - 1);
                        DateToken combined = new DateToken();
                        combined.setConfidence(Math.max(last.getConfidence(), token.getConfidence()));
                        combined.setOffsetStart(last.getOffsetStart());
                        combined.setOffsetEnd(last.getOffsetEnd());
                        combined.setInstant(false);
                        combined.setStart(last.getStart());
                        combined.setEnd(token.getEnd());
                        LOG.debug("combined {} wiht {} to interval {}", last, token, combined);
                        token = combined;
                    }
                }
            }
            contextualizedTokens.add(token); //add the first returned token to the contextualized tokens
            //search for more Tokens with the same mention (durations)
            int nextIdx = 1;
            for (; nextIdx < tokens.size(); nextIdx++) {
                DateToken next = tokens.get(nextIdx);
                if (DateToken.IDX_START_END_COMPARATOR.compare(token, next) == 0) {
                    contextualizedTokens.add(next);
                } else {
                    break;
                }
            }
            //still more tokens ... we need to re-parse those with the last
            //extracted time as context
            if (tokens.size() > nextIdx) {
                context = value.getDate(); //use the extracted time as context
                contextGrain = value.getGrain();
                if (contextGrain.ordinal() >= Grain.day.ordinal()) {
                    if (DateUtils.truncatedEquals(referenceDate, context, Calendar.DAY_OF_MONTH)) {
                        context = referenceDate; //keeo the time of the message as context
                    } else {
                        //Duckling time uses the hours of the context for
                        //deciding 12/24 hours format. 
                        //having the context time set to 00:00 is bad for
                        //those heuristics hence we do set it to early in 
                        //the morning
                        //TODO: check if 6:00 is a good default time
                        context = DateUtils.setHours(context, 6);
                    }
                }
                offset = token.getOffsetEnd();
                tokens = extractTokens(offset, message, language, context, includeLatent);
            } else { //no more tokens
                tokens.clear();
            }
        }
        return contextualizedTokens;
    }

    /**
     * Woraround for {@link DateUtils#truncate(Date, int)} is not
     * supported for all {@link Grain}s ({@link Calendar#WEEK_OF_YEAR} in the case of
     * the reported issue).
     * <p>
     * This Method works around this issue by repeatedly trying smaller {@link Grain}s
     * until it succeeds or it has no longer a smaller {@link Grain} to try. In this
     * case it will just compare based on the parsed {@link Date}s
     */
    private int saveTruncatedCompareTo(Date date1, Date date2, Grain parsedGrain) {
        Grain grain = parsedGrain;
        Date truncatedDate1 = null;
        Date truncatedDate2 = null;
        while (truncatedDate2 == null && grain != null) {
            try {
                truncatedDate1 = DateUtils.truncate(date1, grain.getDateField());
                truncatedDate2 = DateUtils.truncate(date2, grain.getDateField());
            } catch (IllegalArgumentException e) {
                Grain smallerGrain = grain.ordinal() > 0 ? Grain.values()[grain.ordinal() - 1] : null;
                if (smallerGrain != null) {
                    LOG.debug("Can not turncate Dates with Grain {} will use {} instead", grain, smallerGrain);
                }
                grain = smallerGrain;
            }
        }
        if (truncatedDate1 == null) {
            LOG.warn("Unable to turncate Date {} using Grain {} (will use original Date for comparision)", date1, parsedGrain);
            truncatedDate1 = date1;
        }
        if (truncatedDate2 == null) {
            LOG.warn("Unable to turncate Date {} using Grain {} (will use original Date for comparision)", date2, parsedGrain);
            truncatedDate2 = date2;
        }
        return truncatedDate1.compareTo(truncatedDate2);
    }

    private List<DateToken> extractTokens(int offset, String content, String language, Date context, Boolean includeLatentState) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extract tokens [offset: {} | context: {} | content: {}]",
                    offset, DATE_FORMAT.get().format(context), content.substring(offset));
        }
        boolean includeLatent = includeLatentState == null ? this.includeLatent : includeLatentState.booleanValue();
        final Object referenceTime = createTime(context);
        final Object[] minMaxTime = createMinMaxTime(context);

        final Calendar cal = new GregorianCalendar();
        cal.setTime(context);
        final TimeZone contextTimeZone = cal.getTimeZone();

        final IPersistentMap contextMap = PersistentArrayMap.create(new HashMap<>());
        // TODO: The min/max does not really work... is there something missing?
        final IPersistentMap assoc = contextMap
                .assoc(keyMin, minMaxTime[0])
                .assoc(keyMax, minMaxTime[1])
                .assoc(keyRefTime, referenceTime);

        final Object o = parse.invoke(language + "$core", content.substring(offset), dims, assoc);

        //noinspection unchecked
        @SuppressWarnings("unchecked") final Iterator<PersistentArrayMap> map = ((LazySeq) o).iterator();

        Set<Triple<String, Integer, Integer>> matches = new HashSet<>();

        final List<DateToken> tokens = new ArrayList<>();
        while (map.hasNext()) {
            try {
                final PersistentArrayMap next = map.next();
                LOG.trace("Duckling-Match: {}", next);
                String dimension = String.valueOf(next.valAt(keyDim));
                final int startOffset = ((Long) next.valAt(keyStart)).intValue() + offset;
                final int endOffset = ((Long) next.valAt(keyEnd)).intValue() + offset;
                //check if we do have already a match for this span and dimension
                if (matches.add(new ImmutableTriple<String, Integer, Integer>(dimension, startOffset, endOffset)) == false) {
                    LOG.debug("ignore {} because existing match for {}@[{},{}] {}", next, dimension, startOffset, endOffset);
                    continue;
                }
                final boolean isLatent = BooleanUtils.isTrue((Boolean) next.valAt(keyLatent));
                if (":time".equals(dimension)) {
                    if (!includeLatent && isLatent) {
                        continue; //ignore latent times
                    }
                    float conf = isLatent ? 0.1f : 0.9f;
                    final IPersistentMap value = (IPersistentMap) next.valAt(keyValue);
                    final String valueType = String.valueOf(value.valAt(keyType, "date"));
                    if ("interval".equals(valueType)) {
                        final DateToken t = new DateToken();
                        t.setInstant(false);
                        t.setOffsetStart(startOffset);
                        t.setOffsetEnd(endOffset);
                        t.setConfidence(conf);
                        t.setStart(parseDateValue((IPersistentMap) value.valAt(keyFrom), contextTimeZone));
                        t.setEnd(parseDateValue((IPersistentMap) value.valAt(keyTo), contextTimeZone));
                        tokens.add(t);
                    } else { //parse date value
                        final DateToken t = new DateToken();
                        t.setInstant(true);
                        t.setOffsetStart(startOffset);
                        t.setOffsetEnd(endOffset);
                        t.setConfidence(conf);
                        final Temporal dateValue = parseDateValue(value, contextTimeZone);
                        t.setStart(dateValue);
                        //t.setEnd(dateValue); //instants only have a start
                        tokens.add(t);
                    }
                } else if (":duration".equals(dimension)) {
                    //TODO: add support for durations
                    if (!includeLatent && isLatent) {
                        continue; //ignore latent times
                    }
                    //noinspection unused
                    float conf = isLatent ? 0.1f : 0.9f;
                    //noinspection unused
                    final IPersistentMap value = (IPersistentMap) next.valAt(keyValue);

                    LOG.debug("Duration not supported");
                }
            } catch (ParseException e) {
                LOG.error("Duckling returned invalid date-string: {}", e.getMessage(), e);
            }
        }
        Collections.sort(tokens, DATE_TOKEN_COMP);
        return tokens;
    }

    /**
     * Parsed a {@link DateValue} from an {@link IPersistentMap} supporting the
     * <ul>
     * <li>{@link #keyValue} expected to hold a Date parsed by {@link #DATE_FORMAT}
     * <li> {@link #keyGrain} with a value contained in {@link Grain}
     * </ul>
     *
     * @param value           the {@link IPersistentMap} instance to parse the dateValue from
     * @param contextTimeZone the {@link TimeZone} used as context for parsed dateTime values
     * @return the parsed Date value
     * @throws ParseException in case the value of the {@link #keyValue} can not
     *                        be parsed by the {@link #DATE_FORMAT}
     */
    private Temporal parseDateValue(IPersistentMap value, TimeZone contextTimeZone) throws ParseException {
        if (value == null) return null;

        final Temporal dateValue = new Temporal();
        String dateStr = String.valueOf(value.valAt(keyValue));
        LOG.debug("Parsing duckling time-string {} into a DateValue", dateStr);

        //cut the time zone
        dateStr = TIMEZONE_PATTERN.matcher(dateStr).replaceFirst("");
        LOG.trace("Stripped time-zone: {}", dateStr);

        //now parse the date (without TimeZone) and set the TimeZone to the one
        //used by the client
        final Calendar cal = new GregorianCalendar();
        cal.setTime(DATE_FORMAT.get().parse(dateStr));
        cal.setTimeZone(contextTimeZone);
        dateValue.setDate(cal.getTime());
        //finally parse the grain
        final Object grain = value.valAt(keyGrain);
        if (grain != null) {
            String sGrain = String.valueOf(grain);
            if (sGrain.charAt(0) == ':') {
                sGrain = sGrain.substring(1);
            }
            try {
                dateValue.setGrain(Grain.valueOf(sGrain));
            } catch (RuntimeException e) {
                LOG.warn("Unknown Grain value {} (supported: {})", sGrain, Arrays.toString(Grain.values()));
            }
        }
        LOG.debug("Parsed {} into {}", dateStr, dateValue);
        return dateValue;
    }

    protected Object createTime(Date time) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        return createTime.invoke(
                (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (60 * 60 * 1000), // Offsets are in millis
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1, // zero-based month!
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND)
        );
    }

    protected Object[] createMinMaxTime(Date time) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        // Offsets are in millis
        int tzOffset = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (60 * 60 * 1000);
        return new Object[]{
                createTime.invoke(tzOffset, cal.get(Calendar.YEAR) - 1),
                createTime.invoke(tzOffset, cal.get(Calendar.YEAR) + 5)};
    }
}
