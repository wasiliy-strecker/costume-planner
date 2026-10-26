package de.appfabrik.kostuemplan.application;

/** A failure that can be explained without exposing project contents. */
public final class ExportException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public ExportException(String message) {
    super(message);
  }

  public ExportException(String message, Throwable cause) {
    super(message, cause);
  }
}
