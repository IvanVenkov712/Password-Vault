package server.command.commands;

import server.command.Command;
import server.repository.UserRepository;

import java.nio.channels.SelectionKey;

public abstract class AbstractCommand implements Command {
    private final SelectionKey selectionKey;
    private final UserRepository repository;

    public AbstractCommand(UserRepository repository, SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
        this.repository = repository;
    }

    protected SelectionKey getSelectionKey() {
        return selectionKey;
    }

    protected UserRepository getRepository() {
        return repository;
    }
}
