package de.appfabrik.kostuemplan.ui;

import de.appfabrik.kostuemplan.application.PdfFileName;
import de.appfabrik.kostuemplan.i18n.Messages;
import java.nio.file.Path;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/** Only this adapter knows native dialogs. Tests substitute choices without touching files. */
interface ExportDialogs {
  Path choose(Window owner, String suggestedName);

  boolean confirmOverwrite(Window owner, Path path);

  static ExportDialogs nativeDialogs(Messages messages) {
    return new ExportDialogs() {
      @Override
      public Path choose(Window owner, String suggestedName) {
        var chooser = new FileChooser();
        chooser.setTitle(messages.text("export.chooser"));
        chooser.setInitialFileName(suggestedName);
        chooser
            .getExtensionFilters()
            .add(new FileChooser.ExtensionFilter(messages.text("export.filter"), "*.pdf"));
        var selected = chooser.showSaveDialog(owner);
        return selected == null ? null : PdfFileName.withExtension(selected.toPath());
      }

      @Override
      public boolean confirmOverwrite(Window owner, Path path) {
        return UiSupport.confirm(
            owner,
            messages.text("export.overwriteTitle"),
            messages.text("export.overwriteBody", path.getFileName()),
            messages.text("export.overwrite"),
            messages.text("action.cancel"));
      }
    };
  }
}
