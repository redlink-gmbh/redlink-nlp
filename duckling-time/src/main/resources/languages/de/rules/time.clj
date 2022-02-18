(
  ;; generic

  "intersect"
  [(dim :time #(not (:latent %))) (dim :time #(not (:latent %)))] ; sequence of two tokens with a time dimension
  (intersect %1 %2)

  ; same thing, with "of" in between like "Sunday of last week"
  "intersect by \"of\", \"from\", \"'s\""
  [(dim :time #(not (:latent %))) #"(?i)(in )?de(r|s)" (dim :time #(not (:latent %)))] ; sequence of two tokens with a time fn
  (intersect %1 %3)

  ; mostly for January 12, 2005
  ; this is a separate rule, because commas separate very specific tokens
  ; so we want this rule's classifier to learn this
  "intersect by \",\""
  [(dim :time #(not (:latent %))) #"," (dim :time #(not (:latent %)))] ; sequence of two tokens with a time fn
  (intersect %1 %3)

  "on <date>" ; on Wed, March 23
  [#"(?i)(, )?am|de(r|s|m|n)|," (dim :time)]
  %2 ; does NOT dissoc latent

  "on a named-day" ; on a sunday
  [#"(?i)(an eine(m|n))|am" {:form :day-of-week}]
  %2 ; does NOT dissoc latent


  ;;;;;;;;;;;;;;;;;;;
  ;; Named things

  "named-day"
  #"(?i)Montags?\b|Mon?(\.|\b)"
  (day-of-week 1)

  "named-day"
  #"(?i)Dienstags?\b|Di(\.|\b)|Die\." ; 'Die' requires a '.' as it is also an article in German
  (day-of-week 2)

  "named-day"
  #"Die" ; 'Die' case sensitive to distinquish from 'die' the article
  (day-of-week 2)

  "named-day"
  #"(?i)die" ; 'die' is also an article in German
  (assoc (day-of-week 2) :latent true)

  "named-day"
  #"(?i)Mittwochs?\b|Mi(\.|\b)|Mit\."
  (day-of-week 3)

  "named-day"
  #"Mit" ; 'Mit' case sensitive to distinquish from 'mit' (with) in German
  (day-of-week 3)

  "named-day"
  #"(?i)mit" ; 'mit' (with) in German
  (assoc (day-of-week 3) :latent true)

  "named-day"
  #"(?i)Donnerstags?\b|Don?(\.|\b)"
  (day-of-week 4)

  "named-day"
  #"(?i)Freitags?\b|Fre?i?(\.|\b)"
  (day-of-week 5)

  "named-day"
  #"(?i)Samstags?\b|Sam?(\.|\b)"
  (day-of-week 6)

  "named-day"
  #"(?i)Sonntags?\b|Son?(\.|\b)"
  (day-of-week 7)

  "named-month"
  #"(?i)Jänner\b|Januar\b|J(a|ä)n(\.|\b)"
  (month 1)

  "named-month"
  #"(?i)Februar\b|Feber\b|Feb(\.|\b)"
  (month 2)

  "named-month"
  #"(?i)März\b|Mär(\.|\b)"
  (month 3)

  "named-month"
  #"(?i)April\b|Apr(\.|\b)"
  (month 4)

  "named-month"
  #"(?i)Mai(\.|\b)"
  (month 5)

  "named-month"
  #"(?i)Juni\b|Jun(\.|\b)"
  (month 6)

  "named-month"
  #"(?i)Juli\b|Jul(\.|\b)"
  (month 7)

  "named-month"
  #"(?i)August\b|Aug(\.|\b)"
  (month 8)

  "named-month"
  #"(?i)September\b|Sept?(\.|\b)"
  (month 9)

  "named-month"
  #"(?i)Oktober\b|Okt(\.|\b)"
  (month 10)

  "named-month"
  #"(?i)November\b|Nov(\.|\b)"
  (month 11)

  "named-month"
  #"(?i)Dezember\b|Dez(\.|\b)"
  (month 12)

  ; Holiday TODO: check online holidays
  ; or define dynamic rule (last thursday of october..)

  "christmas"
  #"(?i)(Weihnacht(en)?|Weihnachts?tag|xmas|christmas( day)?)\b"
  (month-day 12 25)

  "christmas eve"
  #"(?i)Heiliger Abend"
  (month-day 12 24)

  "new year's eve"
  #"(?i)Silvester(abend)?\b|Altjahre?s?abend\b"
  (month-day 12 31)

  "new year's day"
  #"(?i)Neujahrs? ?tag"
  (month-day 1 1)
  
  "Heilige Drei Könige"
  #"(?i)Heilige? Drei Könige?"
  (month-day 1 6)

  "valentine's day"
  #"(?i)Valentinstag"
  (month-day 2 14)

  "memorial day" ;the last Monday of May
  #"(?i)Volkstrauertag"
  (pred-last-of (day-of-week 1) (month 5))

  "memorial day weekend" ;the weekend leading to the last Monday of May
  #"(?i)Volkstrauertag Wochenende"
  (interval (intersect (cycle-nth-after :day -3 (pred-last-of (day-of-week 1) (month 5))) (hour 18 false))
            (intersect (pred-last-of (day-of-week 2) (month 5)) (hour 0 false)) ;need to use Tuesday to include monday
            false)

  "independence day"
  #"(?i)Tag der Unabhängigkeit|Unabhängigkeitstag"
  (month-day 7 4)

  "labor day" ;NOTE: America: first Monday in September | Europe: 1st May
  #"(?i)Tag der Arbeit"
  (month-day 5 1)

  "Tag der deutschen Einheit"
  #"(?i)Tag der deutschen Einheit"
  (month-day 10 3)
  

;  "Father's Day" ;TODO: In Österreich - 2nd Sunday of June | in Deutschland zu Christi Himmelfahrt
;  #"(?i)Vatertag"
;  (intersect (day-of-week 7) (month 6) (cycle-nth-after :week 2 (month-day 6 1)))

  "Mother's Day" ;second Sunday in May.
  #"(?i)Muttertag"
  (intersect (day-of-week 7) (month 5) (cycle-nth-after :week 1 (month-day 5 1)))

  "halloween day"
  #"(?i)Halloween"
  (month-day 10 31)

  "thanksgiving day" ; fourth Thursday of November
  #"(?i)Erntedankfest|Thanksgiving"
  (intersect (day-of-week 4) (month 11) (cycle-nth-after :week 4 (month-day 11 1)))

  "black friday"; (the fourth Friday of November),
  #"(?i)Schwarzer Freitag|black frid?day"
  (intersect (day-of-week 5) (month 11) (cycle-nth-after :week 4 (month-day 11 1)))

  "absorption of , after named day"
  [{:form :day-of-week} #","]
  %1

  "now"
  #"(?i)((jetzt)? gleich|nächster?|sofort|jetzt)\b"
  (cycle-nth :second 0)

  "today"
  #"(?i)heute\b"
  (cycle-nth :day 0)

  "morgen"
  #"(?i)morgen\b"
  (cycle-nth :day 1)

  "übermorgen"
  #"(?i)übermorgen\b"
  (cycle-nth :day 2)

  "yesterday"
  #"(?i)gestern\b"
  (cycle-nth :day -1)

  "vorgestern"
  #"(?i)vorgestern\b"
  (cycle-nth :day -2)

  "EOM|End of month"
  #"(?i)(the )?(EOM|(bis )?(zum )?Ende des Monats?)" ; TO BE IMPROVED
  (cycle-nth :month 1)

  "heuer"
  #"(?i)heuer\b"
  (cycle-nth :year 0)

  "EOY|End of year"
  #"(?i)EOY|((bis )?(zum )?Ende des Jahr(es)?)"
  (cycle-nth :year 1)

  ;;
  ;; This, Next, Last

  ;; In German
  ;; "kommenden" is the next <day-of-week> strictly in the future
  ;; "diesen" is the <day-of-week> in the current week (past and future)
  ;; "nächsten" is the <day-of-week> in the week following the current one
  
  ;; instead of "(am )?kommenden" one often just says "am <day-of-week>"
  "kommenden <day-of-week>"
  [#"(?i)am|(am )?kommende(r|n|m)" {:form :day-of-week}]
  (pred-nth-not-immediate %2 0)

  "diesen <day-of-week>"
  [#"(?i)(an )?diese(s|r|n|m)?" {:form :day-of-week}]
  (intersect %2 (cycle-nth :week 0))

  "nächsten <day-of-week>"
  [#"(?i)(am )?nächste(r|n|m)" {:form :day-of-week}]
  (intersect %2 (cycle-nth :week 1))

  ;; for other preds, it can be immediate:
  ;; "this month" => now is part of it
  ; See also: cycles in en.cycles.clj
  "this <time>"
  [#"(?i)diese(s|r|n|m)?|am|für(s| de(n|m)| das)?" (dim :time #(not (:latent %)))]
  (pred-nth %2 0)

  "this <time>"
  [#"(?i)(am )?kommende(r|n|m)?" (dim :time #(not (:latent %)))]
  (pred-nth-not-immediate %2 0)

  "next <time>"
  [#"(?i)(am )?nächste(r|n|m)?" (dim :time)]
  (pred-nth %2 1)

  "next <time>"
  [#"(?i)(am )?nächste(r|n|m)?" (dim :time #(not (:latent %)))]
  (pred-nth-not-immediate %2 1)

  "last <time>"
  [#"(?i)letzt?e(s|r|n|m)?" (dim :time)]
  (pred-nth %2 -1)

  "<time> after next"
  [#"(?i)über ?nächste(r|n|m)?" (dim :time)]
  (pred-nth-not-immediate %2 1)

  "<day-of-week> after next"
  [#"(?i)über ?nächste(r|n|m)" {:form :day-of-week}]
  (intersect %2 (cycle-nth :week 2))

   "<time> before last"
  [#"(?i)vor ?letzt?e(s|r|n|m)?" (dim :time)]
  (pred-nth %2 -2)

  "<day-of-week> before next"
  [#"(?i)vor ?letzt?e(s|r|n|m)?" {:form :day-of-week}]
  (pred-nth-not-immediate %2 -2)

  "last <day-of-week> of <time>"
  [#"(?i)(am )?letzt?e(s|r|n|m)?" {:form :day-of-week} #"(?i)de(s|r)|i(m|n)" (dim :time)]
  (pred-last-of %2 %4)

  "last <cycle> of <time>"
  [#"(?i)letzt?e(s|r|n|m)?" (dim :cycle) #"(?i)(in )?de(s|r)|i(m|n)" (dim :time)]
  (cycle-last-of %2 %4)

  ; Ordinals
  "nth <time> of <time>"
  [(dim :ordinal) (dim :time) #"(?i)des|im" (dim :time)]
  (pred-nth (intersect %4 %2) (dec (:value %1)))

  "nth <time> of <time>"
  [#"(?i)der" (dim :ordinal) (dim :time) #"(?i)des|im" (dim :time)]
  (pred-nth (intersect %5 %3) (dec (:value %2)))

  "nth <time> after <time>"
  [(dim :ordinal) (dim :time) #"(?i)nach( diese(m|n|r))?" (dim :time)]
  (pred-nth-after %2 %4 (dec (:value %1)))

  "nth <time> after <time>"
  [#"(?i)der" (dim :ordinal) (dim :time) #"(?i)nach( diese(m|n|r))?" (dim :time)]
  (pred-nth-after %3 %5 (dec (:value %2)))

    ; Years
  ; Between 1000 and 2100 we assume it's a year
  ; Outside of this, it's safer to consider it's latent

  "year"
  (integer 1000 2100)
  (year (:value %1))

  "year (latent)"
  (integer -10000 999)
  (assoc (year (:value %1)) :latent true)

  "year (latent)"
  (integer 2101 10000)
  (assoc (year (:value %1)) :latent true)

    ; Day of month appears in the following context:
  ; - the nth
  ; - March nth
  ; - nth of March
  ; - mm/dd (and other numerical formats like yyyy-mm-dd etc.)
  ; In general we are flexible and accept both ordinals (3rd) and numbers (3)

  "the <day-of-month> (ordinal)" ; this one is not latent
  [#"(?i)der" (dim :ordinal #(<= 1 (:value %) 31))]
  (day-of-month (:value %2))

  "<day-of-month> (ordinal)" ; this one is latent
  [(dim :ordinal #(<= 1 (:value %) 31))]
  (assoc (day-of-month (:value %1)) :latent true)

  "der <day-of-month> (non ordinal)" ; this one is latent
  [#"(?i)the" (integer 1 31)]
  (assoc (day-of-month (:value %2)) :latent true)

  "<named-month> <day-of-month> (ordinal)" ; march 12th
  [{:form :month} (dim :ordinal #(<= 1 (:value %) 31))]
  (intersect %1 (day-of-month (:value %2)))

  "<named-month> <day-of-month> (non ordinal)" ; Mai 12
  [{:form :month} (integer 1 31)]
  (intersect %1 (day-of-month (:value %2)))

  "<day-of-month> (non ordinal) <named-month>" ; 12 mars
  [(integer 1 31) {:form :month}]
  (intersect %2 (day-of-month (:value %1)))

  "<day-of-month>(ordinal) <named-month>" ; 12nd mars
  [(dim :ordinal #(<= 1 (:value %) 31)) {:form :month}]
  (intersect %2 (day-of-month (:value %1)))

  "<day-of-month>(ordinal) <named-month> year" ; 12nd mars 12
  [(dim :ordinal #(<= 1 (:value %) 31)) {:form :month} #"(\d{2,4})"]
  (intersect %2 (day-of-month (:value %1)) (year (Integer/parseInt(first (:groups %3)))))

  "the ides of <named-month>" ; the ides of march 13th for most months, but on the 15th for March, May, July, and October
  [#"(?i)die Iden? des" {:form :month}]
  (intersect %2 (day-of-month (if (#{3 5 7 10} (:month %2)) 15 13)))

  ;; Hours and minutes (absolute time)

  "time-of-day (latent)"
  (integer 0 23)
  (assoc (hour (:value %1) true) :latent true)

  "at <time-of-day>" ; at four
  [#"(?i)um|@" {:form :time-of-day}]
  (dissoc %2 :latent)


  "<time-of-day> o'clock"
  [{:form :time-of-day} #"(?i)Uhr\b|h(\.|\b)"]
  (dissoc %1 :latent)

  "at <time-of-day> o'clock"
  [#"(?i)um|@" {:form :time-of-day} #"(?i)Uhr\b|h(\.|\b)"]
  (dissoc %2 :latent)

  "hh:mm (12h)"
  #"(?i)\b((?:1[012]|0?\d))[:.]([0-5]\d)\b"
  (hour-minute (Integer/parseInt (first (:groups %1)))
               (Integer/parseInt (second (:groups %1)))
               true)

  "hh:mm (24h)"
  #"(?i)\b((?:[01]?\d)|(?:2[0-3]))[:.]([0-5]\d)\b"
  (hour-minute (Integer/parseInt (first (:groups %1)))
               (Integer/parseInt (second (:groups %1)))
               false)

  "hh.mmh"
  #"(?i)\b((?:[01]?\d)|(?:2[0-3]))[,.]([0-5]\d)(?:Stunden?\b|std?(?:\.|\b)|h(?:\.|\b))"
  (hour-minute (Integer/parseInt (first (:groups %1)))
               (Integer/parseInt (second (:groups %1)))
               false)

  "hh:mm:ss (12h)"
  #"(?i)\b((?:1[012]|0?\d))[:.]([0-5]\d)[:.]([0-5]\d)\b"
  (hour-minute-second (Integer/parseInt (first (:groups %1)))
               (Integer/parseInt (second (:groups %1)))
               (Integer/parseInt (second (next (:groups %1))))
               true)

  "hh:mm:ss (24h)"
  #"(?i)\b((?:[01]?\d)|(?:2[0-3]))[:.]([0-5]\d)[:.]([0-5]\d)\b"
  (hour-minute-second (Integer/parseInt (first (:groups %1)))
               (Integer/parseInt (second (:groups %1)))
               (Integer/parseInt (second (next (:groups %1))))
               false)


  "hhmm (military)"
  #"(?i)\b((?:[01]?\d)|(?:2[0-3]))([0-5]\d)\b"
  (-> (hour-minute (Integer/parseInt (first (:groups %1)))
                   (Integer/parseInt (second (:groups %1)))
                   false) ; not a 12-hour clock)
      (assoc :latent true))

  "hhmm (military) am|pm" ; hh only from 00 to 12
  [#"(?i)\b((?:1[012]|0?\d))([0-5]\d)" #"(?i)([ap])\.?m?(\.|\b)"]
  ; (-> (hour-minute (Integer/parseInt (first (:groups %1)))
  ;                  (Integer/parseInt (second (:groups %1)))
  ;                  false) ; not a 12-hour clock)
  ;     (assoc :latent true))
  (let [[p meridiem] (if (= "a" (-> %2 :groups first .toLowerCase))
                       [[(hour 0) (hour 12) false] :am]
                       [[(hour 12) (hour 0) false] :pm])]
    (-> (intersect
          (hour-minute (Integer/parseInt (first (:groups %1)))
                       (Integer/parseInt (second (:groups %1)))
                   true)
          (apply interval p))
        (assoc :form :time-of-day)))

  "<time-of-day> am|pm"
  [{:form :time-of-day} #"(?i)(in the )?([ap])(\s|\.)?m?(\.|\b)"]
  ;; TODO set_am fn in helpers => add :ampm field
  (let [[p meridiem] (if (= "a" (-> %2 :groups second .toLowerCase))
                       [[(hour 0) (hour 12) false] :am]
                       [[(hour 12) (hour 0) false] :pm])]
    (-> (intersect %1 (apply interval p))
        (assoc :form :time-of-day)))

  "noon"
  #"(?i)(zu )?Mittags?\b"
  (hour 12 false)

  "midnight|EOD|end of day"
  #"(?i)((zu|um) )?Mitt(er)?nacht\b|EOD\b"
  (hour 0 false)


  "number (as relative minutes)"
  (integer 1 59)
  {:relative-minutes (:value %1)}

  "number (as relative minutes)"
  [(integer 1 59) #"(?i)Minuten|min(\.|\b)|m(\.|\b)"]
  {:relative-minutes (:value %1)}

  "<hour-of-day> <integer> (as relative minutes)"
  [(dim :time :full-hour) #(:relative-minutes %)]
  (hour-relativemin (:full-hour %1) (:relative-minutes %2) true)

  "relative minutes to|till|before <integer> (hour-of-day)"
  [#(:relative-minutes %) #"(?i)bis|vor" (dim :time :full-hour)]
  (hour-relativemin (:full-hour %3) (- (:relative-minutes %1)) true)

  "1/4 to|till|before (hour-of-day)"
  [#"(?i)viertel (bis|vor)" (dim :time :full-hour)]
  (hour-relativemin (:full-hour %2) -15 true)

  "1/4 (hour-of-day)"
  [#"(?i)viertel" (dim :time :full-hour)]
  (hour-relativemin (:full-hour %2) -45 true)
  
  "1/2 (hour-of-day)"
  [#"(?i)halb" (dim :time :full-hour)]
  (hour-relativemin (:full-hour %2) -30 true)

  "3/4 (hour-of-day)"
  [#"(?i)drei ?viertel" (dim :time :full-hour)]
  (hour-relativemin (:full-hour %2) -15 true)

  "relative minutes after|past <integer> (hour-of-day)"
  [#(:relative-minutes %) #"(?i)nach|über" (dim :time :full-hour)]
  (hour-relativemin (:full-hour %3) (:relative-minutes %1) true)

  "1/4 after|past (hour-of-day)"
  [#"(?i)viertel (nach|über)" (dim :time :full-hour)]
  (hour-relativemin (:full-hour %2) 15 true)

  ; Formatted dates and times

  "dd/mm/yyyy"
  #"(3[01]|[12]\d|0?[1-9])[\./-](1[0-2]|0?[1-9])[\./-](\d{2,4})"
  (parse-dmy (first (:groups %1)) (second (:groups %1)) (nth (:groups %1) 2) true)

  "yyyy-mm-dd"
  #"(\d{4})[\./-](1[0-2]|0?[1-9])[\./-](3[01]|[12]\d|0?[1-9])"
  (parse-dmy (nth (:groups %1) 2) (second (:groups %1)) (first (:groups %1)) true)

  "dd/mm"
  #"(3[01]|[12]\d|0?[1-9])[\./-](1[0-2]|0?[1-9])"
  (parse-dmy (first (:groups %1)) (second (:groups %1)) nil true)


  ; Part of day (morning, evening...). They are intervals.

  "in der Frühe" ;;
  [#"(?i)Frühe?\b"]
  (assoc (interval (hour 1 false) (hour 5 false) false) :form :part-of-day :latent true)

  "früh morgens" ;; early in the morning
  [#"(?i)früh (am )?morgens?\b"]
  (assoc (interval (hour 4 false) (hour 8 false) false) :form :part-of-day :latent true)

  ; NOTE: in German "morgen" is tomorrow; "morgens" or "am morgen" mean morning
  "Morgens" ;; TODO "3am this morning" won't work since morning starts at 4...
  [#"(?i)Morgens\b"]
  (assoc (interval (hour 5 false) (hour 12 false) false) :form :part-of-day :latent true)

  "am Morgen" ;; TODO "3am this morning" won't work since morning starts at 4...
  [#"(?i)am Morgen\b"]
  (assoc (interval (hour 5 false) (hour 12 false) false) :form :part-of-day :latent true)

  "before noon"
  [#"(?i)Vormittags?\b"]
  (assoc (interval (hour 8 false) (hour 12 false) false) :form :part-of-day :latent true)

  "afternoon"
  [#"(?i)Nach ?Mittags?\b"]
  (assoc (interval (hour 12 false) (hour 19 false) false) :form :part-of-day :latent true)

  "evening"
  [#"(?i)Abends?\b"]
  (assoc (interval (hour 17 false) (hour 22 false) false) :form :part-of-day :latent true)

  "night"
  [#"(?i)Nacht\b|nächtens?\b"] ; TODO: I would like to define this until 4 in the morning (of the next day)
  (assoc (interval (hour 20 false) (hour 0 false) false) :form :part-of-day :latent true)

  "lunch"
  [#"(?i)((um die )|zur )?Mittagszeit\b|(zum )?Mittags?essen\b"]
  (assoc (interval (hour 12 false) (hour 14 false) false) :form :part-of-day :latent true)

  ; NOTE: in German one can have week days and part of day 
  "Montagmorgen"
  #"(?i)Montagmorgens?"
  (assoc (intersect (day-of-week 1) (interval (hour 5 false) (hour 12 false) false)) :form :part-of-day)

  "Dienstagmorgen"
  #"(?i)Dienstagmorgens?"
  (assoc (intersect (day-of-week 2) (interval (hour 5 false) (hour 12 false) false)) :form :part-of-day)

  "Mittwochmorgen"
  #"(?i)Mittwochmorgens?"
  (assoc (intersect (day-of-week 3) (interval (hour 5 false) (hour 12 false) false)) :form :part-of-day)

  "Donnerstagmorgen"
  #"(?i)Donnerstagmorgens?"
  (assoc (intersect (day-of-week 4) (interval (hour 5 false) (hour 12 false) false)) :form :part-of-day)

  "Freitagmorgen"
  #"(?i)Freitagmorgens?"
  (assoc (intersect (day-of-week 5) (interval (hour 5 false) (hour 12 false) false)) :form :part-of-day)

  "Samstagmorgen"
  #"(?i)Samstagmorgens?"
  (assoc (intersect (day-of-week 6) (interval (hour 5 false) (hour 12 false) false)) :form :part-of-day)

  "Sonntagmorgen"
  #"(?i)Sonntagmorgens?"
  (assoc (intersect (day-of-week 7) (interval (hour 5 false) (hour 12 false) false)) :form :part-of-day)
  
  "Montagvormittag"
  #"(?i)Montagvormittags?"
  (assoc (intersect (day-of-week 1) (interval (hour 8 false) (hour 12 false) false)) :form :part-of-day)

  "Dienstagvormittag"
  #"(?i)Dienstagvormittags?"
  (assoc (intersect (day-of-week 2) (interval (hour 8 false) (hour 12 false) false)) :form :part-of-day)

  "Mittwochvormittag"
  #"(?i)Mittwochvormittags?"
  (assoc (intersect (day-of-week 3) (interval (hour 8 false) (hour 12 false) false)) :form :part-of-day)

  "Donnerstagvormittag"
  #"(?i)Donnerstagvormittags?"
  (assoc (intersect (day-of-week 4) (interval (hour 8 false) (hour 12 false) false)) :form :part-of-day)

  "Freitagvormittag"
  #"(?i)Freitagvormittags?"
  (assoc (intersect (day-of-week 5) (interval (hour 8 false) (hour 12 false) false)) :form :part-of-day)

  "Samstagvormittag"
  #"(?i)Samstagvormittags?"
  (assoc (intersect (day-of-week 6) (interval (hour 8 false) (hour 12 false) false)) :form :part-of-day)

  "Sonntagvormittag"
  #"(?i)Sonntagvormittags?"
  (assoc (intersect (day-of-week 7) (interval (hour 8 false) (hour 12 false) false)) :form :part-of-day)
  
  "Montagnachmittag"
  #"(?i)Montagnachmittags?"
  (assoc (intersect (day-of-week 1) (interval (hour 12 false) (hour 19 false) false)) :form :part-of-day)

  "Dienstagnachmittag"
  #"(?i)Dienstagnachmittags?"
  (assoc (intersect (day-of-week 2) (interval (hour 12 false) (hour 19 false) false)) :form :part-of-day)

  "Mittwochnachmittag"
  #"(?i)Mittwochnachmittags?"
  (assoc (intersect (day-of-week 3) (interval (hour 12 false) (hour 19 false) false)) :form :part-of-day)

  "Donnerstagnachmittag"
  #"(?i)Donnerstagnachmittags?"
  (assoc (intersect (day-of-week 4) (interval (hour 12 false) (hour 19 false) false)) :form :part-of-day)

  "Freitagnachmittag"
  #"(?i)Freitagnachmittags?"
  (assoc (intersect (day-of-week 5) (interval (hour 12 false) (hour 19 false) false)) :form :part-of-day)

  "Samstagnachmittag"
  #"(?i)Samstagnachmittags?"
  (assoc (intersect (day-of-week 6) (interval (hour 12 false) (hour 19 false) false)) :form :part-of-day)

  "Sonntagnachmittag"
  #"(?i)Sonntagnachmittags?"
  (assoc (intersect (day-of-week 7) (interval (hour 12 false) (hour 19 false) false)) :form :part-of-day)

  "Montagabend"
  #"(?i)Montagabends?"
  (assoc (intersect (day-of-week 1) (interval (hour 17 false) (hour 22 false) false)) :form :part-of-day)

  "Dienstagabend"
  #"(?i)Dienstagabends?"
  (assoc (intersect (day-of-week 2) (interval (hour 17 false) (hour 22 false) false)) :form :part-of-day)

  "Mittwochabend"
  #"(?i)Mittwochabends?"
  (assoc (intersect (day-of-week 3) (interval (hour 17 false) (hour 22 false) false)) :form :part-of-day)

  "Donnerstagabend"
  #"(?i)Donnerstagabends?"
  (assoc (intersect (day-of-week 4) (interval (hour 17 false) (hour 22 false) false)) :form :part-of-day)

  "Freitagabend"
  #"(?i)Freitagabends?"
  (assoc (intersect (day-of-week 5) (interval (hour 17 false) (hour 22 false) false)) :form :part-of-day)

  "Samstagabend"
  #"(?i)Samstagabends?"
  (assoc (intersect (day-of-week 6) (interval (hour 17 false) (hour 22 false) false)) :form :part-of-day)

  "Sonntagabend"
  #"(?i)Sonntagabends?"
  (assoc (intersect (day-of-week 7) (interval (hour 17 false) (hour 22 false) false)) :form :part-of-day)

  "Montagnacht"
  #"(?i)Montagn(a|ä)cht(en)?s?"
  (assoc (intersect (day-of-week 1) (interval (hour 20 false) (hour 0 false) false)) :form :part-of-day)

  "Dienstagnacht"
  #"(?i)Dienstagn(a|ä)cht(en)?s?"
  (assoc (intersect (day-of-week 2) (interval (hour 20 false) (hour 0 false) false)) :form :part-of-day)

  "Mittwochnacht"
  #"(?i)Mittwochn(a|ä)cht(en)?s?"
  (assoc (intersect (day-of-week 3) (interval (hour 20 false) (hour 0 false) false)) :form :part-of-day)

  "Donnerstagnacht"
  #"(?i)Donnerstagn(a|ä)cht(en)?s?"
  (assoc (intersect (day-of-week 4) (interval (hour 20 false) (hour 0 false) false)) :form :part-of-day)

  "Freitagnacht"
  #"(?i)Freitagn(a|ä)cht(en)?s?"
  (assoc (intersect (day-of-week 5) (interval (hour 20 false) (hour 0 false) false)) :form :part-of-day)

  "Samstagnacht"
  #"(?i)Samstagn(a|ä)cht(en)?s?"
  (assoc (intersect (day-of-week 6) (interval (hour 20 false) (hour 0 false) false)) :form :part-of-day)

  "Sonntagnacht"
  #"(?i)Sonntagn(a|ä)cht(en)?s?"
  (assoc (intersect (day-of-week 7) (interval (hour 20 false) (hour 0 false) false)) :form :part-of-day)


  "in|during the <part-of-day>" ;; removes latent
  [#"(?i)(heute )?(am|in der|zur|heute)" {:form :part-of-day}]
  (dissoc %2 :latent)

  "this <part-of-day>"
  [#"(?i)diese(s|n|m|r)?" {:form :part-of-day}]
  (assoc (intersect (cycle-nth :day 0) %2) :form :part-of-day) ;; removes :latent

  "tonight" ; Huete Nacht|Abend ist schon abgedeckt, aber tonight könnte auch in Deutsch verwendet werden
  #"(?i)toni(ght|gth|te)\b"
  (assoc (intersect (cycle-nth :day 0)
                    (interval (hour 18 false) (hour 0 false) false))
         :form :part-of-day) ; no :latent

  "after work"
  #"(?i)nach der Arbeit\b"
  (assoc (intersect (cycle-nth :day 0)
                    (interval (hour 17 false) (hour 21 false) false))
         :form :part-of-day) ; no :latent

  "<time> <part-of-day>" ; since "morning" "evening" etc. are latent, general time+time is blocked
  [(dim :time) {:form :part-of-day}]
  (intersect %2 %1)
  
  "<time>, <part-of-day>"
  [(dim :time #(not (:latent %))) #"(?i)," {:form :part-of-day}]
  (intersect %3 %1)
  

  "<part-of-day> of <time>" ; since "morning" "evening" etc. are latent, general time+time is blocked
  [{:form :part-of-day} #"(?i)um" (dim :time)]
  (intersect %1 %3)


  ; Other intervals: week-end, seasons

  "week-end" ; from Friday 6pm to Sunday midnight
  #"(?i)(Wochenende\b|WE\b)"
  (interval (intersect (day-of-week 5) (hour 18 false))
            (intersect (day-of-week 7) (hour 22 false))
            false)

  "season"
  #"(?i)Sommer\b" ;could be smarter and take the exact hour into account... also some years the day can change
  (interval (month-day 6 21) (month-day 9 23) false)

  "season"
  #"(?i)Herbst\b"
  (interval (month-day 9 23) (month-day 12 21) false)

  "season"
  #"(?i)Winter\b"
  (interval (month-day 12 21) (month-day 3 20) false)

  "season"
  #"(?i)Früh(ling|jahr)\b"
  (interval (month-day 3 20) (month-day 6 21) false)


  ; Time zones

  "timezone"
  #"(?i)(YEKT|YEKST|YAPT|YAKT|YAKST|WT|WST|WITA|WIT|WIB|WGT|WGST|WFT|WEZ|WET|WESZ|WEST|WAT|WAST|VUT|VLAT|VLAST|VET|UZT|UYT|UYST|UTC|ULAT|TVT|TMT|TLT|TKT|TJT|TFT|TAHT|SST|SRT|SGT|SCT|SBT|SAST|SAMT|RET|PYT|PYST|PWT|PT|PST|PONT|PMST|PMDT|PKT|PHT|PHOT|PGT|PETT|PETST|PET|PDT|OMST|OMSST|NZST|NZDT|NUT|NST|NPT|NOVT|NOVST|NFT|NDT|NCT|MYT|MVT|MUT|MST|MSK|MSD|MMT|MHT|MEZ|MESZ|MDT|MAWT|MART|MAGT|MAGST|LINT|LHST|LHDT|KUYT|KST|KRAT|KRAST|KGT|JST|IST|IRST|IRKT|IRKST|IRDT|IOT|IDT|ICT|HOVT|HNY|HNT|HNR|HNP|HNE|HNC|HNA|HLV|HKT|HAY|HAT|HAST|HAR|HAP|HAE|HADT|HAC|HAA|GYT|GST|GMT|GILT|GFT|GET|GAMT|GALT|FNT|FKT|FKST|FJT|FJST|ET|EST|EGT|EGST|EET|EEST|EDT|ECT|EAT|EAST|EASST|DAVT|ChST|CXT|CVT|CST|COT|CLT|CLST|CKT|CHAST|CHADT|CET|CEST|CDT|CCT|CAT|CAST|BTT|BST|BRT|BRST|BOT|BNT|AZT|AZST|AZOT|AZOST|AWST|AWDT|AST|ART|AQTT|ANAT|ANAST|AMT|AMST|ALMT|AKST|AKDT|AFT|AEST|AEDT|ADT|ACST|ACDT)"
  {:dim :timezone
   :value (-> %1 :groups first .toUpperCase)}

  "<time> timezone"
  [(dim :time) (dim :timezone)]
  (set-timezone %1 (:value %2))


  ; Precision
  ; FIXME
  ; - should be applied to all dims not just time-of-day
  ;-  shouldn't remove latency, except maybe -ish

  "<time-of-day> approximately" ; 7ish
  [{:form :time-of-day} #"(?i)(-?isch)\b"]
  (-> %1
    (dissoc :latent)
    (merge {:precision "approximate"}))

  "<time-of-day> sharp" ; sharp
  [{:form :time-of-day} #"(?i)(pünktlich|exakt)\b"]
  (-> %1
    (dissoc :latent)
    (merge {:precision "exact"}))

  "about <time-of-day>" ; about
  [#"(?i)(~|(cir)?ca\.?( um)?|gegen|ungefähr|etwa( um| gegen)?|so ( um| gegen)?)" {:form :time-of-day}]
  (-> %2
    (dissoc :latent)
    (merge {:precision "approximate"}))

  "exactly <time-of-day>" ; sharp
  [#"(?i)(exakt|pünktlich|genau)( um)?" {:form :time-of-day} ]
  (-> %2
    (dissoc :latent)
    (merge {:precision "exact"}))


  ; Intervals

  "<month> dd-dd (interval)"
  [{:form :month} #"(3[01]|[12]\d|0?[1-9])\.?" #"\-|bis( zum)?|(und|aber)( vor( de(m|n))?)?" #"(3[01]|[12]\d|0?[1-9])(\.|\b)"]
  (interval (intersect %1 (day-of-month (Integer/parseInt (-> %2 :groups first))))
            (intersect %1 (day-of-month (Integer/parseInt (-> %4 :groups first))))
            true)

  "from <month> dd-dd (interval)"
  [#"(?i)vo(n|m)|(ab|zwischen|nach)( de(m|n))?" {:form :month} #"(3[01]|[12]\d|0?[1-9])\.?" #"\-|bis( zum)?|(und|aber)( vor( de(m|n))?)?" #"(3[01]|[12]\d|0?[1-9])(\.|\b)"]
  (interval (intersect %2 (day-of-month (Integer/parseInt (-> %3 :groups first))))
            (intersect %2 (day-of-month (Integer/parseInt (-> %5 :groups first))))
            true)

  "dd-dd <month> (interval)"
  [#"(3[01]|[12]\d|0?[1-9])\.?" #"\-|bis( zum)?|(und|aber)( vor( de(m|n))?)?" #"(3[01]|[12]\d|0?[1-9])\.?" {:form :month}]
  (interval (intersect %4 (day-of-month (Integer/parseInt (-> %1 :groups first))))
            (intersect %4 (day-of-month (Integer/parseInt (-> %3 :groups first))))
            true)

  "from dd-dd <month> (interval)"
  [#"(?i)vo(n|m)|(ab|zwischen|nach)( de(m|n))?" #"(3[01]|[12]\d|0?[1-9])\.?" #"\-|bis( zum)?|(und|aber)( vor( de(m|n))?)?" #"(3[01]|[12]\d|0?[1-9])\.?" {:form :month}]
  (interval (intersect %5 (day-of-month (Integer/parseInt (-> %2 :groups first))))
            (intersect %5 (day-of-month (Integer/parseInt (-> %4 :groups first))))
            true)

  "dd-dd.mm (interval)"
  [#"(3[01]|[12]\d|0?[1-9])\.?" #"\-|bis( zum)?|(und|aber)( vor( de(m|n))?)?" #"(3[01]|[12]\d|0?[1-9])(?:\.| ) ?(1[0-2]|0?[1-9])(\.|\b)"]
  (interval (intersect (month (Integer/parseInt (-> %3 :groups second))) (day-of-month (Integer/parseInt (-> %1 :groups first))))
            (intersect (month (Integer/parseInt (-> %3 :groups second))) (day-of-month (Integer/parseInt (-> %3 :groups first))))
            true)

  "from dd-dd.mm (interval)"
  [#"(?i)vo(n|m)|(ab|zwischen|nach)( de(m|n))?" #"(3[01]|[12]\d|0?[1-9])\.?" #"\-|bis( zum)?|(und|aber)( vor( de(m|n))?)?" #"(3[01]|[12]\d|0?[1-9])(?:\.| ) ?(1[0-2]|0?[1-9])(\.|\b)"]
  (interval (intersect (month (Integer/parseInt (-> %4 :groups second))) (day-of-month (Integer/parseInt (-> %2 :groups first))))
            (intersect (month (Integer/parseInt (-> %4 :groups second))) (day-of-month (Integer/parseInt (-> %4 :groups first))))
            true)

  "dd.mm-dd.mm (interval)"
  [#"(3[01]|[12]\d|0?[1-9])(?:\.| ) ?(1[0-2]|0?[1-9])\.?" #"\-|bis( zum)?|(und|aber)( vor( de(m|n))?)?" #"(3[01]|[12]\d|0?[1-9])(?:\.| ) ?(1[0-2]|0?[1-9])(\.|\b)"]
  (interval (intersect (month (Integer/parseInt (-> %1 :groups second))) (day-of-month (Integer/parseInt (-> %1 :groups first))))
            (intersect (month (Integer/parseInt (-> %3 :groups second))) (day-of-month (Integer/parseInt (-> %3 :groups first))))
            true)

  "from dd.mm-dd.mm (interval)"
  [#"(?i)vo(n|m)|(ab|zwischen|nach)( de(m|n))?" #"(3[01]|[12]\d|0?[1-9])(?:\.| ) ?(1[0-2]|0?[1-9])\.?" #"\-|bis( zum)?|(und|aber)( vor( de(m|n))?)?" #"(3[01]|[12]\d|0?[1-9])(?:\.| ) ?(1[0-2]|0?[1-9])(\.|\b)"]
  (interval (intersect (month (Integer/parseInt (-> %2 :groups second))) (day-of-month (Integer/parseInt (-> %2 :groups first))))
            (intersect (month (Integer/parseInt (-> %4 :groups second))) (day-of-month (Integer/parseInt (-> %4 :groups first))))
            true)

  "dd-dd.mm.yyyy (interval)"
  [#"(3[01]|[12]\d|0?[1-9])\.?" #"\-|bis( zum)?|(und|aber)( vor( de(m|n))?)?" #"(3[01]|[12]\d|0?[1-9])(?:\.| ) ?(1[0-2]|0?[1-9])(?:\.| ) ?(\d{2,4})(\.|\b)"]
  (interval (intersect (year (Integer/parseInt (-> (nth (:groups %3) 2)))) (month (Integer/parseInt (-> %3 :groups second))) (day-of-month (Integer/parseInt (-> %1 :groups first))))
            (intersect (year (Integer/parseInt (-> (nth (:groups %3) 2)))) (month (Integer/parseInt (-> %3 :groups second))) (day-of-month (Integer/parseInt (-> %3 :groups first))))
            true)

  "from dd-dd.mm.yyyy (interval)"
  [#"(?i)vo(n|m)|(ab|zwischen|nach)( de(m|n))?" #"(3[01]|[12]\d|0?[1-9])\.?" #"\-|bis( zum)?|(und|aber)( vor( de(m|n))?)?" #"(3[01]|[12]\d|0?[1-9])(?:\.| ) ?(1[0-2]|0?[1-9])(?:\.| ) ?(\d{2,4})(\.|\b)"]
  (interval (intersect (year (Integer/parseInt (-> (nth (:groups %4) 2)))) (month (Integer/parseInt (-> %4 :groups second))) (day-of-month (Integer/parseInt (-> %2 :groups first))))
            (intersect (year (Integer/parseInt (-> (nth (:groups %4) 2)))) (month (Integer/parseInt (-> %4 :groups second))) (day-of-month (Integer/parseInt (-> %4 :groups first))))
            true)

  "dd.mm-dd.mm.yyyy (interval)"
  [#"(3[01]|[12]\d|0?[1-9])(?:\.| ) ?(1[0-2]|0?[1-9])\.?" #"\-|bis( zum)?|(und|aber)( vor( de(m|n))?)?" #"(3[01]|[12]\d|0?[1-9])(?:\.| ) ?(1[0-2]|0?[1-9])(?:\.| ) ?(\d{2,4})(\.|\b)"]
  (interval (intersect (year (Integer/parseInt (-> (nth (:groups %3) 2)))) (month (Integer/parseInt (-> %1 :groups second))) (day-of-month (Integer/parseInt (-> %1 :groups first))))
            (intersect (year (Integer/parseInt (-> (nth (:groups %3) 2)))) (month (Integer/parseInt (-> %3 :groups second))) (day-of-month (Integer/parseInt (-> %3 :groups first))))
            true)

  "from dd.mm-dd.mm.yyyy (interval)"
  [#"(?i)vo(n|m)|(ab|zwischen|nach)( de(m|n))?" #"(3[01]|[12]\d|0?[1-9])(?:\.| ) ?(1[0-2]|0?[1-9])\.?" #"\-|bis( zum)?|(und|aber)( vor( de(m|n))?)?" #"(3[01]|[12]\d|0?[1-9])(?:\.| ) ?(1[0-2]|0?[1-9])(?:\.| ) ?(\d{2,4})(\.|\b)"]
  (interval (intersect (year (Integer/parseInt (-> (nth (:groups %4) 2)))) (month (Integer/parseInt (-> %2 :groups second))) (day-of-month (Integer/parseInt (-> %2 :groups first))))
            (intersect (year (Integer/parseInt (-> (nth (:groups %4) 2)))) (month (Integer/parseInt (-> %4 :groups second))) (day-of-month (Integer/parseInt (-> %4 :groups first))))
            true)

  "dd.mm.yyyy-dd.mm.yyyy (interval)"
  [#"(3[01]|[12]\d|0?[1-9])(?:\.| ) ?(1[0-2]|0?[1-9])(?:\.| ) ?(\d{2,4})\.?" #"\-|bis( zum)?|(und|aber)( vor( de(m|n))?)?" #"(3[01]|[12]\d|0?[1-9])(?:\.| ) ?(1[0-2]|0?[1-9])(?:\.| ) ?(\d{2,4})(\.|\b)"]
  (interval (intersect (year (Integer/parseInt (-> (nth (:groups %1) 2)))) (month (Integer/parseInt (-> %1 :groups second))) (day-of-month (Integer/parseInt (-> %1 :groups first))))
            (intersect (year (Integer/parseInt (-> (nth (:groups %3) 2)))) (month (Integer/parseInt (-> %3 :groups second))) (day-of-month (Integer/parseInt (-> %3 :groups first))))
            true)

  "from dd.mm.yyyy-dd.mm.yyyy (interval)"
  [#"(?i)vo(n|m)|(ab|zwischen|nach)( de(m|n))?" #"(3[01]|[12]\d|0?[1-9])(?:\.| ) ?(1[0-2]|0?[1-9])(?:\.| ) ?(\d{2,4})\.?" #"\-|bis( zum)?|(und|aber)( vor( de(m|n))?)?" #"(3[01]|[12]\d|0?[1-9])(?:\.| ) ?(1[0-2]|0?[1-9])(?:\.| ) ?(\d{2,4})(\.|\b)"]
  (interval (intersect (year (Integer/parseInt (-> (nth (:groups %2) 2)))) (month (Integer/parseInt (-> %2 :groups second))) (day-of-month (Integer/parseInt (-> %2 :groups first))))
            (intersect (year (Integer/parseInt (-> (nth (:groups %4) 2)))) (month (Integer/parseInt (-> %4 :groups second))) (day-of-month (Integer/parseInt (-> %4 :groups first))))
            true)

  ; Blocked for :latent time. May need to accept certain latents only, like hours

  "<datetime> - <datetime> (interval)"
  [(dim :time #(not (:latent %))) #"\-|bis( zum)?" (dim :time #(not (:latent %)))]
  (interval %1 %3 true)

  "from <datetime> - <datetime> (interval)"
  [#"(?i)vo(n|m)|ab( de(m|n))?" (dim :time) #"\-|bis( zum)?" (dim :time)]
  (interval %2 %4 true)

  "between <datetime> and <datetime> (interval)"
  [#"(?i)zwischen" (dim :time) #"und|-|bis" (dim :time)]
  (interval %2 %4 true)

  "after <datetime> and vor <datetime> (interval)"
  [#"(?i)nach( de(n|m))?" (dim :time) #"(und|aber) vor( de(m|n))?" (dim :time)]
  (interval %2 %4 true)

  ; Specific for time-of-day, to help resolve ambiguities

  "<time-of-day> - <time-of-day> (interval)"
  [#(and (= :time-of-day (:form %)) (not (:latent %))) #"\-|:|bis( einschließlich)?" {:form :time-of-day}] ; Prevent set alarm 1 to 5pm
  (interval %1 %3 true)

  "from <time-of-day> - <time-of-day> (interval)"
  [#"(?i)(beginnend )?von" {:form :time-of-day} #"(\-|bis( einschließlich)?)" {:form :time-of-day}]
  (interval %2 %4 true)

  "between <time-of-day> and <time-of-day> (interval)"
  [#"(?i)zwischen" {:form :time-of-day} #"und" {:form :time-of-day}]
  (interval %2 %4 true)

  ; Specific for within duration... Would need to be reworked
  "within <duration>"
  [#"(?i)für|innerhalb|während( de(s|r|m|n))?" (dim :duration)]
  (interval (cycle-nth :second 0) (in-duration (:value %2)) false)

  "bis zum <time>"; if time is interval, take the start of the interval (by tonight = by 6pm)
  [#"(?i)bis( z?u(m|r))?" (dim :time)]
  (interval (cycle-nth :second 0) %2 false)

  "by the end of <time>"; in this case take the end of the time (by the end of next week = by the end of next sunday)
  ; NOTE: noch heute; vor 14:00, vor Ende des Monats, vor dem Ende dieser Woche
  [#"(?i)noch|((noch )?vor (de(m|n) )?|bis (zu(r|m))?)Ende( de(s|r|n|m))?" (dim :time)]
  (interval (cycle-nth :second 0) %2 true)

  ; One-sided Intervals

  "until <time-of-day>"
  [#"(?i)bis|vor( de(m|n))?|spätestens um|nicht später als" (dim :time #(not (:latent %)))]
  (merge %2 {:direction :before})

  "until <time-of-day> o'clock"
  [#"(?i)bis|vor|spätestens um|nicht später als" {:form :time-of-day} #"(?i)Uhr\b|h(\.|\b)"]
  (dissoc (merge %2 {:direction :before}) :latent)

  "after <time-of-day>"
  [#"(?i)nach( de(m|n))?|ab|frühestens um|nicht früher als" (dim :time #(not (:latent %)))]
  (merge %2 {:direction :after})

  "after <time-of-day> o'clock"
  [#"(?i)nach|ab|frühestens um|nicht früher als" {:form :time-of-day}  #"(?i)Uhr\b|h(\.|\b)"]
  (dissoc (merge %2 {:direction :after}) :latent)

  ; ;; In this special case, the upper limit is exclusive
  ; "<hour-of-day> - <hour-of-day> (interval)"
  ; [{:form :time-of-day} #"-|to|th?ru|through|until" #(and (= :time-of-day (:form %))
  ; 									  (not (:latent %)))]
  ; (interval %1 %3 :exclusive)

  ; "from <hour-of-day> - <hour-of-day> (interval)"
  ; [#"(?i)from" {:form :time-of-day} #"-|to|th?ru|through|until" #(and (= :time-of-day (:form %))
  ; 									              (not (:latent %)))]
  ; (interval %2 %4 :exclusive)

  ; "time => time2 (experiment)"
  ; (dim :time)
  ; (assoc %1 :dim :time2)

)
