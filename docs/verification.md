# Prüfbericht

Stand: 26. Oktober 2026, KostümPlan 0.1.0.

## Ausgeführte automatische Prüfungen

Linux x64, Eclipse Temurin JDK 21.0.11, JavaFX 21.0.11,
SQLite JDBC 3.53.4.0, PDFBox 3.0.8 und Maven 3.9.16 über Wrapper.

```bash
./mvnw spotless:apply verify -Dkostuemplan.uiTests=true
bash -n scripts/run.sh scripts/package.sh
git diff --cached --check
```

Der vollständige Lauf auf einem separaten Xvfb-Bildschirm meldet **138 Tests,
keine Fehler, keine Fehlschläge und keine übersprungenen Tests**.
122 Fälle laufen ohne Oberfläche, 16 weitere prüfen echte JavaFX-Steuerelemente.
Compiler-Lint mit `-Werror`, Maven Enforcer mit Dependency Convergence, Spotless
und SpotBugs bestehen. SpotBugs meldet keine Befunde. POM, SpotBugs-XML und
Workflow-YAML wurden geparst. Shellsyntax, Paketressourcen und Lizenzkopien
wurden kontrolliert.

Hier wurde der bereits lokal entpackte Xvfb aus `/tmp/buecherregal-tools/root/usr/bin/Xvfb`
auf Bildschirm `:94` verwendet. Das ist ein Entwicklungswerkzeug außerhalb der
Apps, keine gemeinsame App-Laufzeit. Auf einem System mit installiertem Xvfb:

```bash
xvfb-run --auto-servernum ./mvnw verify -Dkostuemplan.uiTests=true
```

Bei einfachem `./mvnw verify` sind die UI-Tests absichtlich deaktiviert.
Alle Datenbanken, Bilder und Projekte sind synthetisch. Normale Benutzerdaten
wurden nicht geöffnet. Logdateien und erzeugte PDFs liegen im ignorierten `target`.

## Übernommene Grundfunktionen

Die angepassten NähPlan-Tests prüfen weiterhin CRUD, gleichnamige Projekte,
stabile IDs, alle Textfelder, Unicode-Suche, mehrere passende Schritte ohne
Doppelungen, Pflichtfelder, SQLite-Neustart, Fremdschlüssel, Statusschlüssel,
alle Statuswechsel und unabhängige Häkchen. Schritt-UUIDs und explizite
Positionen bleiben nach Bearbeitung und Neustart erhalten.

JPEG/PNG-Inhaltsprüfung, eigene defensive Fotokopie, Quellenlöschung,
Ersetzen und Entfernen, beschädigte Bilder, 10 MiB, 6000 Pixel je Seite und
16 Millionen Pixel sind abgedeckt. Ein später Fehler beim Schritt-INSERT
rollt den vollständigen Projektstand zurück. Texte, Foto, Status und Schritte
bleiben auch nach Sperren und ungültigen Eingaben erhalten.

Zehn echte Editor-Tests prüfen gemeinsame Entwürfe, alleinige Schrittänderungen,
Abbrechen, Escape, Fensterschließen, Weiterbearbeiten als sichere Vorauswahl,
Verwerfen, Validierung mit Korrektur, Speicherfehler mit erneutem Versuch und
veraltete Foto-Callbacks.

## PDF- und Exporttests

