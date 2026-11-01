package de.appfabrik.kostuemplan.ui;

import de.appfabrik.kostuemplan.i18n.Messages;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javafx.stage.Window;

/** Only this adapter knows native dialogs. Tests substitute choices without touching files. */
interface ExportDialogs {
  Path choose(Window owner, String suggestedName);

  boolean confirmOverwrite(Window owner, Path path);

  static ExportDialogs nativeDialogs(Messages messages) {
    return new ExportDialogs() {
      @Override
      public Path choose(Window owner, String suggestedName) {
        String timestamp =
            LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("uuuu-MM-dd_HH-mm-ss", Locale.ROOT));
        String stem = suggestedName.substring(0, suggestedName.length() - ".pdf".length());
        return ExportFileDialog.choose(
            owner,
            messages,
            messages.text("export.chooser"),
            stem + "-" + timestamp + ".pdf",
            "pdf",
            messages.text("export.filter"));
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
