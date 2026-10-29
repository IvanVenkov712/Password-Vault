package server.command.commands;

import server.command.Command;
import server.command.CommandResult;
import server.command.Status;
import exception.ArgumentCountMismatchException;
import exception.PasswordVaultException;
import server.repository.UserRepository;

import java.nio.channels.SelectionKey;

public class LogoutCommand extends AbstractCommand {
    public static final String COMMAND_STRING = "logout";

    private static final int ARGS_COUNT = 0;

    public LogoutCommand(UserRepository repository, SelectionKey key, String[] args)
        throws ArgumentCountMismatchException {
        super(repository, key);
        Command.validateArgs(args, ARGS_COUNT);
    }

    @Override
    public CommandResult execute() throws PasswordVaultException {
        getRepository().logoutUser(getSelectionKey());
        return new CommandResult(Status.OK, "Logout successful");
    }
}