| Bereich | Nachweis |
| --- | --- |
| Vollständiges Projekt | PDF erneut geladen, alle Felder, Umlaute, ß, Euro, typografische Zeichen, Foto und gemischte Häkchen geprüft |
| Minimales Projekt | Name, Geplant und verständlicher Leerzustand, keine unnötigen optionalen Felder oder Bilder |
| Status | Alle drei Statuswerte werden deutsch ausgegeben, jeweils mit offenen und erledigten Schritten |
| Schrift und Foto | Schrift eingebettet, Bildpixel und Dimensionen aus der PDF gelesen, proportionaler Bildbereich im gerenderten Ergebnis vermessen |
| Umbrüche | 180 Notizzeilen mit Leerzeilen, sehr lange Wörter, langer Titel und 160 Schritte über 16 Seiten |
| Vollständigkeit | Alle Zeilenmarker und Schritte in Reihenfolge im erneut gelesenen PDF-Text, überlange Wörter vollständig rekonstruiert |
| Positionen | Textkoordinaten auf allen Seiten innerhalb der Ränder, separate Prüfung expliziter Leerzeilen und verschiedener Zeilenenden |
| Unbekannte Zeichen | Fehlende Glyphen, Emoji und unsichtbare Steuerzeichen erzeugen einen ausdrücklichen Unicode-Hinweis |
| Gespeicherter Stand | Vollständiger Snapshot inklusive Foto, unabhängig von späterer Änderung und anderen Projekten |
| Parallelität | 100 konkurrierende vollständige Datenbankänderungen und 100 Snapshot-Lesevorgänge, kein vermischter Stand |
| Originalfoto gelöscht | Gespeichertes JPEG nach Quellenlöschung exportiert, Bildobjekt in erneut geöffneter PDF nachgewiesen |
| Daten unverändert | Datenbankdatei nach erfolgreichem und fehlgeschlagenem Export bytegenau identisch |
| Zielnamen | Windows-Gerätenamen, problematische Zeichen, Punkte, Unicode, Byte-Limit und PDF-Endung |
| Überschreiben | Ohne Freigabe kein Rendereraufruf, nach Freigabe vollständig lesbare Ersatz-PDF |
| Schreibfehler | Teilweise geschriebene temporäre Datei, simulierter belegter Zielpfad und fehlender Zielordner, vorhandene Datei bleibt erhalten |
| Konkurrierendes Ziel | Zwischenzeitlich angelegte oder geänderte Datei wird nicht überschrieben, Dateiverweise werden abgelehnt |
| Aufräumen | Temporäre Exportdateien nach Erfolg und Fehler entfernt |

Sechs zusätzliche JavaFX-Tests prüfen Dateiauswahl-Abbruch, abgelehntes und
bestätigtes Überschreiben, gespeicherte statt veralteter UI-Texte, Fehler mit
Wiederholung, Hintergrundarbeit, konkurrierende Klicks und feste Projektidentität.
Der Hauptfenster-Button benötigt eine Auswahl. Der Editor enthält keine Exportaktion.
Die nativen Dialoge sind in diesen sechs Tests durch einen kontrollierten Adapter
ersetzt. Die echten nativen Dialoge wurden zusätzlich aus dem Paket bedient.

## Gerenderte PDFs visuell geprüft

`PdfBoxProjectWriterTest` öffnet die erzeugten PDFs erneut und rendert jede Seite
mit PDFBox. Repräsentative PNGs bleiben unter `target/verification/pdf`.
Die vollständige Projektseite sowie die Seiten 1, 2, 10, 13, 14 und 16 des langen
Dokuments wurden visuell angesehen. Schrift, Foto, Häkchen, Ränder, Notiz-
Leerzeilen und Übergänge zwischen Abschnitten sind lesbar. Lange Wörter und
Schritte laufen ohne Abschneiden oder Überlagerung über die Seiten.

Zusätzlich wurde der Paketexport mit Poppler `pdftotext`, `pdfinfo` und
`pdftoppm` unabhängig geöffnet und gerendert. Vollständige Projektseite und
Checklisten-Folgeseite wurden visuell geprüft. Die Checklistenkennzeichen werden
zusätzlich als „Offen“ und „Erledigt“ ausgeschrieben. Poppler kann beim Extrahieren
Ligaturen als einzelne Unicode-Zeichen liefern. Der Inhaltsvergleich normalisiert
sie mit NFKC. Die Anzeige und der Text sind vollständig.

## Echte Oberfläche und eingebettete Laufzeit

```bash
./scripts/package.sh app-image
./target/app-image/KostuemPlan/bin/KostuemPlan --version
./target/app-image/KostuemPlan/bin/KostuemPlan --data-dir=/absoluter/testpfad
```

Das Linux-x64-App-Image wurde gebaut. Der Launcher meldet `KostümPlan 0.1.0`.
Die Oberfläche wurde ohne `JAVA_HOME` und mit `PATH=/nonexistent` gestartet.
Die Prozessbibliotheken zeigen ausdrücklich
`target/app-image/KostuemPlan/lib/runtime/lib/server/libjvm.so`.
Die PDFBox-Archive, FontBox, pdfbox-io, Commons Logging, DejaVu-TTF und sämtliche
app-lokalen Lizenzdateien sind im Paket enthalten. Die TTF im paketierten JAR
und die Lizenzkopien wurden bytegenau gegen die Quellen geprüft.

