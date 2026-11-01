# Arbeitsschritte in Costume Planner einheitlich benennen

Auf Nutzerauftrag die bestehenden selbst angelegten Schritte beibehalten und
ihre Bezeichnung in allen sechs angebotenen Sprachen vereinheitlicht.
Hauptfenster, Editor und PDF verwenden Work steps, Arbeitsschritte,
Pasos de trabajo, Étapes de travail, Fasi di lavoro beziehungsweise
Etapas de trabalho. Zugehörige Leerzustände, Hinweise und betroffene
Import-/Exporttexte sind angepasst. Datenformat und Funktionsumfang bleiben gleich.

Maven verify mit `-Dmaven.test.skip=true` bestanden, einschließlich Compiler-Lint,
Enforcer, Spotless und SpotBugs. Sprachschlüssel und Platzhalter bleiben erhalten.
UI-/PDF-Überschriften stimmen für alle sechs Sprachen überein. Diff geprüft.
Gemäß app-lokaler Vorgabe keine Test- oder GUI-Suite ausgeführt.
Manuelle Sprach- und PDF-Prüfung in `docs/verification.md` beschrieben.

Linux-App-Image mit dem verifizierten JAR aktualisiert und SHA-256 verglichen.
Costume Planner ohne offenen Editor regulär geschlossen und mit derselben
Sammlung neu geöffnet. Fenster und laufender Prozess bestätigt. Der Nutzer
übernimmt den Funktionstest. Kein Push und keine Veröffentlichung.
