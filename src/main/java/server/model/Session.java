package server.model;

import java.nio.channels.SelectionKey;

/**
 * In-memory authentication state associated with one client connection.
 *
 * @param selectionKey connection identity
 * @param user authenticated vault account
 * @param loggedIn whether the session is authenticated
 */
public record Session(SelectionKey selectionKey, User user, boolean loggedIn) {
}
