# Costume Planner: deutsche Mac-Store-Inhalte

Vorbereitet für **Costume Planner**, Store-Version **1.0**, Primärsprache Deutsch.
Vorlage ist die Inhaltslieferung von Sewing Planner. Die Lieferung enthält
fünf Bildentwürfe, direkt kopierbare Feldtexte, synthetische Beispieldaten,
zwei echte PDF-Exporte und die bestehende deutsche Datenschutzerklärung.
Die vorhandene App wurde verwendet. Kein App-Build und kein Maven-Aufruf.

Die für die neue Registrierung gewählte englische Bundle-ID lautet
`de.appfabrik.costumeplanner`. Der gezeigte App-Store-Connect-Eintrag heißt
Costume Planner. Seine hinterlegte Bundle-ID wurde nicht aus dem Konto ausgelesen.
`bundle-id.txt` dokumentiert die gewählte Kennung. Die vorhandene Paketkonfiguration
verwendet noch `de.appfabrik.kostuemplan`. Ihre Ausrichtung auf die registrierte
englische Kennung gehört zum späteren technischen Releaseauftrag.

## Die gezeigte Versionsseite ausfüllen

`Store/de-DE` enthält ausschließlich den kopierbaren Feldinhalt ohne Listenmarker.
Die Originale liegen unter `store-listings/mac-app-store/de-DE` im Repository.
Der öffentliche Name bleibt in jeder Sprache Costume Planner.

| Feld | Datei |
| --- | --- |
| Name unter App-Informationen | `name.txt` |
| Untertitel unter App-Informationen | `subtitle.txt` |
| Werbetext | `promotional-text.txt` |
| Beschreibung | `description.txt` |
| Schlüsselwörter | `keywords.txt` |
| Support-URL | `support-url.txt` |
| Marketing-URL, optional | `marketing-url.txt` |
| Copyright | `copyright.txt` |
| Version | `version.txt` |
| Anmerkungen zur App-Prüfung | `review-notes.txt` |
| Vorbereitete Erstversionsnotiz | `release-notes.txt` |

Die erste Version benötigt kein Feld „Neu in dieser Version“. Die vorbereitete
Erstversionsnotiz ist für die Übergabe vorhanden. `captions.txt` enthält fünf
redaktionelle Bildtexte. Sie sind keine separaten Pflichtfelder in App Store Connect.

„Anmeldung erforderlich“ ausschalten. Die App benötigt weder Konto noch
Demo-Zugang. Die erreichbaren Prüferkontaktdaten mit Vorname, Nachname,
Telefonnummer im internationalen Format und E-Mail ergänzen.
Game Center ausgeschaltet lassen. Ein Prüfungsanhang ist optional.

Die Texte beschreiben den tatsächlichen Funktionsumfang: Projekte mit Material,
Farbe, Aufbewahrungsort, Notiz, einem optionalen Foto, drei Statuswerten und eigenen
Arbeitsschritten. JSON sichert die ganze Sammlung. PDF exportiert ein ausgewähltes
gespeichertes Projekt oder alle gespeicherten Projekte. Beide Sammlungsformate
ignorieren einen aktiven Suchfilter. PDF ist ein Bericht, JSON dient dem erneuten Import.
Die Oberfläche startet auf Englisch. Die Store-Sprache ändert das nicht.

## Fünf Bildentwürfe wie bei Sewing Planner

Die Dateien unter `Screenshot-Drafts-Linux/de-DE` sind echte Aufnahmen der
vorhandenen JavaFX-App auf einem eigenen virtuellen Bildschirm. Sie haben
2560 × 1600 Pixel, RGB und keine Transparenz. Die Oberfläche wurde nicht
nachgebaut und keine macOS-Fensterdekoration hinzugefügt.

| Datei | Ansicht |
| --- | --- |
| `01-overview.png` | Fünf Kostümprojekte, drei Statuswerte, Foto und gespeicherte Arbeitsschritte |
| `02-edit-checklist.png` | Projekteditor mit offenen und erledigten Arbeitsschritten |
| `03-search.png` | Suche nach Verschluss mit einem passenden Projekt |
| `04-json-export.png` | Bestätigung des vollständigen JSON-Exports |
| `05-pdf-export.png` | Echtes PDF-Menü mit Einzelprojekt und gesamter Sammlung |

Die Aufnahmen stammen von Linux. Die eigene JavaFX-Oberfläche nutzt dieselben
Layout- und Schriftressourcen auf beiden Plattformen. Vor der Einreichung die
Bilder mit der tatsächlich laufenden Mac-App vergleichen. Abweichende Ansichten,
insbesondere native Dialoge oder Fensterrahmen, auf dem Mac erneut aufnehmen.
Die Einstufung als Entwurf ist kein Nachweis eines Darstellungsfehlers. Sie hält
fest, dass dieser Vergleich und die native Mac-Funktionsprüfung noch offen sind.
`Screenshots-Final-Mac/de-DE` bleibt bis zum Vergleich leer.

