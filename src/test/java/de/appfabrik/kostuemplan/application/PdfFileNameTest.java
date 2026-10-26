package de.appfabrik.kostuemplan.application;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PdfFileNameTest {
  @ParameterizedTest
  @CsvSource({
    "CON,Projekt_CON.pdf",
    "lpt1,Projekt_lpt1.pdf",
    "COM¹,Projekt_COM¹.pdf",
    "NUL.txt,Projekt_NUL.txt.pdf",
    "' . ',Kostümprojekt.pdf",
    "Umhang.PDF,Umhang.pdf",
    "Frühling,Frühling.pdf",
    "a/b:c*,a_b_c_.pdf"
  })
  void suggestsPortableNames(String input, String expected) {
    assertEquals(expected, PdfFileName.suggest(input));
  }

  @Test
  void limitsUnicodeByteLengthAndAlwaysAppendsExtension() {
    String name = PdfFileName.suggest("Ä🦋".repeat(200));
    assertTrue(name.getBytes(StandardCharsets.UTF_8).length <= 164);
    assertFalse(name.contains("\ufffd"));
    assertEquals("Grüße.pdf", PdfFileName.suggest("Gru\u0308ße"));
    assertEquals(Path.of("file.txt.pdf"), PdfFileName.withExtension(Path.of("file.txt")));
    assertEquals(Path.of("file.PDF"), PdfFileName.withExtension(Path.of("file.PDF")));
  }
}
