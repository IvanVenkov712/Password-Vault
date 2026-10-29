package server.command.factory;

import server.command.Command;
import exception.ArgumentCountMismatchException;
import server.repository.UserRepository;

import java.nio.channels.SelectionKey;

/** Converts client input into executable commands backed by a shared repository. */
public interface CommandFactory {
    /**
     * Parses a command and validates its argument count without executing it.
     *
     * @param key connection identity used by session-dependent commands
     * @param clientInput command name followed by arguments; null produces an error command
     * @return the requested command, or an error command for unrecognized input
     * @throws ArgumentCountMismatchException if a recognized command has the wrong argument count
     */
    Command newCommand(SelectionKey key, String clientInput) throws ArgumentCountMismatchException;

    /** @return the repository shared by commands created by this factory */
    UserRepository getRepository();
}
