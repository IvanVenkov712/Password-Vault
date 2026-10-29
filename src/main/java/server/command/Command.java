package server.command;

import exception.ArgumentCountMismatchException;
import exception.PasswordVaultException;

/** A validated request that can be executed independently of input parsing. */
public interface Command {

    /**
     * Executes the request against its captured dependencies.
     *
     * @return the command status and user-facing message
     * @throws PasswordVaultException if a business rule prevents the operation
     */
    CommandResult execute() throws PasswordVaultException;

    /**
     * Checks the number of arguments, excluding the command name.
     *
     * @param commandArgs parsed argument tokens
     * @param argsCount required argument count
     * @throws IllegalArgumentException if the argument array is null
     * @throws ArgumentCountMismatchException if the count differs from the required count
     */
    static void validateArgs(String[] commandArgs, int argsCount) throws ArgumentCountMismatchException {
        if (commandArgs == null) {
            throw new IllegalArgumentException("args is null");
        }
        if (commandArgs.length != argsCount) {
            throw new ArgumentCountMismatchException("Arguments count mismatch");
        }
    }
}
