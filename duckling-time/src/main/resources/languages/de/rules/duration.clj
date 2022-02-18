; Durations / Periods

(
  "second (unit-of-duration)"
  #"(?i)Sekunden?\b\b|se(k|c)(\.|\b)|s(\.|\b)"
  {:dim :unit-of-duration
   :grain :second}

  "minute (unit-of-duration)"
  #"(?i)Minuten?\b|min(\.|\b)|m(\.|\b)"
  {:dim :unit-of-duration
   :grain :minute}

  "hour (unit-of-duration)"
  #"(?i)Stunden?\b|std(\.|\b)|h(\.|\b)"
  {:dim :unit-of-duration
   :grain :hour}

  "day (unit-of-duration)"
  #"(?i)Tage?n?\b"
  {:dim :unit-of-duration
   :grain :day}

  "night (unit-of-duration)" ; Hotel rooms are booked for nights and not days
  #"(?i)N(a|ä)chte?\b"
  {:dim :unit-of-duration
   :grain :day}

  "week (unit-of-duration)"
  #"(?i)Wochen?\b|Wo(\.|\b)"
  {:dim :unit-of-duration
   :grain :week}

  "month (unit-of-duration)"
  #"(?i)Monate?n?\b"
  {:dim :unit-of-duration
   :grain :month}
  
  "year (unit-of-duration)"
  #"(?i)Jahre?n?\b|Jr?(\.|\b)"
  {:dim :unit-of-duration
   :grain :year}
  
   "half an hour"
  [#"(?i)(einer )?(1/2|½|halben ) ?Stunde\b|std(\.|\b)|h(\.|\b)"]
  {:dim :duration
   :value (duration :minute 30)}

   "half a minute"
  [#"(?i)(einer )?(1/2|½|halben ) ?Minute\b|min(\.|\b)|m(\.|\b)"]
  {:dim :duration
   :value (duration :second 30)}

   "half a day"
  [#"(?i)(eine(n|m) )?(1/2|½|halben? ) ?Tag\b"]
  {:dim :duration
   :value (duration :hour 12)}

  "<integer> <unit-of-duration>"
  [(integer 0) (dim :unit-of-duration)]; duration can't be negative...
  {:dim :duration
   :value (duration (:grain %2) (:value %1))}
    
;; NOTE: IMHO this phrase is not used in German
;  "<integer> more <unit-of-duration>"
;  [(integer 0) #"(?i)mehr|weniger" (dim :unit-of-duration)]; would need to add fields at some point
;  {:dim :duration
;   :value (duration (:grain %3) (:value %1))}

  ; TODO handle cases where ASR outputs "1. 5 hours"
  ; but allowing a space creates many false positive
  "number.number hours" ; in 1.5 hour but also 1.75
  [#"(\d+)\.(\d+)" #"(?i)Stunden\b|std(\.|\b)|h(\.|\b)"] ;duration can't be negative...
  {:dim :duration
   :value (duration :minute (int (+ (quot (* 6 (Long/parseLong (second (:groups %1)))) (java.lang.Math/pow 10 (- (count (second (:groups %1))) 1))) (* 60 (Long/parseLong (first (:groups %1)))))))}

  "<integer> and an half hours"
  [(integer 0) #"(?i)und einer? halben? Stunde\b|std(\.|\b)|h(\.|\b)"] ;duration can't be negative...
  {:dim :duration
   :value (duration :minute (+ 30 (* 60 (:value %1))))}

  "a <unit-of-duration>"
  [#"(?i)eine(r|n|m)?" (dim :unit-of-duration)]
  {:dim :duration
   :value (duration (:grain %2) 1)}

  "in <duration>"
  [#"(?i)in" (dim :duration)]
  (in-duration (:value %2))

;; NOTE "I missed the train by 5 minutes"
;; but 'um' is more often used with time (um 12:00) 
;; and some might both denote to a duration and a time (e.g. um 12h)
;; so this does more bad as good and is commented for now
;  "um <duration>"
;  [#"(?i)um" (dim :duration)]
;  (assoc (in-duration (:value %2)) :latent true)

;; NOTE: "für <duration>" is in german more like within <duration> -> inverval
;;       so this rule is deactivated and added to the within <duration> rule 
;  "vor <duration>"
;  [#"(?i)für" (dim :duration)]
;  (in-duration (:value %2))

  "after <duration>"
  [#"(?i)nach" (dim :duration)]
  (merge (in-duration (:value %2)) {:direction :after})

  "<duration> from now"
  [(dim :duration) #"(?i)von (heute|jetzt)\b"]
  (in-duration (:value %1))

  "<duration> ago"
  [#"(?i)vor" (dim :duration)]
  (duration-ago (:value %2))
  
  "<duration> hence"
  [(dim :duration) #"(?i)von jetzt( an)?\b"]
  (in-duration (:value %1))
  
  "<duration> after <time>"
  [(dim :duration) #"(?i)nach( de(m|n|s|r)| diese(s|r|n|m))?" (dim :time)]
  (duration-after (:value %1) %3)

  "<duration> before <time>"
  [(dim :duration) #"(?i)vor( de(m|n|s|r)| diese(s|r|n|m))?" (dim :time)]
  (duration-before (:value %1) %3)

  "about <duration>" ; about
  [#"(?i)(~|(cir)?ca\.?( um)?|gegen|ungefähr|etwa( um| gegen)?|so ( um| gegen)?)" (dim :duration)]
  (-> %2
    (merge {:precision "approximate"}))

  "exactly <duration>" ; sharp
  [#"(?i)(exakt|pünktlich|genau)( um)?" (dim :duration)]
  (-> %2
    (merge {:precision "exact"}))

)
