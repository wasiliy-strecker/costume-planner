package de.appfabrik.kostuemplan.ui;

import de.appfabrik.kostuemplan.i18n.Messages;
import de.appfabrik.kostuemplan.i18n.PrivacyNotice;
import javafx.stage.Window;

final class PrivacyDialog {
  private final InfoDialog dialog;

  PrivacyDialog(Window owner, Messages messages) {
    dialog =
        new InfoDialog(
            owner,
            messages,
            messages.text("privacy.title"),
            PrivacyNotice.text(messages),
            "privacy");
  }

  void show() {
    dialog.show();
  }
}
