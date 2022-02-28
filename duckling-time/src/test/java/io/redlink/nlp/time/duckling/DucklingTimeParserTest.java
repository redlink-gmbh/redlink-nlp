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

import io.redlink.nlp.model.temporal.Temporal;
import io.redlink.nlp.model.temporal.Temporal.Grain;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class DucklingTimeParserTest {

    private static DucklingTimeParser ducklingTimeParser;

    private Calendar refereceCal = new GregorianCalendar(2016, Calendar.APRIL, 1, 8, 0);//Calendar.getInstance();
    private Date referenceDate = refereceCal.getTime();

    private static boolean defaultLatent;

    private Logger log = LoggerFactory.getLogger(DucklingTimeParserTest.class);

    @Rule
    public TestWatcher logTestMethod = new TestWatcher() {
        @Override
        protected void starting(Description description) {
            log.debug("Starting {}", description.getMethodName());
        }

        @Override
        protected void finished(Description description) {
            log.debug("Finished {}", description.getMethodName());
        }
    };

    @BeforeClass
    public static void setUpClass() throws Exception {
        ducklingTimeParser = new DucklingTimeParser();
        //ducklingTimeParser.setIncludeLatent(true);
        ducklingTimeParser.init(DucklingTimeParser.class.getClassLoader());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        ducklingTimeParser.shutdown();
        defaultLatent = ducklingTimeParser.isIncludeLatent();
    }

    @Before
    public void setUp() {
        //ensure that Latent=false is the default
        ducklingTimeParser.setIncludeLatent(defaultLatent);
    }

    @Test
    public void testSimpleDateDe() throws Exception {
        final List<DateToken> tokens = ducklingTimeParser.parse("Morgen um 9 Uhr", "de", referenceDate);

        assertEquals("Num of Tokens", 1, tokens.size());
        final DateToken token = tokens.get(0);
        assertThat("Class/Type", token.getStart(), CoreMatchers.instanceOf(Temporal.class));

        final Calendar cal = Calendar.getInstance();
        cal.setTime(token.getStart().getDate());
        assertEquals("+1 day", refereceCal.get(Calendar.YEAR), cal.get(Calendar.YEAR));
        assertEquals("9am", 9, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals("9am", 0, cal.get(Calendar.MINUTE));
    }

    @Test
    public void testSimpleDateEn() throws Exception {
        final List<DateToken> tokens = ducklingTimeParser.parse("Tomorrow at 9am", "en", referenceDate);

        assertEquals("Num of Tokens", 1, tokens.size());
        final DateToken token = tokens.get(0);
        assertThat("Class/Type", token.getStart(), CoreMatchers.instanceOf(Temporal.class));

        final Calendar cal = Calendar.getInstance();
        cal.setTime(token.getStart().getDate());
        assertEquals("+1 day", refereceCal.get(Calendar.YEAR), cal.get(Calendar.YEAR));
        assertEquals("9am", 9, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals("9am", 0, cal.get(Calendar.MINUTE));
    }

    @Test
    public void testMultipleContexts() throws Exception {
        //define vars so that I can easily comment tests
        List<DateToken> tokens;
        Calendar cal;
        DateToken token;

        tokens = ducklingTimeParser.parse("Hallo, ich will am 27. Mai, gegen 18 Uhr mit dem ICE von Köln nach"
                + " Berlin fahren und am 29. Abends zurück nach Bonn", "de", referenceDate);

        Assert.assertTrue(tokens.size() > 1); //just make sure we do not run in an IooBE
        token = tokens.get(0);
        assertThat("Class/Type", token.getStart(), CoreMatchers.instanceOf(Temporal.class));
        assertTrue("instant", token.isInstant());

        cal = Calendar.getInstance();
        cal.setTime(token.getStart().getDate());
        assertEquals("same year", refereceCal.get(Calendar.YEAR), cal.get(Calendar.YEAR));
        assertEquals("May", Calendar.MAY, cal.get(Calendar.MONTH));
        assertEquals("27th", 27, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals("18h", 18, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals("00m", 0, cal.get(Calendar.MINUTE));

        token = tokens.get(1);
        assertThat("Class/Type", token.getStart(), CoreMatchers.instanceOf(Temporal.class));
        assertTrue("interval", token.isInterval());
        assertFalse("closed interval", token.isOpenInterval());

        cal = Calendar.getInstance();
        cal.setTime(token.getStart().getDate());
        assertEquals("same year", refereceCal.get(Calendar.YEAR), cal.get(Calendar.YEAR));
        assertEquals("May", Calendar.MAY, cal.get(Calendar.MONTH)); //this is the central test!!
        assertEquals("29th", 29, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals("17h", 17, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals("00m", 0, cal.get(Calendar.MINUTE));

        cal = Calendar.getInstance();
        cal.setTime(token.getEnd().getDate());
        assertEquals("same year", refereceCal.get(Calendar.YEAR), cal.get(Calendar.YEAR));
        assertEquals("May", Calendar.MAY, cal.get(Calendar.MONTH)); //this is the central test!!
        assertEquals("29th", 29, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals("22h", 22, cal.get(Calendar.HOUR_OF_DAY));


        assertEquals("Num of Tokens", 2, tokens.size());


        tokens = ducklingTimeParser.parse((
                "Hallo, ich brauche übermorgen ein Hotelzimmer im Hamburg ab 10 Uhr für zwei "
                        + "Nächte. Bitte in der Nähe des Hbf in Hamburg."), "de", referenceDate);

        Assert.assertTrue(tokens.size() > 0); //just make sure we do not run in an IooBE
        token = tokens.get(0);
        assertThat("Class/Type", token.getStart(), CoreMatchers.instanceOf(Temporal.class));
        assertTrue("interval", token.isInterval());
        assertFalse("closed interval", token.isOpenInterval());

        cal = Calendar.getInstance();
        cal.setTime(token.getStart().getDate());
        assertEquals("same year", refereceCal.get(Calendar.YEAR), cal.get(Calendar.YEAR));
        assertEquals("April", Calendar.APRIL, cal.get(Calendar.MONTH));
        assertEquals("3rd", 3, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals("10h", 10, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals("00m", 0, cal.get(Calendar.MINUTE));


        cal = Calendar.getInstance();
        cal.setTime(token.getEnd().getDate());
        assertEquals("same year", refereceCal.get(Calendar.YEAR), cal.get(Calendar.YEAR));
        assertEquals("April", Calendar.APRIL, cal.get(Calendar.MONTH)); //this is the central test!!
        assertEquals("5th", 5, cal.get(Calendar.DAY_OF_MONTH));
        //TODO assertEquals("18h", 18, cal.get(Calendar.HOUR_OF_DAY));
        //TODO assertEquals("00m", 0, cal.get(Calendar.MINUTE));

        assertEquals("Num of Tokens", 1, tokens.size());


        tokens = ducklingTimeParser.parse((
                "Hallo, am Samstag 28.5. Komme ich um 12h im HBF "
                        + "Magdeburg an. Was könnte ich bis 16h Unternehmen"), "de", referenceDate);

        Assert.assertEquals(1, tokens.size());
        token = tokens.get(0);
        assertThat("Class/Type", token.getStart(), CoreMatchers.instanceOf(Temporal.class));
        assertFalse("interval", token.isInstant());
        assertTrue("interval", token.isInterval());
        assertFalse("interval", token.isOpenInterval());

        cal = Calendar.getInstance();
        cal.setTime(token.getStart().getDate());
        assertEquals("same year", refereceCal.get(Calendar.YEAR), cal.get(Calendar.YEAR));
        assertEquals("May", Calendar.MAY, cal.get(Calendar.MONTH));
        assertEquals("281h", 28, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals("12h", 12, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals("00m", 0, cal.get(Calendar.MINUTE));

        cal = Calendar.getInstance();
        cal.setTime(token.getEnd().getDate());
        assertEquals("same year", refereceCal.get(Calendar.YEAR), cal.get(Calendar.YEAR));
        assertEquals("May", Calendar.MAY, cal.get(Calendar.MONTH)); //this is the central test!!
        assertEquals("28th", 28, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals("16h", 16, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals("00m", 0, cal.get(Calendar.MINUTE));

    }

    @Test
    public void testInterval() throws Exception {
        ducklingTimeParser.setIncludeLatent(true);
        final List<DateToken> tokens = ducklingTimeParser.parse(("Such am Wochenende ein Hotel in Berlin"), "de", referenceDate);

        assertEquals("Num of Tokens", 2, tokens.size());
        final DateToken token = tokens.get(0);
        assertThat("Class/Type", token.getStart(), CoreMatchers.instanceOf(Temporal.class));
        assertTrue("interval", token.isInterval());
        assertFalse("closed interval", token.isOpenInterval());

        Temporal startValue = token.getStart();
        assertEquals("Grain minute", Grain.hour, startValue.getGrain());

        final Calendar startCal = Calendar.getInstance();
        startCal.setTime(startValue.getDate());
        assertEquals("+1 day", refereceCal.get(Calendar.YEAR), startCal.get(Calendar.YEAR));
        assertEquals("Friday", refereceCal.get(Calendar.DAY_OF_MONTH), startCal.get(Calendar.DAY_OF_MONTH));
        assertEquals("11am", 18, startCal.get(Calendar.HOUR_OF_DAY));
        assertEquals("11am", 0, startCal.get(Calendar.MINUTE));

        assertThat("Class/Type", token.getEnd(), CoreMatchers.instanceOf(Temporal.class));

        Temporal endValue = token.getEnd();
        assertEquals("Grain minute", Grain.hour, endValue.getGrain());

        final Calendar endCal = Calendar.getInstance();
        endCal.setTime(endValue.getDate());
        assertEquals("+1 day", refereceCal.get(Calendar.YEAR), endCal.get(Calendar.YEAR));
        assertEquals("Sunday", refereceCal.get(Calendar.DAY_OF_MONTH) + 2, endCal.get(Calendar.DAY_OF_MONTH));
        assertEquals("22h", 22, endCal.get(Calendar.HOUR_OF_DAY));
        assertEquals("00m", 0, endCal.get(Calendar.MINUTE));

        final DateToken latentYear = tokens.get(1);
        Assert.assertTrue("latent (conf <= 0.1f", latentYear.getConfidence() <= .1f);
    }

    @Test
    public void testOpenInterval() throws Exception {
        final List<DateToken> tokens = ducklingTimeParser.parse(("spätestens um 11:00"), "de", referenceDate);

        assertEquals("Num of Tokens", 1, tokens.size());
        final DateToken token = tokens.get(0);
        assertThat("Class/Type", token.getStart(), CoreMatchers.nullValue());
        assertThat("Class/Type", token.getEnd(), CoreMatchers.instanceOf(Temporal.class));

        Temporal value = token.getEnd();
        assertEquals("Grain minute", Grain.minute, value.getGrain());

        final Calendar cal = Calendar.getInstance();
        cal.setTime(value.getDate());
        assertEquals("+1 day", refereceCal.get(Calendar.YEAR), cal.get(Calendar.YEAR));
        assertEquals("11am", 11, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals("11am", 0, cal.get(Calendar.MINUTE));
    }

    @Test
    public void testWeirdDate() throws Exception {
        ducklingTimeParser.setIncludeLatent(true);
        final List<DateToken> tokens = ducklingTimeParser.parse(("Morgen um 9:00 im ICE 599"), "de", referenceDate);

        assertEquals("Num of Tokens", 2, tokens.size());
        for (DateToken token : tokens) {
            assertThat("Class/Type", token.getStart(), CoreMatchers.instanceOf(Temporal.class));

            final Calendar cal = Calendar.getInstance();
            cal.setTime(token.getStart().getDate());
            if (token.getConfidence() > .5) {
                // A save guess - tomorrow at 9
                assertEquals("+1 day", refereceCal.get(Calendar.YEAR), cal.get(Calendar.YEAR));
                assertEquals("9am", 9, cal.get(Calendar.HOUR_OF_DAY));
                assertEquals("9am", 0, cal.get(Calendar.MINUTE));
            } else {
                // A 'latent' match - 599
                cal.add(Calendar.MONTH, 1); // - try to avoid TimeZone fuck-up
                assertEquals("599 A.D.", 599, cal.get(Calendar.YEAR));
            }

        }
    }

    @Test
    public void testSupportedLanguages() {
        for (String lang : Arrays.asList("en", "de")) {
            assertTrue(lang, ducklingTimeParser.isLanguageSupported(lang));
        }
    }
}