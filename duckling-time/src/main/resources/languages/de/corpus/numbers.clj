(
  ; Context map
  {}

  "0"
  "null"
  "Null"
  (number 0)

  "1"
  "ein"
  "eins"
  (number 1)

  "33"
  "dreiunddreissig"
  "dreiunddreißig"
  "drei und dreissig"
  "0033"
  (number 33)
  
  "14"
  "vierzehn"
  (number 14)
  
  "16"
  "sechzehn"
  (number 16)

  "17"
  "siebzehn"
  (number 17)

  "18"
  "achzehn"
  "achtzehn"
  (number 18)

  "1.1"
  "1.10"
  "01.10"
  (number 1.1)

;; not yet supported
;  "1,1"
;  "1,10"
;  "01,10"
;  (number 1.1)

  "0.77"
  ".77"
  (number 0.77)

;; not yet supported
;  "0,77"
;  ",77"
;  (number 0.77)
  
  "100,000"
  "100000"
  "100K"
  "100k"
  (number 100000)
  
  "3M"
  "3,000K"
  "3000000"
  "3,000,000"
  (number 3000000)
  
  "1,200,000"
  "1200000"
  "1.2M"
  "1200K"
  ".0012G"
  (number 1200000)

  "- 1,200,000"
  "- 1200000"
  "minus 1,200,000"
;  "negativ 1200000"
  "- 1.2M"
  "- 1200K"
  "- 0.0012G"
  (number -1200000)

  "5 Tausend"
  "fünf Tausend"
  (number 5000)

  "eins zwei zwei"
  (number 122)

  "zweihunderttausend"
  "200 Tausend"
  "zweihunderd Tausend"
  (number 200000)

  "Einundzwanzigtausendundelf"
  "Einundzwanzigtausendelf"  
  (number 21011)

  "Siebenhunderteinundzwanzigtausendundzwölf"
  "Siebenhunderteinundzwanzigtausendzwölf"
  (number 721012)

  "Einunddreißig Millionen zweihundertfünfzig Tausend und siebenhunderteinundzwanzig"
  (number 31256721)

  "4."
  "vierte"
  (ordinal 4)
)

