package server.command;

import exception.ArgumentCountMismatchException;
import exception.PasswordVaultException;
import org.junit.jupiter.api.Test;
import server.command.factory.CommandFactory;
import server.logger.Logger;
import server.repository.UserRepository;

import java.nio.channels.SelectionKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommandExecutorTest {
    @Test
    void testExecute() throws PasswordVaultException {

        CommandFactory factory = mock();
        SelectionKey key = mock();
        Command command = mock();
        when(factory.newCommand(key, "my-command")).thenReturn(command);
        Logger logger = mock();

        CommandExecutor executor = new CommandExecutor(factory, logger);
        executor.execute(key, "my-command");
        verify(command).execute();
    }

    @Test
    void testExecuteWithOKResult() throws PasswordVaultException {
        CommandFactory factory = mock();
        SelectionKey key = mock();
        Command command = mock();
        CommandResult okResult = new CommandResult(Status.OK, "Success");
        when(command.execute()).thenReturn(okResult);
        when(factory.newCommand(key, "test")).thenReturn(command);
        Logger logger = mock();

        CommandExecutor executor = new CommandExecutor(factory, logger);
        CommandResult result = executor.execute(key, "test");

        assertEquals(Status.OK, result.status());
        assertEquals("Success", result.message());
    }

    @Test
    void testExecuteWithPasswordVaultExceptionReturnsError() throws PasswordVaultException {
        CommandFactory factory = mock();
        SelectionKey key = mock();
        Command command = mock();
        PasswordVaultException exception = new PasswordVaultException("Test error");
        when(command.execute()).thenThrow(exception);
        when(factory.newCommand(key, "test")).thenReturn(command);
        Logger logger = mock();

        CommandExecutor executor = new CommandExecutor(factory, logger);
        CommandResult result = executor.execute(key, "test");

        assertEquals(Status.ERROR, result.status());
        assertEquals("Test error", result.message());
        verify(logger).log(exception);
    }

    @Test
    void testExecuteWithGeneralExceptionReturnsInternalError() throws PasswordVaultException {
        CommandFactory factory = mock();
        SelectionKey key = mock();
        Command command = mock();
        RuntimeException exception = new RuntimeException("Unexpected error");
        when(command.execute()).thenThrow(exception);
        when(factory.newCommand(key, "test")).thenReturn(command);
        Logger logger = mock();

        CommandExecutor executor = new CommandExecutor(factory, logger);
        CommandResult result = executor.execute(key, "test");

        assertEquals(Status.ERROR, result.status());
        assertEquals("Internal server error", result.message());
        verify(logger).log(exception);
    }

    @Test
    void testExecuteWithDefaultLogger() throws PasswordVaultException {
        CommandFactory factory = mock();
        SelectionKey key = mock();
        Command command = mock();
        when(command.execute()).thenReturn(new CommandResult(Status.OK, "OK"));
        when(factory.newCommand(key, "test")).thenReturn(command);

        CommandExecutor executor = new CommandExecutor(factory);
        CommandResult result = executor.execute(key, "test");

        assertEquals(Status.OK, result.status());
    }

    @Test
    void testGetRepository() {
        CommandFactory factory = mock();
        UserRepository repository = mock();
        when(factory.getRepository()).thenReturn(repository);
        Logger logger = mock();

        CommandExecutor executor = new CommandExecutor(factory, logger);
        UserRepository result = executor.getRepository();

        assertEquals(repository, result);
    }

    @Test
    void testExecuteArgumentCountMismatchException() throws PasswordVaultException {
        CommandFactory factory = mock();
        SelectionKey key = mock();
        ArgumentCountMismatchException exception = new ArgumentCountMismatchException("Args mismatch");
        when(factory.newCommand(key, "test")).thenThrow(exception);
        Logger logger = mock();

        CommandExecutor executor = new CommandExecutor(factory, logger);
        CommandResult result = executor.execute(key, "test");

        assertEquals(Status.ERROR, result.status());
        assertEquals("Args mismatch", result.message());
        verify(logger).log(exception);
    }

    @Test
    void testExecuteMultipleCommandsSequentially() throws PasswordVaultException {
        CommandFactory factory = mock();
        SelectionKey key = mock();
        Command command1 = mock();
        Command command2 = mock();
        when(command1.execute()).thenReturn(new CommandResult(Status.OK, "First"));
        when(command2.execute()).thenReturn(new CommandResult(Status.OK, "Second"));
        when(factory.newCommand(key, "cmd1")).thenReturn(command1);
        when(factory.newCommand(key, "cmd2")).thenReturn(command2);
        Logger logger = mock();

        CommandExecutor executor = new CommandExecutor(factory, logger);
        CommandResult result1 = executor.execute(key, "cmd1");
        CommandResult result2 = executor.execute(key, "cmd2");

        assertEquals("First", result1.message());
        assertEquals("Second", result2.message());
    }
}