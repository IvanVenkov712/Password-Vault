package server.command.factory;

import server.command.Command;
import exception.ArgumentCountMismatchException;
import server.repository.UserRepository;

import java.nio.channels.SelectionKey;

public interface CommandFactory {
    Command newCommand(SelectionKey key, String clientInput) throws ArgumentCountMismatchException;

    UserRepository getRepository();
}
