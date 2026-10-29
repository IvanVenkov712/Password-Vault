package server.command;

/** Outcome of command execution within the server. */
public enum Status {
    /** The requested operation completed. */
    OK,
    /** The request failed validation or execution. */
    ERROR
}
