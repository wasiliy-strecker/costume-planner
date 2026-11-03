package de.appfabrik.kostuemplan.ui;

import java.io.IOException;
import java.net.URI;

/** One app-specific public policy, independent of the interface language. */
final class PrivacyPolicy {
  static final URI URL =
      URI.create("https://github.com/wasiliy-strecker/costume-planner/blob/main/PRIVACY.md");

  private PrivacyPolicy() {}

  static boolean open(BrowserOpener browser) {
    try {
      browser.open(URL);
      return true;
    } catch (IOException | RuntimeException exception) {
      return false;
    }
  }
}
