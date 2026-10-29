package server.command.commands;

import server.command.Command;
import server.command.CommandResult;
import server.command.Status;
import exception.ArgumentCountMismatchException;
import exception.PasswordVaultException;
import server.repository.UserRepository;

import java.nio.channels.SelectionKey;

/** Authenticates a vault account and binds its session to the connection. */
public class LoginCommand extends AbstractCommand {
    public static final String COMMAND_STRING = "login";

    private static final int ARGS_COUNT = 2;
    private static final int USER_INDEX = 0;
    private static final int PASSWORD_INDEX = 1;

    private final String user;
    private final String password;

    /**
     * Captures command arguments after validating their count.
     *
     * @param repository account and credential service
     * @param key connection identity used for session lookup
     * @param args vault username and account password
     * @throws IllegalArgumentException if args is null
     * @throws ArgumentCountMismatchException if the argument count is incorrect
     */
    public LoginCommand(UserRepository repository, SelectionKey key, String[] args)
        throws ArgumentCountMismatchException {
        super(repository, key);

        Command.validateArgs(args, ARGS_COUNT);
        user = args[USER_INDEX];
        password = args[PASSWORD_INDEX];
    }

    @Override
    public CommandResult execute() throws PasswordVaultException {
        getRepository().loginUser(getSelectionKey(), user, password);
        return new CommandResult(Status.OK, "Login successful");
    }
}
