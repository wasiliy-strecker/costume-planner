# Projektdialog um jeweils 25 Prozent vergrößern

Auf Nutzerauftrag den Dialog zum Hinzufügen und Bearbeiten in Höhe und Breite
um jeweils 25 % vergrößert. Neue Startgröße 825 × 675 statt effektiv 660 × 540
Pixel. Nach dem Layout setzen und an den verfügbaren Bildschirmarbeitsbereich
anpassen. Bestehende Mindestgröße und Scrollfunktion bleiben erhalten.
Die gemeinsame Oberfläche gilt für alle sechs angebotenen Sprachen.

Formatter und Maven verify mit `-Dmaven.test.skip=true` bestanden, einschließlich
Compiler-Lint, Enforcer und SpotBugs. Diff geprüft. Keine automatisierten Test-
oder GUI-Suiten gemäß app-lokaler Vorgabe. Manuelle Abnahme dokumentiert.

Offenen Dialog über den Fenstermanager auf 825 × 675 Pixel vergrößert und Maße
bestätigt. Eingaben weder geschlossen noch gespeichert oder verworfen.
Neues JAR atomar ins Linux-App-Image übernommen und SHA-256 verglichen.
Neue Standardgröße ist für den nächsten App-Start hinterlegt. Laufende App
zum Schutz des Entwurfs offen gelassen. Kein Push oder Veröffentlichung.
