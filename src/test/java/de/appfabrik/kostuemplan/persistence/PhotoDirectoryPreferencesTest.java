package de.appfabrik.kostuemplan.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PhotoDirectoryPreferencesTest {
  @TempDir Path directory;

  @Test
  void remembersAConfirmedSelectionAcrossInstancesWithoutChangingItsFile() throws Exception {
    Path folder = Files.createDirectory(directory.resolve("Fotos Ö"));
    Path photo = folder.resolve("photo.jpg");
    Files.writeString(photo, "synthetic selection");
    Path settings = directory.resolve("settings");
    var preferences = new PhotoDirectoryPreferences(settings);
    assertTrue(preferences.remember(photo));
    assertEquals(folder, preferences.initialDirectory());
    assertEquals(folder, new PhotoDirectoryPreferences(settings).initialDirectory());
    assertEquals("synthetic selection", Files.readString(photo));
    Files.delete(photo);
    Files.delete(folder);
    assertNotEquals(folder, new PhotoDirectoryPreferences(settings).initialDirectory());
  }

  @Test
  void failedPreferenceWriteDoesNotPreventUsingTheSelectedDirectory() throws Exception {
    Path blocked = directory.resolve("settings");
    Files.writeString(blocked, "keep");
    var preferences = new PhotoDirectoryPreferences(blocked);
    assertFalse(preferences.remember(directory.resolve("photo.png")));
    assertEquals(directory, preferences.initialDirectory());
    assertEquals("keep", Files.readString(blocked));
  }

  @Test
  void nativePanelKeepsSavedNavigationHintEvenWhenFolderIsUnavailable() throws Exception {
    Path folder = Files.createDirectory(directory.resolve("Fotos Ö"));
    Path settings = directory.resolve("settings");
    var preferences = new PhotoDirectoryPreferences(settings);
    assertNull(preferences.lastDirectoryHint());
    assertTrue(preferences.remember(folder.resolve("synthetic.png")));
    Files.delete(folder);
    var restarted = new PhotoDirectoryPreferences(settings);
    assertEquals(folder, restarted.lastDirectoryHint());
    assertNotEquals(folder, restarted.initialDirectory());
  }
}
