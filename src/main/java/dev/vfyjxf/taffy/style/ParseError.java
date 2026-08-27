package dev.vfyjxf.taffy.style;

/** Indicates that a string cannot be parsed as a supported Taffy CSS value. */
public class ParseError extends IllegalArgumentException {
    public ParseError(String message) {
        super(message);
    }

    public ParseError(String message, Throwable cause) {
        super(message, cause);
    }
}
