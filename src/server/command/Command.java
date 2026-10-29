package server.command;

import exception.ArgumentCountMismatchException;
import exception.PasswordVaultException;

public interface Command {

    CommandResult execute() throws PasswordVaultException;

    static void validateArgs(String[] commandArgs, int argsCount) throws ArgumentCountMismatchException {
        if (commandArgs == null) {
            throw new IllegalArgumentException("args is null");
        }
        if (commandArgs.length != argsCount) {
            throw new ArgumentCountMismatchException("Arguments count mismatch");
        }
    }
}
