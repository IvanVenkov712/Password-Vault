package server.command;

/**
 * Result passed from a command to the server; only the message is sent to the client.
 *
 * @param status whether execution succeeded
 * @param message response text, which may contain credentials or multiple lines
 */
public record CommandResult(Status status, String message) {
}
