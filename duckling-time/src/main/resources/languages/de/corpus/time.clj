(
  ; Context map
  ; Tuesday Feb 12, 2013 at 4:30am is the "now" for the tests
  {:reference-time (time/t -2 2013 2 12 4 30 0)
   :min (time/t -2 1900)
   :max (time/t -2 2100)}

  "jetzt"
  "nächste"
  "nächster"
  "jetzt gleich"
  "sofort"
  (datetime 2013 2 12 4 30 00)

  "heute"
  ; "at this time"
  (datetime 2013 2 12)

  "gestern"
  (datetime 2013 2 11)

  "morgen"
  (datetime 2013 2 13)

  "Montag"
  "Mon."
  "Mo."
  "Nächsten Montag"
  (datetime 2013 2 18 :day-of-week 1)

  "Montag, 18. Februar"
  "Montag, 18. Feb."
  "Mo, 18. Feb."
  "Mon, 18. Februar"
  (datetime 2013 2 18 :day-of-week 1 :day 18 :month 2)

  "Dienstag"
  "Di."
  "Die."
  (datetime 2013 2 19)
  
  "Mittwoch"
  "Mi."
  (datetime 2013 2 13)

  "Donnerstag"
  "Do"
  (datetime 2013 2 14)

  "Freitag"
  "Frei."
  "Fr."
  (datetime 2013 2 15)

  "Samstag"
  "Sa."
  (datetime 2013 2 16)

  "Sonntag"
  "So."
  (datetime 2013 2 17)

  "erster März"
  "ersten März"
  "Erster März"
  "1. März"
  (datetime 2013 3 1 :day 1 :month 3)
  
  "3. März"
  "dritter März"
  (datetime 2013 3 3 :day 3 :month 3)

  "Freitag, den 20. September"
  "Freitag, 20. September"
  "am 20. September"
  "am 20 September"
  (datetime 2013 9 20)

  "Freitag 18 Juli 2014"
  "Freitag 18 Juli, 2014"
  (datetime 2014 7 18 :day-of-week 5)

  "die Iden des März"
  (datetime 2013 3 15 :month 3)

  "3. März 2015"
  ;"march 3rd 2015"
  ;"march third 2015"
  "03/03/2015"
  "3/3/2015"
  "3/3/15"
  "3.3.15"
  "3.3.2015"
  "3-3-15"
  "3-3-2015"
  "2015-3-3"
  "2015-03-03"
  (datetime 2015 3 3 :day 3 :month 3 :year 2015)

  "am 15."
  "am Freitag"
  (datetime 2013 2 15 :day 15)

  "der 15. Februar"
  "15. Februar"
  "15/02"
  "15-02"
  "15.02"
  (datetime 2013 2 15 :day 15 :month 2)

  "8. August"
  (datetime 2013 8 8 :day 8 :month 8)

  "Oktober 2014"
  (datetime 2014 10 :year 2014 :month 10)

  "31/10/1974"
  "31/10/74"
  "31-10-1974"
  (datetime 1974 10 31 :day 31 :month 10 :year 1974)

  "14. April 2015"
  "April 14, 2015"
  (datetime 2015 4 14 :day 14 :month 4 :years 2015)

  "nächsten Dienstag" ; when today is Tuesday, "mardi prochain" is a week from now
  (datetime 2013 2 19 :day-of-week 2)

  "nächsten Freitag"
  "nächsten Fr"
  "nächsten Frei"
  "nächsten Fr."
  "nächsten Frei."
  (datetime 2013 2 22 :day-of-week 2)

  "nächsten Samstag"
  "nächsten Sam"
  "nächsten Sa"
  "nächsten Sam."
  "nächsten Sa."
  (datetime 2013 2 23 :day-of-week 2)

  "nächsten Sonntag"
  "nächsten Son"
  "nächsten So"
  "nächsten Son."
  "nächsten So."
  (datetime 2013 2 24 :day-of-week 2)

  "übernächsten Freitag"
  "über nächsten Fr"
  (datetime 2013 3 1 :day-of-week 2)

  "nächsten März"
  (datetime 2014 3)

;  "March after next"
;  (datetime 2014 3)

  "Sonntag, 10. Februar"
  (datetime 2013 2 10 :day-of-week 7 :day 10 :month 2)

  "Mittwoch, 13. Februar"
  (datetime 2013 2 13 :day-of-week 3 :day 13 :month 2)

  "Montag, 18. Februar"
  "Mo, 18. Feb."
  "Mo, 18. Februar"
  (datetime 2013 2 18 :day-of-week 1 :day 18 :month 2)

;   ;; Cycles

  "diese Woche"
  (datetime 2013 2 11 :grain :week)
  
  "letzte Woche"
  (datetime 2013 2 4 :grain :week)

  "nächste Woche"
  "kommende Woche"
  (datetime 2013 2 18 :grain :week)

  "letztes Monat"
  (datetime 2013 1)

  "nächstes Monat"
  (datetime 2013 3)

  "dieses Quartal"
  (datetime 2013 1 1 :grain :quarter)

  "nächstes Quartal"
  (datetime 2013 4 1 :grain :quarter)

  "drittes Quartal"
  (datetime 2013 7 1 :grain :quarter)

  "4. Quartal 2018"
  (datetime 2018 10 1 :grain :quarter)

  "letzes Jahr"
  (datetime 2012)

  "dieses Jahr"
  "heuer"
  (datetime 2013)

  "nächstes Jahr"
  (datetime 2014)

  "letzten Sonntag"
  "letzen Sonntag"
;  "sunday from last week"
;  "last week's sunday"
  (datetime 2013 2 10 :day-of-week 7)

  "letzten Dienstag"
  "letzen Dienstag"
  (datetime 2013 2 5 :day-of-week 2)

  "nächsten Mittwoch"
  "Mittwoch nächster Woche"
  (datetime 2013 2 20 :day-of-week 3)

  "Freitag nächster Woche"
  (datetime 2013 2 22 :day-of-week 5)

  "diesen Montag"
  "Montag dieser Woche"
  (datetime 2013 2 11 :day-of-week 1)

  "diesen Dienstag"
  "Dienstag dieser Woche"
  "Dienstag der aktuellen Woche"
  (datetime 2013 2 12 :day-of-week 2)

  "nächsten Dienstag" ; when today is Tuesday, "mardi prochain" is a week from now
  "kommenden Dienstag" ;
  (datetime 2013 2 19 :day-of-week 2)

  "diesen Mittwoch"
  "kommenden Mittwoch"
  (datetime 2013 2 13 :day-of-week 3)

  "nächsten Mittwoch"
  "Mittwoch nächster Woche"
  (datetime 2013 2 20 :day-of-week 3)

  "übermorgen"
  (datetime 2013 2 14)

  "vorgestern"
  (datetime 2013 2 10)

  "letzter Montag im März"
  "letzer Montag im März"
  (datetime 2013 3 25 :day-of-week 1)

  "letzer Sonntag des März 2014"
  (datetime 2014 3 30 :day-of-week 7)

  "dritter Tag im Oktober"
  (datetime 2013 10 3)

  "erste Woche im Oktober 2014"
  (datetime 2014 10 6 :grain :week)

;  "the week of october 6th"
;  "the week of october 7th"
;  (datetime 2013 10 7 :grain :week)

  "letzter Tag im Oktober 2015"
  (datetime 2015 10 31)

  "letzte Woche im September 2014"
  (datetime 2014 9 22 :grain :week)

  ;; nth of
  "erster Dienstag im Oktober"
  (datetime 2013 10 1)

  "dritter Dienstag im September 2014"
  (datetime 2014 9 16)

  "erster Mittwoch im Oktober 2014"
  (datetime 2014 10 1)

  "zweiter Mittwoch im Oktober 2014"
  (datetime 2014 10 8)

  ;; nth after

  "dritter Dienstag nach Weihnachten 2014"
  (datetime 2015 1 13)

  ;; Hours

  "3"
  "3 Uhr"
  "3Uhr"
  "um 3"
  "um 3Uhr"
  "um 3 Uhr"
  "@3"
  (datetime 2013 2 12 15)

  "um 5"
  "um 5Uhr"
  "um 5 Uhr"
  "5 Uhr am Morgen"
  "5 Uhr Morgens"
  (datetime 2013 2 12 5)

  "um 15"
  "um 15Uhr"
  "um 15 Uhr"
  "@ 3pm"
;  "3PM"
;  "3pm"
;  "3 oclock pm"
  "um 3 am Nachmittag"
  "3 Uhr Nachmittags"
  (datetime 2013 2 12 15 :hour 3 :meridiem :pm)

  "15 18"
  "15Uhr 18"
  "15 Uhr 18"
  "um 15Uhr 18"
  (datetime 2013 2 12 15 18)



  ;; FIXME pm overrides precision
  "ungefähr um 3 Uhr"
  "gegen 3 Uhr"
  "etwa um 3 Uhr"
  "so um 3 Uhr"
  "etwa gegen 3 Uhr"
  "ca. 3 Uhr"
  "circa 3 Uhr"
  "ungefähr drei"
  (datetime 2013 2 12 15 :hour 3 :meridiem :pm) ;; :precision "approximate"

  "morgen, genau um 5 Uhr" ;; FIXME precision is lost
  "morgen, pünktlich um 5 Uhr"
  "morgen, exakt 5 Uhr"
  (datetime 2013 2 13 5 :hour 5 :meridiem) ;; :precision "exact"

  "15 nach drei"
  "viertel nach drei"
  "3:15 am nachmittag"
  "15:15"
  "3:15pm"
  "3:15PM"
  "3:15p"
  (datetime 2013 2 12 15 15 :hour 3 :minute 15 :meridiem :pm)

  "zwanzig nach 3"
  "20 Minuten nach 3"
  "20 Minuten nach 3 Uhr"
  "3:20p"
  (datetime 2013 2 12 15 20 :hour 3 :minute 20 :meridiem :pm)

  "halb vier"
  "halb vier nachmittags"
  "15:30"
  "3:30pm"
  "3:30PM"
  "330 p.m."
  "3:30 p m"
  (datetime 2013 2 12 15 30 :hour 3 :minute 30 :meridiem :pm)

  "15:23:24"
  (datetime 2013 2 12 15 23 24 :hour 15 :minute 23 :second 24)

  "dreiviertel zwölf"
  "dreiviertel 12"
  "drei viertel zwölf"
  "viertel vor 12"
  "11:45"
  (datetime 2013 2 12 11 45 :hour 11 :minute 45)

  "8 Uhr abends"
  "20 Uhr"
  "acht Uhr abends"
  "abends um 8"
  "abends um acht"
;  "eight tonight"
;  "8 this evening"
  (datetime 2013 2 12 20)

  ;; Mixing date and time

  "7 Uhr 30 abends am Freitag, den 20. September"
  "19:30, am Freitag, den 20. September"
  (datetime 2013 9 20 19 30 :hour 7 :minute 30 :meridiem :pm)

  "am Samstag um 9 Uhr"
  (datetime 2013 2 16 9 :day-of-week 6 :hour 9 :meridiem :am)

  "Freitag, 18. Juli 2014 07:00"
  (datetime 2014 7 18 7 0 :day-of-week 5 :hour 7 :meridiem :pm)


; ;; Involving periods

  "in 1 Sekunde"
  "in einer Sekunde"
  (datetime 2013 2 12 4 30 1)

  "in 30 Sekunden"
  "in 30 sek"
  "in 30 sec"
  (datetime 2013 2 12 4 30 30)

  "in einer Minute"
  "in einer min."
  (datetime 2013 2 12 4 31 0)

  "in 2 min."
  "in 2min."
  "in 2 Minuten"
  "in zwei Minuten"
  (datetime 2013 2 12 4 32 0)

  "in 3600 Sekunden"
  "in 60 Minuten"
  (datetime 2013 2 12 5 30 0)
  
  "in einer Stunde"
  "in 1h"
  "in 1 std."
  "in 1std"
  (datetime 2013 2 12 5 30)

  "in einer halben Minute"
  (datetime 2013 2 12 4 30 30)

  "in einer halben Stunde"
  (datetime 2013 2 12 5 0 0)

  "in zweieinhalb Stunden"
  (datetime 2013 2 12 7 0 0)

  "in einem halben Tag"
  (datetime 2013 2 12 16 30)

  "in ein paar Stunden"
  (datetime 2013 2 12 6 30)

  "in wenigen Stunden"
  (datetime 2013 2 12 6 30)

  "in einigen Stunden"
  (datetime 2013 2 12 7 30)

  "in 24 Stunden"
  (datetime 2013 2 13 4 30)

;  "in a day"
;  "a day from now"
;  (datetime 2013 2 13 4)

  "in drei Jahren"
  "in 3 Jahren"
  (datetime 2016 2)

  "in 7 Tagen"
  "in sieben Tagen"
  (datetime 2013 2 19 4)

  "in 1 Woche"
  "in einer Woche"
  (datetime 2013 2 19)

  "in ungefähr einer halben Stunde" ;; FIXME precision is lost
  (datetime 2013 2 12 5 0 0) ;; :precision "approximate"

  "vor sieben Tagen"
  (datetime 2013 2 5 4)

  "vor einer Woche"
  (datetime 2013 2 5)

  "vor 14 Tagen"
  (datetime 2013 1 29 4)

  "vor zwei Wochen"
  (datetime 2013 1 29)

  "vor drei Wochen"
  (datetime 2013 1 22)

  "vor drei Monaten"
  (datetime 2012 11 12)

  "vor zwei Jahren"
  (datetime 2011 2)

  "7 Tage von jetzt"
  (datetime 2013 2 19 4)

  "14 Tage von jetzt"
  (datetime 2013 2 26 4)

  "Eine Woche von jetzt an"
  "1 Woche von jetzt"
  (datetime 2013 2 19)

  ; Seasons

  "diesen Sommer"
  (datetime-interval [2013 6 21] [2013 9 24])

  "diesen Winter"
  (datetime-interval [2012 12 21] [2013 3 21])

  ; US holidays (http://www.timeanddate.com/holidays/us/)

  "Heiliger Abend"
  (datetime 2013 12 24)

  "xmas"
  "Weihnachten"
  (datetime 2013 12 25)

  "Silvester"
  (datetime 2013 12 31)

  "Neujahr"
  (datetime 2014 1 1)

  "Valentinstag"
  (datetime 2013 2 14)

;  "memorial day"
  "Volkstrauertag"
  (datetime 2013 5 27)

  "Muttertag"
  (datetime 2013 5 12)

;  "Vatertag"
;  (datetime 2013 6 16)

;  "memorial day week-end"
  "Volkstrauertag Wochenende"
  (datetime-interval [2013 5 24 18] [2013 5 28 0])

; "independence day"
  "Tag der Unabhängigkeit"
  "Unabhängigkeitstag"
  "4. Juli"
  (datetime 2013 7 4)

  "Tag der Arbeit"
  (datetime 2013 5 1)
  
 "Tag der deutschen Einheit"
  (datetime 2013 10 3) 


;  "halloween"
  "Halloween"
  (datetime 2013 10 31)

;  "thanksgiving day"
  "thanksgiving"
  "Erntedankfest"
  (datetime 2013 11 28)

  ; Part of day (morning, afternoon...)

  "heute Nachmittags"
  (datetime-interval [2013 2 12 12] [2013 2 12 19])

  "heute Abend"
  (datetime-interval [2013 2 12 18] [2013 2 12 22])

  "heute Nacht"
  (datetime-interval [2013 2 12 20] [2013 2 13 00])

  "letzes Wochenende"
  "letzes WE"
  (datetime-interval [2013 2 8 18] [2013 2 10 22])

  "morgen Abend"
  (datetime-interval [2013 2 13 18] [2013 2 13 22])

  "morgen Nacht"
  (datetime-interval [2013 2 13 20] [2013 2 14 00])

  "morgen Mittag"
  "morgen mittags"
  (datetime 2013 2 13 12)

  "morgen um die Mittagszeit"
  "morgen zur Mittagszeit"
  "morgen zum Mittagessen"
  (datetime-interval [2013 2 13 12] [2013 2 13 14])

  "gestern Abend"
  (datetime-interval [2013 2 11 18] [2013 2 11 22])

  "dieses Wochenende"
  (datetime-interval [2013 2 15 18] [2013 2 17 22])

  "Montag morgens"
  (datetime-interval [2013 2 18 5] [2013 2 18 12])

  "am 15. Februar früh morgens"
  (datetime-interval [2013 2 15 4] [2013 2 15 8])


  ; Intervals involving cycles

;  "last 2 seconds"
  "letzten zwei sekunden"
  "in den letzten zwei sekunden"
  (datetime-interval [2013 2 12 4 29 58] [2013 2 12 4 30 00])

;  "next 3 seconds"
  "nächsten drei Sekunden"
  "in den nächsten drei Sekunden"
  (datetime-interval [2013 2 12 4 30 01] [2013 2 12 4 30 04])

;  "last 2 minutes"
  "letzten zwei min."
  "in den letzten zwei min."
  (datetime-interval [2013 2 12 4 28] [2013 2 12 4 30])

;  "next 3 minutes"
  "nächsten 3 minuten"
  "während der nächsten 3 minuten"
  (datetime-interval [2013 2 12 4 31] [2013 2 12 4 34])

  "in der letzten Stunde"
  "während der letzten Stunde"
  "innerhalb der letzten Stunde"
  (datetime-interval [2013 2 12 3] [2013 2 12 4])

  "in der nächsten Stunde"
  "während der kommenden Stunde"
  "innerhalb der nächsten Stunde"
  (datetime-interval [2013 2 12 5] [2013 2 12 6])

  "nächsten drei Stunden"
  "in den nächsten drei Stunden"
  "in den nächsten 3 Stunden"
  "innerhalb der nächsten drei Stunden"
  "während der nächsten drei Stunden"
  (datetime-interval [2013 2 12 5] [2013 2 12 8])

  "letzten 2 Tage"
  "in den letzen 2 Tagen"
  "während der letzen zwei Tagen"
  (datetime-interval [2013 2 10] [2013 2 12])

  "nächsten 3 Tage"
  "nächsten 3 Nächte"
  "in den nächsten 3 Tagen"
  "in den folgenden 3 Tagen"
  "innerhalb der kommenden 3 Tage"
  "während der nachfolgenden drei Tage"
  (datetime-interval [2013 2 13] [2013 2 16])

; TODO: not supported as this requires to distinguish
;       between singular and plural for cycles
  "in den nächsten Tagen"
  "die nächsten Tage"
  (datetime-interval [2013 2 13] [2013 2 16])

  "letzten 2 Wochen"
  "in den letzten 2 Wochen"
  "während der letzten 2 Wochen"
  "innerhalb der letzten zwei Wochen"
  (datetime-interval [2013 1 28 :grain :week] [2013 2 11 :grain :week])

  "nächsten drei Wochen"
  "in den nächsten drei Wochen"
  "während der nächsten 3 Wochen"
  "innerhalb der nächsten drei Wochen"
  (datetime-interval [2013 2 18 :grain :week] [2013 3 11 :grain :week])

  "letzten 2 Monate"
  "in den letzten 2 Monate"
  "während der letzten 2 Monate"
  "innerhalb der letzten zwei Monate"
  (datetime-interval [2012 12] [2013 02])

  "nächsten 3 Monaten"
  "in den nächsten 3 Monaten"
  "während der nächsten 3 Monate"
  "innerhalb der nächsten drei Monaten"
  (datetime-interval [2013 3] [2013 6])

  "in den letzten 2 Jahren"
  "während der letzten 2 Jahre"
  "in den vergangenen zwei Jahren"
  (datetime-interval [2011] [2013])

  "in den nächsten 3 Jahren"
  "während der nächsten drei Jahren"
  (datetime-interval [2014] [2017])


  ; Explicit intervals

  "13.-15. Juli"
  "13-15 Juli"
  "13. - 15. Juli"
  "13 - 15 Juli"
  "13. bis zum 15. Juli"
  "Juli 13. - 15."
  "Juli 13 - 15"
  "Juli 13-15"
  (datetime-interval [2013 7 13] [2013 7 16])

  "8.-12. August"
  "8-12 August"
  "8. - 12. August"
  "8. bis 12. August"
  "August 8 - 12"
  "August 8-12"
  (datetime-interval [2013 8 8] [2013 8 13])

  "9:30 - 11:00"
  "9:30 bis 11:00"
  (datetime-interval [2013 2 12 9 30] [2013 2 12 11 1])

  "9 Uhr 30 bis 11 Uhr"
  "9Uhr30 bis 11Uhr"
  (datetime-interval [2013 2 12 9 30] [2013 2 12 12 0])

  "am Donnerstag zwischen 9:30 - 11:00"
  "am Donnerstag nach 9:30 und vor 11:00"
  "zwischen 9:30 und 11:00 am Donnerstag"
  "nach 9:30 aber vor 11:00 am Donnerstag"
;  "9:30 - 11:00 on Thursday"
;  "later than 9:30 but before 11:00 on Thursday"
;  "Thursday from 9:30 to 11:00"
  (datetime-interval [2013 2 14 9 30] [2013 2 14 11 1])

  "am Donnerstag nach 9 aber vor 11Uhr"
  (datetime-interval [2013 2 14 9] [2013 2 14 12])

  "11:30-13:30" ; go train this rule!
  (datetime-interval [2013 2 12 11 30] [2013 2 12 13 31])

  "13:30 am Samstag, den 21. September"
  "um 13 Uhr 30, am Samstag, den 21. September"
  (datetime 2013 9 21 13 30)

  "in den nächsten 2 Wochen"
  "in den nächsten 2 Wochen"
  (datetime-interval [2013 2 12 4 30 0] [2013 2 26])

;; NOTE: the following test fails, but deliver the correct result
;;       this means that the assertion is somehow wrong :(
  "vor 14 Uhr"
  "nicht später als 14Uhr"
  (datetime 2013 2 12 14 0 :direction :before)

  "vor 14:00"
  "spätestens um 14:00"
  (datetime 2013 2 12 14 0 0 :direction :before)

  "ab 14 Uhr"
  "nach 14 Uhr"
  "frühestens um 14Uhr"
  (datetime 2013 2 12 14 0 :direction :before)

  "nach 14:00"
  "ab 14:00"
  "frühestens um 14:00"
  (datetime 2013 2 12 14 0 0 :direction :after)

  "bis 14 Uhr"
  "bis 14:00"
  (datetime-interval [2013 2 12 4 30 0] [2013 2 12 14])

  "noch heute"
  (datetime-interval [2013 2 12 4 30 0] [2013 2 13 0])

  "noch dieses Monat"
  (datetime-interval [2013 2 12 4 30 0] [2013 3 1 0])

  "bis Ende nächstes Monat"
  (datetime-interval [2013 2 12 4 30 0] [2013 4 1 0])
  ; Timezones

  "16 Uhr CET"
  (datetime 2013 2 12 16 :hour 4 :meridiem :pm :timezone "CET")

  "Donnerstag 8:00 GMT"
  (datetime 2013 2 14 8 00 :timezone "GMT")

  ;; Bookface tests
  "heute um 14:00"
  "heute um 14 Uhr"
  (datetime 2013 2 12 14)

;  "4/25 at 4:00pm"
;  (datetime 2013 4 25 16 0)

  "morgen 3 Uhr nachmittags"
  "morgen 15 Uhr nachmittags"
  (datetime 2013 2 13 15)

  "nach 14 Uhr"
  "nach 14:00"
  (datetime 2013 2 12 14 :direction :after)

  "nach 5 Tagen"
  "nach fünf Tagen"
  (datetime 2013 2 17 4 :direction :after)  

  "morgen nach 14 Uhr"
  "morgen nach 14:00"
  "tomorrow after 2pm" ;; FIXME this is actually not ambiguous it's 2pm - midnight.
  (datetime 2013 2 13 14 :direction :after)

  "vor 11 Uhr"
  "vor 11"
  (datetime 2013 2 12 11 :direction :before)

  "morgen bevor 11 Uhr" ;; FIXME this is actually not ambiguous. it's midnight to 11 am
  (datetime 2013 2 13 11 :direction :before)

  "am Nachmittag"
  (datetime-interval [2013 2 12 12] [2013 2 12 17])

  "um 13:30"
  "um halb zwei nachmittags"
  (datetime 2013 2 12 13 30)

  "in 15 Minuten"
  (datetime 2013 2 12 4 45 0)

  "nach dem Mittagessen"
  (datetime-interval [2013 2 12 13] [2013 2 12 17])

  "10:30"
  (datetime 2013 2 12 10 30)

  "morgens" ;; how should we deal with fb mornings?
  (datetime-interval [2013 2 12 5] [2013 2 12 12])

  "nächsten Montag"
  (datetime 2013 2 18 :day-of-week 1)


)
