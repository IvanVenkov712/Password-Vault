package server.command.commands;

import exception.ArgumentCountMismatchException;
import exception.PasswordVaultException;
import org.junit.jupiter.api.Test;
import server.repository.UserRepository;

import java.nio.channels.SelectionKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RegisterCommandTest {
    @Test
    void testExecuteValidArgs() throws PasswordVaultException {
        UserRepository repo = mock();
        SelectionKey key = mock();
        String[] args = new String[] {"a", "b", "c"};
        RegisterCommand command = new RegisterCommand(repo, key, args);
        command.execute();
        verify(repo).registerUser(key, "a", "b", "c");
    }

    @Test
    void testExecuteLessArgs() {
        UserRepository repo = mock();
        SelectionKey key = mock();
        String[] args = new String[] {"a", "b"};
        assertThrows(ArgumentCountMismatchException.class, () -> new RegisterCommand(repo, key, args),
            "ArgumentCountMismatchException expected when used with less args"
        );
    }

    @Test
    void testExecuteMoreArgs() {
        UserRepository repo = mock();
        SelectionKey key = mock();
        String[] args = new String[] {"a", "b", "c", "d"};
        assertThrows(ArgumentCountMismatchException.class, () ->  new RegisterCommand(repo, key, args),
            "ArgumentCountMismatchException expected when used with more args"
        );
    }
}