# Costume Planner: deutsche Mac-Store-Inhalte

Auf Nutzerauftrag die Inhaltslieferung von Sewing Planner für Costume Planner
übernommen und um den PDF-Export ergänzt. Keine App-Builds, keine Paketierung
und keine Änderung der anderen Apps.

## Ergebnis

13 kopierbare deutsche Felddateien für Version 1.0 mit dem öffentlichen Namen
Costume Planner und der gewählten neuen Bundle-ID `de.appfabrik.costumeplanner`.
Fünf echte JavaFX-Aufnahmen in 2560 × 1600 RGB zeigen Übersicht, Arbeitsschritte,
Suche, JSON-Export und das PDF-Menü. Die Bilder stammen von Linux und sind bis
zum Vergleich mit der tatsächlichen Mac-Oberfläche als Entwürfe gekennzeichnet.

Fünf erfundene Kostümprojekte mit drei app-eigenen Illustrationen, sämtlichen
Statuswerten und 15 Arbeitsschritten. Ein Einzelprojekt-PDF und ein
Sammlungs-PDF entstanden über die vorhandene App. Die bestehende In-App-
Datenschutzerklärung liegt als TXT und HTML bei. Eine Anleitung ordnet die
Dateien den Feldern in App Store Connect zu.

Übergabeordner:
`kostuemplan/build/releases/mac-store-content/Costume-Planner-1.0-de-DE-2026-11-01-1052/`

## Prüfung

Feldlängen, Klartextformat, Identität und synthetische JSON-Daten geprüft.
Support- und Marketing-URL antworten mit HTTP 200. Python mit Ruff formatiert
und geprüft, Syntaxprüfung bestanden. Java-Aufnahmehelfer mit dem vorhandenen
Google Java Format formatiert und im Java-Quelltextmodus ausgeführt.

Isolierter Xvfb und neue temporäre Sammlung. Import übernimmt sämtliche
Projektfelder, Statuswerte, Farben, Fotos und Arbeitsschritte unverändert.
Wiederholter Import überspringt alle fünf Duplikate. Ein echter UI-JSON-Export
bei aktiver Suche erhält die gesamte Sammlung. Beide PDF-Aktionen über die
Oberfläche ausgeführt. Erneutes Öffnen prüft Texte, Farbwerte, Schritte,
Reihenfolge, Fotos und die erwarteten ein beziehungsweise fünf Seiten.
Der Sammlungs-PDF-Export ignoriert den aktiven Suchfilter wie vorgesehen.

Alle fünf Bilder und zwei mit Poppler gerenderte PDF-Beispielseiten visuell
angesehen. Laufzeit-JARs vor und nach Aufnahme unverändert. Die aktuelle
geprüfte App-JAR entspricht den bestehenden kompilierten Klassen und Ressourcen.
Die ältere App-JAR im App-Image wird nicht verwendet. Quell- und Datei-Hashes
werden in der Übergabe festgehalten.

## Grenzen

Kein Maven-Aufruf, keine vollständige Testsuite, keine neue App-Kompilierung,
kein ZIP, keine Signierung, kein Upload und keine Veröffentlichung.
Persönliche JSON- und PDF-Exporte sowie die laufende Nutzer-App bleiben unberührt.
Mac-Vergleich und native Funktionsabnahme, öffentliche Datenschutz-URL,
Prüferkontaktdaten sowie die restlichen Portalangaben bleiben offen.
Die historische Paketkennung wird bei dieser Inhaltsaufgabe nicht verändert.

Leerer lokaler Sicherungscommit vor Beginn: `a1c2866`.
Abschlusscommit nach Inhaltsprüfung. Kein Push.
