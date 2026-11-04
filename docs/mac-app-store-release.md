# Costume Planner im Mac App Store

Version **1.0**, Build **1**, Bundle-ID **de.appfabrik.costumeplanner**.
Store- und App-Name lauten **Costume Planner**.
Der app-eigene Releaseweg verwendet Universal-Launcher und getrennte ARM64-/Intel-
Laufzeiten. Nutzer benötigen kein global installiertes Java. Der Quellstand,
die Abhängigkeiten und deren Prüfsummen sind an die vorbereiteten Eingaben gebunden.

## Bauen und signieren

Voraussetzungen: macOS mit Xcode, Python 3.12 und Eclipse Temurin 21.0.13+9.
`JAVA_HOME` muss auf dessen `Contents/Home` zeigen. Zuerst Formatter, Compiler,
Enforcer, SpotBugs und die zur Änderung passenden Prüfungen ausführen:

```bash
./mvnw --batch-mode --no-transfer-progress spotless:apply verify
python3 -B scripts/test_macos_release.py
python3 -B scripts/macos_release.py prepare --output build/mac-inputs-neu
python3 -B scripts/macos_release.py build --inputs build/mac-inputs-neu --output build/mac-build-neu
```

Vorhandene Eingabe-, App- und Paketordner werden nicht überschrieben. Die normalen
App-Image-/DMG-Helfer bleiben ein separater Weg. Für den Store jeweils eine eigene
Kopie der gebauten App für Entwicklung und Distribution verwenden.

`packaging/macos/signing.example.json` lokal als `development.local.json` und
`store.local.json` ausfüllen. Profil, Team und Bundle-ID müssen übereinstimmen.
Entwicklung: Apple Development mit Mac-Entwicklungsprofil und registriertem Mac.
Store: Apple Distribution mit Mac-App-Store-Profil sowie Mac-Installer-Zertifikat.
Keine Developer-ID-Signatur für diesen Weg. Private Dateien nie versionieren.

```bash
python3 -B scripts/macos_release.py sign --app "build/mac-development-neu/Costume Planner.app" --config packaging/macos/development.local.json --mode development
open "build/mac-development-neu/Costume Planner.app"
python3 -B scripts/macos_release.py sign --app "build/mac-store-neu/Costume Planner.app" --config packaging/macos/store.local.json --mode store
python3 -B scripts/macos_release.py inspect --app "build/mac-store-neu/Costume Planner.app" --signed
```

Der Helfer prüft beide Architekturen, gültige Bundle-IDs einschließlich `x86-64`
für die Intel-Runtime, Leserechte und Signaturen. Das eingebettete Profil erhält
`0644`; das private Original darf `0600` behalten. Keine zusätzlichen Netzwerk-
oder pauschalen Dateisystemrechte, keine deaktivierte Library Validation.

## Native Funktionsabnahme

Mit erfundenen Daten testen, vorzugsweise in einem separaten macOS-Testkonto.
Eine bestehende Sammlung nicht löschen oder mit Demo-Daten ersetzen.

- Projekte anlegen, bearbeiten, suchen und löschen. Nach Neuanlage ist die Suche
  leer und der neue Eintrag ausgewählt; Abbrechen erhält vorhandene Daten.
- Alle drei Statuswerte, Farbwähler und ein ausgewähltes JPEG-/PNG-Foto prüfen.
  Speichern muss Texte, Status und Fotokopie gemeinsam erhalten.
- Checklistenschritte hinzufügen, umbenennen, abhaken und entfernen. Speichern
  übernimmt die Checkliste gemeinsam mit dem Projekt; Abbrechen erhält den
  bisherigen Stand. Der Projektstatus bleibt von den Häkchen unabhängig.
- Neu starten: Sammlung, Sprache und Fotos bleiben erhalten. Die Fotoauswahl
  schlägt den zuletzt verwendeten Ordner vor; Abbruch und nicht mehr verfügbare
  Ordner bleiben bedienbar. Originalfotos werden nicht verändert.
- JSON exportieren und wieder importieren: Status, Farbe, Fotos und Schritte erhalten;
  bestehende Einträge bleiben, identische Kopien werden übersprungen.
  Abbruch und ungültige Eingaben dürfen keine Teiländerungen hinterlassen.
- Einzelprojekt und gesamte Sammlung als PDF exportieren und in einem unabhängigen
  Viewer öffnen. Texte, Foto, Farbe, Status und Checklisten müssen erhalten bleiben;
  jedes Projekt beginnt im Sammlungsbericht auf einer neuen Seite. Eine Suche darf
  die Sammlung nicht begrenzen. Zielordner wählen, Abbrechen und bestätigtes
  Überschreiben prüfen; bestehende Dateien dürfen bei Abbruch unverändert bleiben.
- Kleine Fenster, Info und Sprache prüfen. Offline-Nutzung benötigt die
  eingebettete Laufzeit; der ausdrücklich gewählte Datenschutz-Link öffnet den Browser.
- ARM64 und Intel-Ausführung prüfen. Auf Apple Silicon ist `open -n --arch x86_64`
  mit Rosetta möglich; das ersetzt keinen Nachweis auf echter Intel-Hardware.

Der Store-Launcher ermittelt seinen Container über Foundation. Die Sammlung
liegt darin unter `Library/Application Support/KostuemPlan/kostuemplan.db`.
Normale Installationen behalten ihre bisherigen Datenpfade. `--data-dir` ist im
Store-Build gesperrt. Ein bewusster Datentransfer erfolgt über den app-eigenen
JSON-Export/-Import; keine automatische Migration oder Zusammenführung.

## Paket und Einreichung

Die tatsächlich bestandenen Abläufe in einer privaten Kopie von
`acceptance.example.json` dokumentieren. Quellendigest, Architektur, verwendete
Hardware und Abnahmeumfang ehrlich erfassen. Nur durchgeführte Prüfungen abhaken.

```bash
python3 -B scripts/macos_release.py package --app "build/mac-store-neu/Costume Planner.app" --config packaging/macos/store.local.json --acceptance packaging/macos/acceptance.local.json --output "build/mac-store-neu/Costume-Planner-1.0-build-1-universal.pkg"
```

Anschließend mit Transporter/altool validieren und gemäß Nutzerauftrag hochladen.
Nach erfolgreicher Verarbeitung Build 1 auswählen und zur Prüfung einreichen.
Kostenlos in allen Ländern/Regionen, automatische Veröffentlichung nach Freigabe.
Store-Texte in sechs Sprachen und bestehende Screenshots getrennt vom Binärpaket
prüfen. Die App-Privacy-Antworten müssen in App Store Connect veröffentlicht sein.
Verschlüsselungsangaben anhand der gebauten App und ihrer Laufzeit beurteilen.
Händlerstatus und persönliche Vertragszustimmungen verbleiben beim Kontoinhaber.

Native Build-/Signaturprotokolle unter `build/` sind lokale Nachweise. Das
übergeordnete Changelog dokumentiert den tatsächlichen Veröffentlichungsstand.
