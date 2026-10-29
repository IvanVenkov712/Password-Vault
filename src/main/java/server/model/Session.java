package server.model;

import java.nio.channels.SelectionKey;

public record Session(SelectionKey selectionKey, User user, boolean loggedIn) {
}
