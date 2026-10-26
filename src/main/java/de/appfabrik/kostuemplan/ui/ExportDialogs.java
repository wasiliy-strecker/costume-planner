package de.appfabrik.kostuemplan.ui;

import de.appfabrik.kostuemplan.application.PdfFileName;
import java.nio.file.Path;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/** Only this adapter knows native dialogs. Tests substitute choices without touching files. */
interface ExportDialogs {
  Path choose(Window owner, String suggestedName);

  boolean confirmOverwrite(Window owner, Path path);

  static ExportDialogs nativeDialogs() {
    return new ExportDialogs() {
      @Override
      public Path choose(Window owner, String suggestedName) {
        var chooser = new FileChooser();
        chooser.setTitle("Projekt als PDF exportieren");
        chooser.setInitialFileName(suggestedName);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF-Dokument", "*.pdf"));
        var selected = chooser.showSaveDialog(owner);
        return selected == null ? null : PdfFileName.withExtension(selected.toPath());
      }

      @Override
      public boolean confirmOverwrite(Window owner, Path path) {
        return UiSupport.confirm(
            owner,
            "PDF überschreiben?",
            "„"
                + path.getFileName()
                + "“ ist bereits vorhanden. Diese Datei durch die neue PDF ersetzen?",
            "Überschreiben",
            "Abbrechen");
      }
    };
  }
}
