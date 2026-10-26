package de.appfabrik.kostuemplan.application;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Locale;

public final class PdfFileName {
  private PdfFileName() {}

  public static String suggest(String projectName) {
    String value =
        Normalizer.normalize(projectName, Normalizer.Form.NFC)
            .replaceAll("[\\p{Cntrl}\\p{Cf}<>:\"/\\\\|?*]", "_")
            .strip();
    if (value.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
      value = value.substring(0, value.length() - 4);
    }
    // Bound both UTF-16 length (Windows) and UTF-8 bytes (macOS filesystems).
    var result = new StringBuilder();
    int bytes = 0;
    for (int point : value.codePoints().toArray()) {
      String character = new String(Character.toChars(point));
      bytes += character.getBytes(StandardCharsets.UTF_8).length;
      if (bytes > 160) {
        break;
      }
      result.append(character);
    }
    value = result.toString().replaceAll("^[. ]+|[. ]+$", "");
    if (value.isBlank()) {
      value = "Kostümprojekt";
    }
    if (value.matches("(?i)(CON|PRN|AUX|NUL|COM[1-9¹²³]|LPT[1-9¹²³])(?:\\..*)?")) {
      value = "Projekt_" + value;
    }
    return value + ".pdf";
  }

  public static Path withExtension(Path selected) {
    Path fileName = selected.getFileName();
    if (fileName == null) {
      throw new ExportException("Bitte wähle eine PDF-Datei als Ziel.");
    }
    return fileName.toString().toLowerCase(Locale.ROOT).endsWith(".pdf")
        ? selected
        : selected.resolveSibling(selected.getFileName() + ".pdf");
  }
}
