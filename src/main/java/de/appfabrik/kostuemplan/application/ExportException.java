package de.appfabrik.kostuemplan.application;

/** Stable export failure codes. The UI chooses the process language. */
public final class ExportException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public enum Code {
    OVERWRITE_REQUIRED,
    FOLDER_REQUIRED,
    TARGET_CHANGED,
    WRITE_FAILED,
    FILE_REQUIRED,
    REGULAR_FILE_REQUIRED,
    UNSUPPORTED_GLYPH
  }

  private final Code code;
  private final String detail;

  public ExportException(Code code) {
    this(code, "", null);
  }

  public ExportException(Code code, Throwable cause) {
    this(code, "", cause);
  }

  public ExportException(Code code, String detail, Throwable cause) {
    super(code.name() + " " + detail, cause);
    this.code = code;
    this.detail = detail;
  }

  public Code code() {
    return code;
  }

  public String detail() {
    return detail;
  }
}