Die Beispieldatei unter `Demo` lässt sich auf einem separaten Mac-Testbenutzer
importieren. Ansichten und Suchbegriff sind dort beschrieben. Ein Vorschauvideo
gehört nicht zur Lieferung.

## PDF-Beispiele und Datenschutz

`PDF-Examples/Waldelfen-Umhang.pdf` zeigt das ausgewählte Projekt auf einer Seite.
`PDF-Examples/Kostuemprojekte.pdf` enthält die gesamte Sammlung auf fünf Seiten.
Beide Dateien wurden über die vorhandene App exportiert und erneut geöffnet.
Projekttexte, Farbwerte, Arbeitsschritte, Reihenfolge, Seiten und Fotos wurden
geprüft. Der Sammlungsbericht entstand bei aktiver Suche mit nur einem Treffer.
Die PDFs sind Beispiele und keine weiteren Store-Screenshots.

`Privacy/de-DE.txt` und `Privacy/index.html` enthalten vollständig die bestehende
deutsche In-App-Erklärung. Der Aufnahmehelfer exportiert dieselbe produktive
Quelle wie der Info-Dialog. Diese Lieferung veröffentlicht die Erklärung nicht.
Die öffentliche Datenschutz-URL bleibt offen. Beim späteren Mac-Release die
Bezugskanal-Hinweise auf Apple ausrichten. App und öffentliche Erklärung weiterhin
aus derselben Quelle erzeugen.

In App Store Connect außerdem Datenschutzangaben, Kategorie, Altersfreigabe,
Preis und Verfügbarkeit prüfen. Produktivität passt zum beschriebenen Zweck.
Die Veröffentlichungseinstellung legt der Nutzer im Portal fest. Die
Sandbox-Angaben müssen zum später tatsächlich signierten Mac-Paket passen.
Diese Inhaltslieferung belegt keine Sandbox-Abnahme.

## Ohne App-Build erneut erzeugen

Benötigt werden das bereits geprüfte `target/kostuemplan-0.1.0.jar` und dessen
unverändertes `target/classes`, die vorhandenen Abhängigkeits-JARs unter
`target/app-image/Costume Planner/lib/app`, JavaFX unter `target/javafx-modules`,
JDK 21, Python mit Pillow, Xvfb und xdotool.
Die ältere App-JAR im App-Image wird nicht verwendet oder ersetzt.

```bash
export JAVA_HOME="$HOME/.local/share/kostuemplan/jdk-21.0.13+9"
python3 scripts/mac_store_content.py check --check-links
python3 scripts/mac_store_content.py capture --output target/mac-store-content-drafts-neu
```

`COSTUMEPLANNER_XVFB` kann auf ein vorhandenes Xvfb zeigen. Der Java-Quelltextmodus
führt ausschließlich den separaten Aufnahmehelfer aus. Die App selbst und ihre
JARs werden weder kompiliert noch geändert. Fehlende oder veraltete Artefakte
führen zum Abbruch, niemals zu einem automatischen Build. Der Helfer nutzt eine
frische markierte Sammlung und einen eigenen Bildschirm. Die laufende App und
persönliche Exporte des Nutzers bleiben unberührt.

Nach visueller Kontrolle und lokalem Commit in einen neuen Ordner liefern:

```bash
python3 scripts/mac_store_content.py assemble --captures target/mac-store-content-drafts-neu --output build/releases/mac-store-content/Costume-Planner-1.0-de-DE-neu
```

Die Übergabe enthält weder Quellarchiv noch App-Paket und wird nicht gezippt.
Quellstand, Laufzeitdateien, Inhaltsprüfung und Aufnahmen sind durch Prüfsummen
zugeordnet. Synthetischer Import, wiederholter Import ohne Duplikate, JSON und
beide PDF-Exporte sind gezielte Inhaltsprüfungen. Eine vollständige Testsuite
und eine native Mac-Abnahme wurden für diese Inhaltsaufgabe nicht ausgeführt.

[Apple: Versionsfelder](https://developer.apple.com/help/app-store-connect/reference/app-information/platform-version-information/)
[Apple: Screenshot-Größen](https://developer.apple.com/help/app-store-connect/reference/app-information/screenshot-specifications/)
[Apple: Darstellung der App in Screenshots](https://developer.apple.com/help/app-review/guideline-reference/2-3-3-screenshots)
