package server.command;

import server.Messages;
import server.command.factory.CommandFactory;
import exception.PasswordVaultException;
import server.logger.FileLogger;
import server.logger.Logger;
import server.repository.UserRepository;

import java.nio.channels.SelectionKey;

/** Creates and runs commands, logging failures and translating exceptions into responses. */
public class CommandExecutor {
    private final CommandFactory factory;
    private final Logger logger;

    /**
     * Creates an executor with explicit parsing and logging dependencies.
     *
     * @param factory command parser and repository owner
     * @param logger destination for command failures
     */
    public CommandExecutor(CommandFactory factory, Logger logger) {
        this.factory = factory;
        this.logger = logger;
    }

    /**
     * Creates an executor that appends failures to the default error log.
     *
     * @param factory command parser and repository owner
     */
    public CommandExecutor(CommandFactory factory) {
        this(factory, new FileLogger());
    }

    /**
     * Executes one request. Business errors retain their messages; unexpected
     * exceptions become {@link Messages#INTERNAL_SERVER_ERROR}.
     *
     * @param key connection identity used to locate the authenticated session
     * @param clientInput raw command text
     * @return a successful result or an error response
     */
    public CommandResult execute(SelectionKey key, String clientInput) {
        try {
            return factory.newCommand(key, clientInput).execute();
        } catch (PasswordVaultException e) {
            logger.log(e);
            return new CommandResult(Status.ERROR, e.getMessage());
        } catch (Exception e) {
            logger.log(e);
            return new CommandResult(Status.ERROR, Messages.INTERNAL_SERVER_ERROR);
        }
    }

    /** @return the factory's repository, used by server shutdown to release resources */
    public UserRepository getRepository() {
        return factory.getRepository();
    }
}
