package server.command.commands;

import exception.ArgumentCountMismatchException;
import server.command.Command;
import server.command.CommandResult;
import server.command.Status;

public class HelpCommand implements Command {
    public static final String COMMAND_STRING = "help";

    private static final int ARGS_COUNT = 0;
    private static final String HELP_MESSAGE = """
        Available commands:
          help - Show available commands.
          register <user> <password> <password-repeat> - Register a new user.
          login <user> <password> - Log in to your account.
          logout - Log out of your account.
          retrieve-credentials <website> <user> - Retrieve saved credentials (requires login).
          generate-password <website> <user> - Generate and save a password (requires login).
          add-password <website> <user> <password> - Check and save a password (requires login).
          remove-password <website> <user> - Remove saved credentials (requires login).
          disconnect - Disconnect the client from the server.
        Enclose arguments containing spaces in double quotes.
        """;

    public HelpCommand(String[] args) throws ArgumentCountMismatchException {
        Command.validateArgs(args, ARGS_COUNT);
    }

    @Override
    public CommandResult execute() {
        return new CommandResult(Status.OK, HELP_MESSAGE);
    }
}
