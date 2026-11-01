package de.appfabrik.kostuemplan.pdf;

import de.appfabrik.kostuemplan.application.ExportException;
import de.appfabrik.kostuemplan.application.PdfWriter;
import de.appfabrik.kostuemplan.application.ProjectPdfSource;
import de.appfabrik.kostuemplan.i18n.AppLanguage;
import de.appfabrik.kostuemplan.i18n.Messages;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

/** One document with a fresh project page for every saved aggregate. */
public final class PdfBoxProjectWriter implements PdfWriter {
  private final Messages messages;

  public PdfBoxProjectWriter() {
    this(new Messages(AppLanguage.GERMAN));
  }

  public PdfBoxProjectWriter(Messages messages) {
    this.messages = messages;
  }

  @Override
  public void write(ProjectPdfSource saved, Path temporary) throws IOException {
    // Spool PDF streams to disk so collection photos do not all remain in heap memory.
    try (var document = new PDDocument(IOUtils.createTempFileOnlyStreamCache());
        var regularSource =
            getClass().getResourceAsStream("/de/appfabrik/kostuemplan/fonts/DejaVuSans.ttf");
        var boldSource =
            getClass().getResourceAsStream("/de/appfabrik/kostuemplan/fonts/DejaVuSans-Bold.ttf")) {
      if (regularSource == null || boldSource == null) {
        throw new IOException("Bundled PDF font missing");
      }
      var regular = PDType0Font.load(document, regularSource, true);
      var bold = PDType0Font.load(document, boldSource, true);
      document.getDocumentInformation().setCreator("Costume Planner");
      try (var layout = new ProjectPdfLayout(document, regular, bold, messages)) {
        saved.read(layout::project);
        if (layout.projectCount() == 0) {
          throw new ExportException(ExportException.Code.EMPTY_COLLECTION);
        }
        document
            .getDocumentInformation()
            .setTitle(
                layout.projectCount() == 1 ? layout.firstTitle() : messages.text("pdf.collection"));
        layout.finish();
      }
      document.save(temporary.toFile());
    }
  }
}
