# Paketierung und spätere Stores

## Voraussetzungen

Sichtbarer Produktname: KostümPlan. Die Launcher- und Paketnamen verwenden
`KostuemPlan` beziehungsweise `kostuemplan`. App-Kennung: `de.appfabrik.kostuemplan`.

Zuerst `./mvnw verify` beziehungsweise `.\mvnw.cmd verify` erfolgreich ausführen.
Paketierung kompiliert den aktuellen Stand, wiederholt jedoch nicht die Tests.
Ein vollständiges Eclipse Temurin JDK 21.0.13+9 mit `javac`, `jlink` und `jpackage` ist erforderlich.
JavaFX-Bibliotheken und JDK müssen zur Zielarchitektur passen.

Das App-Image enthält die App, SQLite JDBC, PDFBox, FontBox, pdfbox-io,
Commons Logging, die app-eigene Schriftressource und eine erzeugte Java-Laufzeit.
JavaFX wird als Modul in die Laufzeit eingebunden. Die eigene App bleibt ein
gewöhnliches Java-Projekt ohne `module-info.java`. Der Anwender benötigt kein
separates Java. Die vollständigen Runtime-Verzeichnisse müssen zusammenbleiben. `java.desktop`
ist über `java.se` enthalten und stellt die ImageIO-Decoder bereit.

## Lokales Linux-App-Image

```bash
./scripts/package.sh app-image
./target/app-image/KostuemPlan/bin/KostuemPlan
```

Linux dient hier als Entwicklungs- und Prüfplattform. Ein App-Image ist ein
startbares Verzeichnis mit Laufzeit. Ein Linux-Store-Release ist nicht geplant.

## Windows x64

Auf Windows mit einem x64-Temurin-JDK 21.0.13+9 und PowerShell ausführen. Für die EXE-Erzeugung
wird WiX Toolset 3.14.1 benötigt, dessen `bin`-Verzeichnis im `PATH` liegt.

```powershell
.\mvnw.cmd verify
.\scripts\package.ps1 -PackageType app-image
.\target\app-image\KostuemPlan\KostuemPlan.exe
.\scripts\package.ps1 -PackageType exe
```

Die EXE liegt unter `target/dist/kostuemplan-0.1.0-windows-x64.exe`.
Die Installation ist pro Benutzer vorgesehen. Die eigene Upgrade-ID `ef194ed0-3f58-4af3-a8d9-ecece05614c9` ermöglicht
spätere Installer-Updates mit derselben Produktidentität. Die Sammlung liegt
außerhalb des Installationsverzeichnisses.

## macOS Intel und Apple Silicon

Auf der jeweiligen Mac-Architektur mit passendem Temurin-JDK 21.0.13+9 ausführen:

```bash
./mvnw verify
./scripts/package.sh app-image
open target/app-image/KostuemPlan.app
./scripts/package.sh dmg
```

Das Ergebnis heißt `target/dist/kostuemplan-0.1.0-macos-x64.dmg` oder
`target/dist/kostuemplan-0.1.0-macos-arm64.dmg`.
Die Bundle-Kennung ist `de.appfabrik.kostuemplan`.
Die aktuellen Pakete haben keine Distributionssignatur und sind als lokale
Testpakete vorbereitet. Plattform-Sicherheitsdialoge sind bei der späteren
nativen Abnahme zu berücksichtigen.

## Nachweis und CI

Die Skripte starten den erzeugten Launcher mit `--version`. Unter Windows
wird dessen Exitcode geprüft, weil ein GUI-Launcher keine verlässliche
Konsolenausgabe liefert. Danach muss auf jeder Plattform der vollständige
sichtbare Ablauf aus `verification.md` folgen, einschließlich Datenbankzugriff, Fotoimport, Bildanzeige, Statuswechsel, Checklistenänderungen
und erneutem Start. Zusätzlich den PDF-Export aus genau diesem Paket mit Foto,
Umlauten, mehrseitiger Notiz, offenen und erledigten Schritten durchführen. Die
PDF in einem unabhängigen Viewer öffnen. Das belegt PDFBox und Schriftressourcen
in der eingebetteten Laufzeit. Eine Launcher-Prüfung allein belegt keine funktionierende GUI.

Vorhandene versionierte EXE- und DMG-Dateien werden nicht still überschrieben.
Die generierten Zwischenverzeichnisse unter `target` werden neu aufgebaut.

Der GitHub-Workflow `Verify` prüft Java, SQLite, Bildimport, Status, Checklisten und PDF-Inhalte auf Linux, Windows und macOS.
`Build native test packages` wird manuell gestartet und erzeugt Windows-x64-,
macOS-x64- und macOS-arm64-Testartefakte. Er erstellt keine GitHub-Releases
und lädt nichts in einen Store. Auf Linux führt `Verify` auch die JavaFX-Dialogtests unter Xvfb aus.
Die Workflows sind vorbereitet, aber noch
nicht auf GitHub ausgeführt. Eine Remote-Adresse ist noch nicht gesetzt.

## Offene Store-Schritte

Windows und macOS sind lokal noch nicht nativ geprüft. Vor einer Veröffentlichung
sind Installation, Start, Schrift, Tastenkürzel, Foto-, Status- und Checklistenspeicherung, PDF-Export und Updates auf
Zielsystemen abzunehmen. Eine EXE oder DMG allein belegt keine Store-Eignung.

