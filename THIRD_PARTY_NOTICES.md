# Verwendete Komponenten

KostümPlan verwendet OpenJFX unter GPLv2 mit Classpath Exception und SQLite
JDBC von Xerial unter Apache License 2.0 und BSD-2-Clause. SQLite selbst ist
Public Domain. Die eingebettete Java-Laufzeit unterliegt den Lizenzbedingungen
der beim Bauen gewählten JDK-Distribution. Die aktuelle Build-Basis ist
Eclipse Temurin 21.0.13+9 unter GPLv2 mit Classpath Exception. Maven prüft
Hersteller, Version und Buildnummer. Das App-Image enthält zusätzlich die
unveränderte JDK-Datei `release` als `legal/BUILD_JAVA_RELEASE.txt`, sodass
Herkunft, Architektur und vollständige Laufzeitversion nachvollziehbar bleiben.

Die vollständigen Hinweise der Laufzeit bleiben im Verzeichnis `legal` des
Runtime-Images erhalten. SQLite JDBC bleibt als unverändertes Archiv mit den
mitgelieferten Lizenzdateien enthalten. Die OpenJFX-Lizenztexte werden aus
`third-party-licenses` in das Paket übernommen. Diese Datei ergänzt diese Hinweise.

## Versionsbezogene Herkunft und Quellcode

| Komponente | Version | Herkunft und zugehöriger Quellcode |
| --- | --- | --- |
| Eclipse Temurin | 21.0.13+9 | [Binärrelease und vollständiges Quellarchiv](https://github.com/adoptium/temurin21-binaries/releases/tag/jdk-21.0.13%2B9), Datei `OpenJDK21U-jdk-sources_21.0.13_9.tar.gz` |
| OpenJFX | 21.0.11+4 | [OpenJFX-Quellen](https://github.com/openjdk/jfx21u/tree/21.0.11%2B4), Module `javafx.base`, `javafx.graphics`, `javafx.controls` |
| Xerial SQLite JDBC | 3.53.4.0 | [Unverändertes JDBC-Archiv und Quellen](https://github.com/xerial/sqlite-jdbc/tree/3.53.4.0), Lizenzen unter `META-INF/maven/org.xerial/sqlite-jdbc` im JAR |

Das JavaFX-Artefakt meldet die Laufzeitversion `21.0.11+4`. Die mitgelieferten
Linux-Nativhinweise decken JPEG und Mesa ab. Die JDK-Lizenzhinweise bleiben
im Runtime-Image erhalten. SQLite selbst ist [Public Domain](https://sqlite.org/copyright.html).
Die Anwendung verändert die Fremdkomponenten nicht.

Die [GPLv2 mit Classpath Exception](https://openjdk.org/legal/gplv2+ce.html)
enthält weiterhin Pflichten für die weitergegebenen Laufzeitkomponenten.
Vor einer öffentlichen Binärlieferung müssen die genau zu den ausgelieferten
Temurin- und OpenJFX-Binärdateien gehörenden vollständigen Quellarchive samt
Buildinformationen und Lizenzhinweisen als Release-Begleitmaterial verfügbar
gemacht werden. Ein bloßer allgemeiner Projektlink ersetzt diese Aufgabe nicht.
Für die spätere Store-Lieferung ist die Bereitstellung aus dem Store-Eintrag
zugänglich zu machen. Für GitHub gehören die Begleitarchive zum zugehörigen
Release. Diese lokale Vorbereitung veröffentlicht weder Binärdateien noch
Quellarchive.

Die zusätzlichen nativen Komponenten des endgültigen Windows-Pakets sind
erst am tatsächlich gebauten Windows-Artefakt abschließend zu inventarisieren.
Die Linux-Prüfung ist keine Lizenz- oder Signaturabnahme eines Windows-Pakets.

Maven, Spotless, SpotBugs und JUnit sind Entwicklungswerkzeuge und werden nicht
in das App-Image aufgenommen. Dieses Dokument vergibt keine Lizenz für den
eigenen KostümPlan-Quellcode.

## PDFBox und Schrift, geprüft am 26. Oktober 2026

Apache PDFBox 3.0.8 mit FontBox 3.0.8, pdfbox-io 3.0.8 und Apache Commons Logging
1.4.0 wird unter Apache License 2.0 verwendet. Die offizielle Downloadseite nennt
3.0.8 als aktuelle 3.x-Version. PDFBox setzt Java 8 oder neuer voraus.
Die konkrete Kombination mit Java 21 wird in dieser App getestet und paketiert.
Die unveränderten Maven-Archive und ihre Lizenz-/NOTICE-Dateien bleiben enthalten.
Zusätzliche Kopien liegen unter `third-party-licenses` und im Paket unter `legal`.
Optionale PDFBox-Erweiterungen für Verschlüsselung, JBIG2, JPEG2000 oder TIFF
werden für diesen Export nicht benötigt und nicht hinzugefügt.

DejaVu Sans 2.37 wird unverändert als `DejaVuSans.ttf` app-lokal mitgeliefert.
Die Bitstream-Vera-Lizenz erlaubt die Einbettung und Weitergabe zusammen mit
dieser Anwendung unter Beibehaltung der Hinweise. DejaVu-Änderungen sind Public
Domain. Die vollständigen Hinweise einschließlich Arev-Anteilen liegen in
`third-party-licenses/dejavu-LICENSE.txt` und werden mit dem Paket ausgeliefert.
Die Schrift wird nicht separat verkauft und nicht unter einem fremden Namen verändert.

Quelle der TTF ist das offizielle Releasearchiv `dejavu-sans-ttf-2.37.zip`.
Die SHA-256-Prüfsumme wurde gegen die offizielle Downloadseite abgeglichen:

```text
5c6e497a2f36552cb5ffb112c413a6af39c0f3c47653662b90b4fa6499822fd7
```

Offizielle Quellen:

[PDFBox-Download und Version](https://pdfbox.apache.org/download.html)

[PDFBox-Abhängigkeiten](https://pdfbox.apache.org/3.0/dependencies.html)

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

[DejaVu Download und Prüfsummen](https://dejavu-fonts.github.io/Download.html)

[DejaVu-Lizenz](https://dejavu-fonts.github.io/License.html)

[Offizielles DejaVu-Release](https://github.com/dejavu-fonts/dejavu-fonts/releases/tag/version_2_37)
