package server.command.commands;

import server.command.Command;
import server.command.CommandResult;
import server.command.Status;
import exception.ArgumentCountMismatchException;
import exception.PasswordVaultException;
import server.repository.UserRepository;

import java.nio.channels.SelectionKey;

/** Removes a website credential entry after matching its stored username. */
public class RemovePasswordCommand extends AbstractCommand {
    public static final String COMMAND_STRING = "remove-password";

    public static final int ARGS_COUNT = 2;
    public static final int WEBSITE_INDEX = 0;
    public static final int USER_INDEX = 1;

    private final String userName;
    private final String website;

    /**
     * Captures command arguments after validating their count.
     *
     * @param repository account and credential service
     * @param key connection identity used for session lookup
     * @param args website and expected website username
     * @throws IllegalArgumentException if args is null
     * @throws ArgumentCountMismatchException if the argument count is incorrect
     */
    public RemovePasswordCommand(UserRepository repository, SelectionKey key, String[] args)
        throws ArgumentCountMismatchException {
        super(repository, key);

        Command.validateArgs(args, ARGS_COUNT);
        this.userName = args[USER_INDEX];
        this.website = args[WEBSITE_INDEX];
    }

    @Override
    public CommandResult execute() throws PasswordVaultException {
        getRepository().removePassword(getSelectionKey(), website, userName);
        return new CommandResult(Status.OK, "Password successfully removed");
    }
}