Für den Microsoft Store ist MSIX vorgesehen, für den kostenlosen GitHub-Download
bleibt die EXE aus demselben Anwendungscode vorgesehen. Die MSIX-Paketierung
ist noch nicht implementiert. Reale Partner-Center-Identität, Publisher,
Signierungsnachweise, Windows-Installation, Updates, WACK, finale Windows-Bilder
und Store-Angaben bleiben offen. Es erfolgt keine automatische Migration
zwischen den beiden Installationswegen. MSIX-Datenpfade und Deinstallation
müssen mit synthetischen Daten separat geprüft werden.

Für den Mac App Store bleiben Apple-Entwickleridentität, App-Sandbox,
Entitlements für Java und native Bibliotheken, Signierung des vollständigen
Bundles und ein geeignetes Einreichungspaket offen. Insbesondere sind der
containerbezogene Datenpfad, die Dateiauswahl mit kurzzeitigem Lesezugriff
auf externe Fotos, der Schreibzugriff auf das gewählte PDF-Ziel, atomarer
Dateiersatz und Hardlink-Veröffentlichung im freigegebenen Zielordner sowie
das Laden der nativen SQLite-Bibliothek in der Sandbox auf einem Mac zu prüfen. Eine notarialisierte Direktdistribution ist
ein eigener Weg und ersetzt keine Mac-App-Store-Abnahme.

Quellen für die später erneut zu prüfenden Vorgaben:

- [JDK 21 Packaging Overview](https://docs.oracle.com/en/java/javase/21/jpackage/packaging-overview.html)
- [Apple: Distribution und App Sandbox](https://help.apple.com/xcode/mac/current/en.lproj/dev91fe7130a.html)
- [Microsoft: Store-Überblick](https://learn.microsoft.com/en-us/windows/apps/publish/)
- [GitHub: Verfügbare Runner](https://docs.github.com/en/actions/how-tos/write-workflows/choose-where-workflows-run/choose-the-runner-for-a-job)

## Internationale Vorschau

Die Laufzeit enthält alle sechs UTF-8-Kataloge, `jdk.localedata`, PDFBox,
DejaVu samt bisherigen Lizenzen und eigenes PNG/ICO. Paketname `KostuemPlan`
und sichtbarer Name `KostümPlan` bleiben erhalten. Die isolierte Paketprüfung
startet ohne Entwicklungs-JDK und prüft auch einen echten PDF-Export.
Siehe [Internationalisierung](internationalization.md).

## Lokale Vorbereitung vom 31. Oktober 2026

Maven und beide CI-Workflows verwenden Temurin 21.0.13+9. Die global vorhandene
Java-Installation bleibt bestehen. App-Images enthalten zusätzlich
`legal/USER_GUIDE.md` mit Datenpfaden und `legal/BUILD_JAVA_RELEASE.txt` mit der
originalen Herkunftsdatei des Build-JDK. Der bestehende explizite Windows-ICO-Pfad
bleibt erhalten. Ein vorbereitetes Skript ist kein Windows-Ausführungsnachweis.
Die geänderten CI-Workflows wurden nicht gestartet.

Vor einer öffentlichen Binärlieferung die genauen vollständigen Laufzeitquellen
und Begleitinformationen gemäß `THIRD_PARTY_NOTICES.md` zuordnen und zugänglich
bereitstellen. Diese Vorbereitung veröffentlicht weder Pakete noch Quellen.

Der Offline-Dialog enthält die Datenschutzerklärung in sechs Sprachen. Der Helfer
`scripts/export_privacy.sh` erzeugt die gleichen Texte für die Store-Einreichung.
Die MSIX-Anleitung erlaubt eine URL oder den direkten Datenschutztext.
Bei der tatsächlichen Einreichung die angebotenen Felder prüfen. Die Angaben
zur Verarbeitung müssen auch die lokal ausgewählten Fotos berücksichtigen.

Quellen, erneut gelesen am 31. Oktober 2026:

- [Microsoft: Distributionswege, Signierung und Updates](https://learn.microsoft.com/en-us/windows/apps/package-and-deploy/choose-distribution-path)
- [Microsoft: Datenschutztext und Supportangaben](https://learn.microsoft.com/en-us/windows/apps/publish/publish-your-app/msix/support-info)

Nächste Schritte: aktuelle Windows-Builds und echte Store-Identität vorbereiten,
MSIX erstellen, Installation und Update mit Datenbestand prüfen, danach WACK,
Windows-Aufnahmen, Lizenzbegleitmaterial und Store-Metadaten abschließen.

## Mitgelieferte UI-Schrift, 31. Oktober 2026

Das App-JAR enthält Noto Sans CJK JP 2.004 als Regular und Bold einschließlich
OFL-Lizenz. Die Oberfläche lädt diese Dateien vor der ersten Anwendung des
Stylesheets. Die beiden Schriften belegen zusammen rund 32 MiB unkomprimiert.
Es gibt keinen Schrift-Download zur Laufzeit und keine Systeminstallation.
Herkunft und Prüfsummen stehen in `third-party-licenses/noto-sans-cjk/NOTICE.md`.
Die Pakethelfer kopieren diesen Ordner in das Legal-Verzeichnis.
Native Darstellung unter Windows und macOS ist weiterhin separat abzunehmen.
