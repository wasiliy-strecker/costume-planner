# Aktualisierte Dialoggröße im laufenden Test aktivieren

Der Nutzer meldete beim erneuten Öffnen wieder die alte Größe. Ursache war der
noch seit 08:03 Uhr laufende App-Prozess mit alten Klassen. Das bereits gebaute
und aktualisierte Paket enthält die korrekte Änderung aus `bab7982`.

Editor und App regulär geschlossen. Keine Verwerfrückfrage und keine Eingaben
gespeichert oder verworfen. JAR-Hash geprüft und App-Image neu gestartet.
Den leeren Anlegedialog zweimal geöffnet und geschlossen. Beide Male bestätigt
der Fenstermanager 825 × 675 Pixel einschließlich Titelleiste ohne Größenkorrektur.
Kein Projekt angelegt. App bleibt zum Testen geöffnet.

Prüfbericht ergänzt, Diff und Whitespace geprüft. Nur Dokumentationsänderungen,
keine erneute Kompilierung oder Test-Suite. Kein Push und keine Veröffentlichung.