Die echte App wurde mit `xdotool` auf einem separaten Xvfb-Bildschirm bedient.
Ein leeres Testverzeichnis wurde von der App initialisiert. Danach wurden drei
synthetische Projekte über SQLite vorbereitet. Das synthetische Originalbild
wurde gelöscht. Die App lud nur den eigenen gespeicherten Foto-BLOB.

| Ablauf aus dem Paket | Ergebnis |
| --- | --- |
| Leere Sammlung | Leerzustand und deaktivierter Export sichtbar |
| Ein Projekt auswählen | Export aktiviert, Foto, Notiz und gespeicherte Schritte sichtbar |
| Nativen Speicherdialog abbrechen | Keine PDF erzeugt, Daten unverändert |
| Vollständiges Projekt Fertig mit offenem Schritt exportieren | Alle Felder, Foto, Status und Häkchen in erneut geöffneter PDF vorhanden |
| Bestehende PDF auswählen, native Rückfrage bestätigen, app-eigene Rückfrage mit Enter abbrechen | Abbrechen ist Standard, vorherige PDF-Prüfsumme bleibt identisch |
| Beide Rückfragen ausdrücklich bestätigen | Lesbare PDF erfolgreich ersetzt |
| Minimales Projekt exportieren | Lesbare einzelne A4-Seite ohne optionale Inhalte |
| Projekt mit langen Notizen, überlangem Wort und 70 Schritten exportieren | Sieben Seiten, letzter Notizmarker und alle Schritte in Reihenfolge nachgewiesen |
| Zielordner nach Dateiauswahl vor bestätigtem Überschreiben auf nicht beschreibbar setzen | Verständliche Fehlermeldung, vorhandene Zieldatei unverändert |
| Nach NÄHEN suchen | Arbeitsschritt findet das Projekt, kein doppelter Treffer |
| Gemeinsamen Editor öffnen und ohne Änderungen abbrechen | Gespeicherter Stand unverändert, kein Export im Dialog |
| Hauptfenster auf 860 × 700 verkleinern | Exportaktion, Status, Foto und Checkliste lesbar |
| Paket neu starten und erneut exportieren | Foto trotz fehlender Quelle vorhanden, Texte und Häkchen erhalten, PDF erfolgreich |

Die SQLite-Datei war nach allen Export-, Abbruch- und Fehlerabläufen einschließlich
Neustart bytegenau identisch zum Ausgangszustand. Keine temporären Exportreste
blieben im Testziel zurück. Dies ist ein GUI- und Exportnachweis, kein bloßer
Versionsaufruf.

Versionierte Screenshots mit ausschließlich synthetischen Daten:
[Hauptfenster](screenshots/kostuemplan.png),
[Projekt-PDF](screenshots/pdf-project.png),
[langer Schritt](screenshots/pdf-long-step.png),
[Überschreibbestätigung](screenshots/overwrite.png).

## Offene native und Store-Prüfungen

Windows und macOS stehen hier nicht zur Verfügung. EXE-, DMG- und PowerShell-
Skripte sowie CI-Workflows sind vorbereitet, auf diesen Systemen aber noch nicht
ausgeführt. Das Linux-App-Image ist keine native Windows- oder macOS-Abnahme.

Auf Windows x64, Intel-Mac und Apple Silicon die vollständige Pakettabelle mit
einem eigenen `--data-dir` wiederholen. Zusätzlich Installation, Update,
Deinstallation ohne Datenverlust, Skalierung, Tastenkürzel, native Dateiauswahl,
Nicht-ASCII-Zielnamen, große Fotos und gesperrte PDF-Ziele prüfen. Atomarer Ersatz
und Hardlink-Veröffentlichung auf den vorgesehenen Dateisystemen sind nativ zu
bestätigen. Es gibt keinen unsicheren Fallback für inkompatible Dateisysteme.

Signierung, Microsoft-Store-Registrierung, Mac-App-Sandbox, Entitlements,
benutzergewählter PDF-Schreibzugriff und Einreichungspakete bleiben einem
späteren Releaseauftrag vorbehalten. Siehe [Paketierung](packaging.md).
Es wurde nichts gepusht, hochgeladen oder veröffentlicht.
