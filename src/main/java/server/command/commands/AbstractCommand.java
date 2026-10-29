package server.command.commands;

import server.command.Command;
import server.repository.UserRepository;

import java.nio.channels.SelectionKey;

/** Holds the repository and connection identity shared by session-dependent commands. */
public abstract class AbstractCommand implements Command {
    private final SelectionKey selectionKey;
    private final UserRepository repository;

    /**
     * Captures the command's execution context without authenticating it.
     *
     * @param repository account and credential service
     * @param selectionKey connection identity passed to repository operations
     */
    public AbstractCommand(UserRepository repository, SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
        this.repository = repository;
    }

    /** @return the connection identity captured for this command */
    protected SelectionKey getSelectionKey() {
        return selectionKey;
    }

    /** @return the repository used to execute this command */
    protected UserRepository getRepository() {
        return repository;
    }
}
