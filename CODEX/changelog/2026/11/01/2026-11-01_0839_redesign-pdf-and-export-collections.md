# Farbige PDF-Berichte und gesamte Sammlung

Auf Nutzerauftrag PDF-Berichte an die vorhandenen Flutter-/React-Berichte
angelehnt. Violette App-Akzente, eingebettete fette Schrift, Datenkarten,
proportionale Projektfotos, Farbkacheln mit Hexcode, Statusflächen und gegliederte
Arbeitsschritte mit Erledigt-Zähler und Fortschrittsbalken. Transparente Farben
erhalten ein Schachbrettmuster, freie Farbtexte bleiben lesbar. Lange Inhalte
fließen vollständig auf Folgeseiten. Seitenzahlen nennen die Gesamtseitenzahl.

Nach ausdrücklicher Nutzerwahl bietet das PDF-Menü Einzelprojekt und gesamte
Sammlung. Der Sammlungsexport verwendet einen konsistenten Lesestand und
alphabetische Reihenfolge in der App-Sprache. Fotos werden nacheinander geladen,
PDF-Streams auf temporäre Dateien ausgelagert. Jedes Projekt beginnt auf einer
neuen Seite. Sicheres Überschreiben, Fehlerbehandlung und Hintergrundarbeit bleiben.
Alle neuen Texte in sechs Sprachen, Hinweise und Dokumentation aktualisiert.

Prüfung: Formatter, Compiler-Lint, Enforcer und SpotBugs bestanden. Testquellen
einschließlich vier neuer Sammlungsregressionen kompilieren. Gemäß app-lokaler
Vorgabe keine JUnit-/GUI-Suite ausgeführt. Synthetische einseitige PDFs für alle
sechs Sprachen, zwei- und dreiseitige Sammlungen sowie ein 20-seitiges Beispiel
erzeugt. PDFBox öffnete und renderte die Beispiele. Poppler bestätigte die
Seitenränder aller Textpositionen. Alle 2.500 Notiz- und 1.800 Schrittphrasen
im langen Beispiel erhalten. Repräsentative Renderbilder visuell geprüft.
SQLite-Datei nach Sammlungsexport bytegleich. Details in docs/verification.md.

Sicherungscommit vor Umsetzung: `e5ce1b8`. Dauerhafte Pflicht für lokale Commits
vor und nach Entwicklungsaufgaben in Workspace- und App-AGENTS.md festgehalten.
Root hat kein Git-Repository, die Regel wird in den betroffenen App-Repositories
umgesetzt. Persönliche JSON-Sicherung bleibt unverändert und unversioniert.
App-Image aktualisiert, Hash verglichen und neue App-Instanz geöffnet.
Kein Push und keine Veröffentlichung.
