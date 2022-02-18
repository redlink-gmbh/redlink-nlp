(

  "intersect"
  [(dim :number :grain #(> (:grain %) 1)) (dim :number)] ; grain 1 are taken care of by specific rule
  (compose-numbers %1 %2) 

  "intersect (with and)"
  [(dim :number :grain #(> (:grain %) 1)) #"(?i)und" (dim :number)] ; grain 1 are taken care of by specific rule
  (compose-numbers %1 %3) 

 ;;
 ;; Integers
 ;;
 
  "integer (0..19)"
  #"(?i)(keine?|null|zero|eins?|eine|zwei|drei|vierzehn|vier|fünfzehn|fünf|sechzehn|sechs|siebzehn|sieben|acht?zehn|acht|neunzehn|neun|elf|zwölf)"
  ; fourteen must be before four, or it won't work because the regex will stop at four
  {:dim :number
   :integer true
   :value (get {"kein" 0 "keine" 0 "null" 0 "zero" 0 "ein" 1 "eins" 1 "eine" 1 "zwei" 2 "drei" 3 "vier" 4 "fünf" 5
              "sechs" 6 "sieben" 7 "acht" 8 "neun" 9 "zehn" 10 "elf" 11
              "zwölf" 12 "thirteen" 13 "vierzehn" 14 "fünfzehn" 15 "sechzehn" 16
              "siebzehn" 17 "achtzehn" 18 "achzehn" 18 "neunzehn" 19}
              (-> %1 :groups first .toLowerCase))}
  
  "ten"
  #"(?i)zehn"
  {:dim :number :integer true :value 10 :grain 1}

  "single"
  #"(?i)single"
  {:dim :number :integer true :value 1 :grain 1}
  
  "dozen"
  #"(?i)dutzend"
  {:dim :number :integer true :value 12 :grain 1 :grouping true} ;;restrict composition and prevent "2 12"

  "hundred"
  #"(?i)Hunderte?"
  {:dim :number :integer true :value 100 :grain 2}

  "thousand"
  #"(?i)Tausende?"
  {:dim :number :integer true :value 1000 :grain 3}

  "million"
  #"(?i)Millionen?"
  {:dim :number :integer true :value 1000000 :grain 6}

  "ein paar"
  #"(ein )?paar"
  {:dim :number :integer true :precision :approximate :value 2}
  
  "wenigen" ; TODO set assumption
  #"wenigen?"
  {:dim :number :integer true :precision :approximate :value 2}

  "einigen" ; TODO set assumption
  #"einigen?"
  {:dim :number :integer true :precision :approximate :value 3}

  "integer (20..90)"
  #"(?i)(zwanzig|dreißig|dreissig|vierzig|fünfzig|sechzig|siebzig|achtzig|neunzig)"
  {:dim :number
   :integer true
   :value (get {"zwanzig" 20 "dreissig" 30 "dreißig" 30"vierzig" 40 "fünfzig" 50 "sechzig" 60
              "siebzig" 70 "achtzig" 80 "neunzig" 90}
             (-> %1 :groups first .toLowerCase))
   :grain 1}

  "integer 21..99"
  [(integer 1 9) #"(?i)und" (integer 10 90 #(#{20 30 40 50 60 70 80 90} (:value %)))]
  {:dim :number
   :integer true
   :value (+ (:value %3) (:value %1))}

  "integer (numeric)"
  #"(\d{1,18})"
  {:dim :number
   :integer true
   :value (Long/parseLong (first (:groups %1)))}
  
  "integer with thousands separator ."
  #"(\d{1,3}(,\d\d\d){1,5})"
  {:dim :number
   :integer true
   :value (-> (:groups %1)
            first
            (clojure.string/replace #"," "")
            Long/parseLong)}
  
  ; composition
  "special composition for missing hundreds like in one twenty two"
  [(integer 1 9) (integer 10 99)] ; grain 1 are taken care of by specific rule
  {:dim :number
   :integer true
   :value (+ (* (:value %1) 100) (:value %2))
   :grain 1}


  "number dozen"
  [(integer 1 10) (dim :number #(:grouping %))]
  {:dim :number
   :integer true
   :value (* (:value %1) (:value %2))
   :grain (:grain %2)}


  "number hundreds"
  [(integer 1 99) (integer 100 100)]
  {:dim :number
   :integer true
   :value (* (:value %1) (:value %2))
   :grain (:grain %2)}

  "number thousands"
  [(integer 1 999) (integer 1000 1000)]
  {:dim :number
   :integer true
   :value (* (:value %1) (:value %2))
   :grain (:grain %2)}

  "number millions"
  [(integer 1 99) (integer 1000000 1000000)]
  {:dim :number
   :integer true
   :value (* (:value %1) (:value %2))
   :grain (:grain %2)}

  ;;
  ;; Decimals
  ;;
  
  "decimal number"
  #"(\d*\.\d+)"
  {:dim :number
   :value (Double/parseDouble (first (:groups %1)))}


  "number dot number"
  [(dim :number #(not (:number-prefixed %))) #"(?i)Komma|Beistrich|Punkt" (dim :number #(not (:number-suffixed %)))]
  {:dim :number
   :value (+ (* 0.1 (:value %3)) (:value %1))}
   

  "decimal with thousands separator"
  #"(\d+(,\d\d\d)+\.\d+)"
  {:dim :number
   :value (-> (:groups %1)
            first
            (clojure.string/replace #"," "")
            Double/parseDouble)}

  ;; negative number
  "numbers prefix with -, negative or minus"
  [#"(?i)-|minus\s?|negativ\s?" (dim :number #(not (:number-prefixed %)))]
  (let [multiplier -1
        value      (* (:value %2) multiplier)
        int?       (zero? (mod value 1)) ; often true, but we could have 1.1111K
        value      (if int? (long value) value)] ; cleaner if we have the right type
    (assoc %2 :value value
              :integer int?
              :number-prefixed true)) ; prevent "- -3km" to be 3 billions


  ;; suffixes

  ; note that we check for a space-like char after the M, K or G
  ; to avoid matching 3 Mandarins
  "numbers suffixes (K, M, G)"
  [(dim :number #(not (:number-suffixed %))) #"(?i)([kmg])(?=[\W\$€]|$)"]
  (let [multiplier (get {"k" 1000 "m" 1000000 "g" 1000000000}
                        (-> %2 :groups first .toLowerCase))
        value      (* (:value %1) multiplier)
        int?       (zero? (mod value 1)) ; often true, but we could have 1.1111K
        value      (if int? (long value) value)] ; cleaner if we have the right type
    (assoc %1 :value value
              :integer int?
              :number-suffixed true)) ; prevent "3km" to be 3 billions

  ;;
  ;; Ordinal numbers
  ;;
  
  "ordinals (first..31st)"
  #"(?i)(erste(r|s|n|m)?|zweite(r|s|n|m)?|dritte(r|s|n|m)?|vierte(r|s|n|m)?|fünfte(r|s|n|m)?|sechste(r|s|n|m)?|siebente(r|s|n|m)?|achte(r|s|n|m)?|neunte(r|s|n|m)?|zehnte(r|s|n|m)?|elfte(r|s|n|m)?|zwölfte(r|s|n|m)?|dreizehnte(r|s|n|m)?|vierzehnte(r|s|n|m)?|fünfzehnte(r|s|n|m)?|sechzehnte(r|s|n|m)?|siebzehnte(r|s|n|m)?|achtzehnte(r|s|n|m)?|neunzehnte(r|s|n|m)?|zwanzigste(r|s|n|m)?|einundzwanzigste(r|s|n|m)?|zweiundzwanzigste(r|s|n|m)?|dreiundzwanzigste(r|s|n|m)?|vierundzwanzigste(r|s|n|m)?|fünfundzwanzigste(r|s|n|m)?|sechsundzwanzigste(r|s|n|m)?|siebenundzwanzigste(r|s|n|m)?|achtundzwanzigste(r|s|n|m)?|neunundzwanzigste(r|s|n|m)?|dreißigste(r|s|n|m)?|einunddreißigste(r|s)?)"
  {:dim :ordinal
   :value (get {"erste" 1 "erster" 1 "erstes" 1 "ersten" 1 "erstem" 1 
              "zweite" 2 "zweiter" 2 "zweites" 2 "zweiten" 2 "zweitem" 2 
              "dritte" 3 "dritter" 3 "drittes" 3 "dritten" 3 "drittem" 3 
              "vierte" 4 "vierter" 4 "viertes" 4 "vierten" 4 "viertem" 4 
              "fünfte" 5 "fünfter" 5 "fünftes" 5 "fünften" 5 "fünftem" 5 
              "sechste" 6 "sechster" 6 "sechstes" 6 "sechsten" 6 "sechstem" 6 
              "siebente" 7 "siebenter" 7 "siebentes" 7 "siebenten" 7 "siebentem" 7 
              "achte" 8 "achter" 8 "achtes" 8 "achten" 8 "achtem" 8 
              "neunte" 9 "neunter" 9 "neuntes" 9 "neunten" 9 "neuntem" 9 
              "zehnte" 10 "zehnter" 10 "zehntes" 10 "zehnten" 10 "zehntem" 10 
              "elfte" 11 "elfter" 11 "elftes" 11 "elften" 11 "elftem" 11 
              "zwölfte" 12 "zwölfter" 12 "zwölftes" 12 "zwölften" 12 "zwölftem" 12 
              "dreizehnte" 13 "dreizehnter" 13 "dreizehntes" 13 "dreizehnten" 13 "dreizehntem" 13 
              "vierzehnte" 14 "vierzehnter" 14 "vierzehntes" 14 "vierzehnten" 14 "vierzehntem" 14 
              "fünfzehnte" 15 "fünfzehnter" 15 "fünfzehntes" 15 "fünfzehnten" 15 "fünfzehntem" 15 
              "sechzehnte" 16 "sechzehnter" 16 "sechzehntes" 16 "sechzehnten" 16" sechzehntem" 16
              "siebzehnte" 17 "siebzehnter" 17 "siebzehntes" 17 "siebzehnten" 17 "siebzehntem" 17 
              "achtzehnte" 18 "achtzehnter" 18 "achtzehntes" 18 "achtzehnten" 18 "achtzehntem" 18 
              "neunzehnte" 19 "neunzehnter" 19 "neunzehntes" 19 "neunzehnten" 19 "neunzehntem" 19 
              "zwanzigste" 20 "zwanzigster" 20 "zwanzigstes" 20 "zwanzigsten" 20 "zwanzigstem" 20 
              "einundzwanzigste" 21 "einundzwanzigster" 21 "einundzwanzigstes" 21 "einundzwanzigsten" 21 "einundzwanzigstem" 21
              "zwiundzwanzigste" 22 "zwiundzwanzigster" 22 "zwiundzwanzigstes" 22 "zwiundzwanzigsten" 22 "zwiundzwanzigstem" 22 
              "dreiundzwanzigste" 23 "dreiundzwanzigster" 23 "dreiundzwanzigstes" 23 "dreiundzwanzigsten" 23 "dreiundzwanzigstem" 23 
              "vierundzwanzigste" 24 "vierundzwanzigster" 24 "vierundzwanzigstes" 24 "vierundzwanzigsten" 24 "vierundzwanzigstem" 24 
              "fünfundzwanzigste" 25 "fünfundzwanzigster" 25 "fünfundzwanzigstes" 25 "fünfundzwanzigsten" 25 "fünfundzwanzigstem" 25
              "sechsundzwanzigste" 26 "sechsundzwanzigster" 26 "sechsundzwanzigstes" 26 "sechsundzwanzigsten" 26 "sechsundzwanzigstem" 26 
              "siebenundzwanzigste" 27 "siebenundzwanzigster" 27 "siebenundzwanzigstes" 27 "siebenundzwanzigsten" 27 "siebenundzwanzigstem" 27 
              "achtundzwanzigste" 28 "achtundzwanzigster" 28 "achtundzwanzigstes" 28 "achtundzwanzigsten" 28 "achtundzwanzigstem" 28 
              "neunundzwanzigste" 29 "neunundzwanzigster" 29 "neunundzwanzigstes" 29 "neunundzwanzigsten" 29 "neunundzwanzigstem" 29
              "dreißigste" 30 "dreißigster" 30 "dreißigstes" 30 "dreißigsten" 30 "dreißigstem" 30 
              "einunddreißigste" 31 "einunddreißigster" 31 "einunddreißigstes" 31 "einunddreißigsten" 31 "einunddreißigstem" 31}
              (-> %1 :groups first .toLowerCase))}

  "ordinal (digits)"
  #"0*(\d+) ?(\.|tens)"
  {:dim :ordinal
   :value (read-string (first (:groups %1)))}  ; read-string not the safest

  
  )
