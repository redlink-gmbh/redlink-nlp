; Cycles are like a heart beat, the next starts just when the previous ends.
; Unlike durations, they have an absolute position in the time, it's just that this position is periodic.
; Examples of phrases involving cycles:
; - this week
; - today (= this day)
; - last month
; - last 2 calendar months (last 2 months is interpreted as a duration)
;
; As soon as you put a quantity (2 months), the cycle becomes a duration.


(
  "second (cycle)"
  #"(?i)Sekunden?\b|se(c|k)(\.|\b)|s(\.|\b)"
  {:dim :cycle
   :grain :second}

  "minute (cycle)"
  #"(?i)Minuten?\b|min(\.|\b)|m(\.|\b)"
  {:dim :cycle
   :grain :minute}

  "hour (cycle)"
  #"(?i)Stunden?\b|std?(\.|\b)|h(\.|\b)"
  {:dim :cycle
   :grain :hour}

  "day (cycle)"
  #"(?i)Tage?n?\b"
  {:dim :cycle
   :grain :day}

  "night (cycle)"
  #"(?i)N(a|ä)chte?\b"
  {:dim :cycle
   :grain :day}

  "week (cycle)"
  #"(?i)Wochen?\b|wo(\.|\b)"
  {:dim :cycle
   :grain :week}

  "month (cycle)"
  #"(?i)Monate?n?\b|mon(\.|\b)"
  {:dim :cycle
   :grain :month}
  
  "quarter (cycle)"
  #"(?i)Quartale?n?\b|qrt(\.|\b)"
  {:dim :cycle
   :grain :quarter}
  
  "year (cycle)"
  #"(?i)Jahre?n?\b|jhr(\.|\b)"
  {:dim :cycle
   :grain :year}
  
  "this <cycle>"
  [#"(?i)diese(r|s|n|m)?|de(r|s) aktuelle(n|m|r|s)" (dim :cycle)]
  (cycle-nth (:grain %2) 0)

  "last <cycle>"
  [#"(?i)letzt?e(r|s|n|m)?|vorherige(r|s|n|m)?" (dim :cycle)]
  (cycle-nth (:grain %2) -1)

  "last one <cycle>"
  [#"(?i)((in(nerhalb)?|während) de(r|s)|im) (letzt?e(n|m)|vorherige(n|m)|vergangenen)" (dim :cycle)]
  (cycle-n-not-immediate (:grain %2) -1)

  "next one <cycle>"
  [#"(?i)((in(nerhalb)?|während) de(r|s)|im) (nächste(n|m)|kommende(n|m)|folgende(n|m))" (dim :cycle)]
  (cycle-n-not-immediate (:grain %2) +1)

 ;; can not be distinguished without seperating singular and plural from cycles 
 ; "last few <cycle>"
 ; [#"(?i)(in(nerhalb)?|während|in) den (letzt?en|vorherigen)" (dim :cycle)]
 ; (cycle-n-not-immediate (:grain %2) -3)

  "next <cycle>"
  [#"(?i)nächste(r|s|n|m)?|kommende(r|s|n|m)?" (dim :cycle)]
  (cycle-nth (:grain %2) 1)
  
  "the <cycle> after <time>"
  [#"(?i)der|die|das" (dim :cycle) #"(?i)nach( de(m|n|s|r)| diese(s|r|n|m))?" (dim :time)]
  (cycle-nth-after (:grain %2) 1 %4)

  "<cycle> after <time>"
  [(dim :cycle) #"(?i)nach( de(m|n|s|r)| diese(s|r|n|m))?" (dim :time)]
  (cycle-nth-after (:grain %1) 1 %3)
  
  "the <cycle> before <time>"
  [#"(?i)der|die|das" (dim :cycle) #"(?i)vor( de(m|n|s|r)| diese(s|r|n|m))?" (dim :time)]
  (cycle-nth-after (:grain %2) -1 %4)
  
  "<cycle> before <time>"
  [(dim :cycle) #"(?i)vor( de(m|n|s|r)| diese(s|r|n|m))?" (dim :time)]
  (cycle-nth-after (:grain %1) -1 %3)

  "last n <cycle>"
  [#"(?i)(((in(nerhalb)?|während) de(r|n)) )?(letzt?e(s|m|n|r)?|vorherige(r|n)?|vergangenen)" (integer 1 9999) (dim :cycle)]
  (cycle-n-not-immediate (:grain %3) (- (:value %2)))
  
  "next n <cycle>"
  [#"(?i)(((in(nerhalb)?|während) de(r|n)) )?(nächsten?|n(ä|a)ch(st)?folgend(en)?|folgenden?|kommenden?)" (integer 1 9999) (dim :cycle)]
  (cycle-n-not-immediate (:grain %3) (:value %2))
  
  "<ordinal> <cycle> of <time>"
  [(dim :ordinal) (dim :cycle) #"(?i)von|i(m|n)" (dim :time)]
  (cycle-nth-after-not-immediate (:grain %2) (dec (:value %1)) %4)
  
  "the <ordinal> <cycle> of <time>"
  [#"(?i)in|der|die|das" (dim :ordinal) (dim :cycle) #"(?i)von|i(m|n)" (dim :time)]
  (cycle-nth-after-not-immediate (:grain %3) (dec (:value %2)) %5)

  "the <cycle> of <time>"
  [#"(?i)der|die|das" (dim :cycle) #"(?i)von|i(m|n)" (dim :time)]
  (cycle-nth-after-not-immediate (:grain %2) 0 %4)

  ; the 2 following rules may need a different helper
  
  "<ordinal> <cycle> after <time>"
  [(dim :ordinal) (dim :cycle) #"(?i)nach( de(m|n|s|r)| diese(s|r|n|m))?" (dim :time)]
  (cycle-nth-after-not-immediate (:grain %2) (dec (:value %1)) %4)
  
  "the <ordinal> <cycle> after <time>"
  [#"(?i)der|die|das" (dim :ordinal) (dim :cycle) #"(?i)nach( de(m|n|s|r)| diese(s|r|n|m))?" (dim :time)]
  (cycle-nth-after-not-immediate (:grain %3) (dec (:value %2)) %5)

  
  ; quarters are a little bit different, you can say "3rd quarter" alone
  
  "<ordinal> quarter"
  [(dim :ordinal) (dim :cycle #(= :quarter (:grain %)))]
  (cycle-nth-after :quarter (dec (:value %1)) (cycle-nth :year 0))

  "<ordinal> quarter <year>"
  [(dim :ordinal) (dim :cycle #(= :quarter (:grain %))) (dim :time)]
  (cycle-nth-after :quarter (dec (:value %1)) %3)
)
