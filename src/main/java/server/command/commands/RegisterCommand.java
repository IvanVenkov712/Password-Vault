package server.command.commands;

import server.command.Command;
import server.command.CommandResult;
import server.command.Status;
import exception.ArgumentCountMismatchException;
import exception.PasswordVaultException;
import server.repository.UserRepository;

import java.nio.channels.SelectionKey;

/** Registers a vault account without creating an authenticated session. */
public class RegisterCommand extends AbstractCommand {
    public static final String COMMAND_STRING = "register";

    private static final int ARGS_NUM = 3;
    private static final int USER_INDEX = 0;
    private static final int PASSWORD_INDEX = 1;
    private static final int REPEAT_PASSWORD_INDEX = 2;

    private final String user;
    private final String password;
    private final String repeatPassword;

    /**
     * Captures command arguments after validating their count.
     *
     * @param repository account and credential service
     * @param key connection identity used for session lookup
     * @param args vault username, password, and repeated password
     * @throws IllegalArgumentException if args is null
     * @throws ArgumentCountMismatchException if the argument count is incorrect
     */
    public RegisterCommand(UserRepository repository, SelectionKey key, String[] args)
        throws ArgumentCountMismatchException {
        super(repository, key);

        Command.validateArgs(args, ARGS_NUM);
        user = args[USER_INDEX];
        password = args[PASSWORD_INDEX];
        repeatPassword = args[REPEAT_PASSWORD_INDEX];
    }

    @Override
    public CommandResult execute() throws PasswordVaultException {
        getRepository().registerUser(getSelectionKey(), user, password, repeatPassword);
        return new CommandResult(Status.OK, "Registration successful!");
    }
}
