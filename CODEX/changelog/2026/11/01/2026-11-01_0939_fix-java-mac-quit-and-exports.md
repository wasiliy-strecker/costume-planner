# Costume Planner: Mac-Beenden und Exportkorrekturen

Cmd+Q bündelt Schließanfragen und fragt genau einmal nach ungespeicherten
Änderungen. Verwerfen beendet die App, Weiterbearbeiten erhält den Entwurf.
Ein laufender Speichervorgang bleibt geschützt.

JSON- und PDF-Export verwendet auf dem Mac eine native Ordnerfreigabe und danach einen
validierten Dateinamen. Damit erhält auch die temporäre Exportdatei Zugriff.
Dateinamendialog vollständig in sechs Sprachen übersetzt.

Prüfung: 241 Tests einschließlich echter JavaFX-Bedienabläufe bestanden.
Keine übersprungenen Tests. Compiler-Lint, Enforcer, Spotless und SpotBugs bestehen.
Details und manuelle Mac-Abnahme in `docs/verification.md`.
Sicherungscommit vor Umsetzung: `7558fb3`. Persönliche Exporte nicht aufgenommen.
Kein Push, keine Store-Paketierung und kein Neustart persönlicher App-Instanzen.
