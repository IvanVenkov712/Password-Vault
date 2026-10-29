package server.command.factory;

import exception.ArgumentCountMismatchException;
import exception.PasswordVaultException;
import org.junit.jupiter.api.Test;
import server.command.Command;
import server.command.CommandResult;
import server.command.Status;
import server.repository.UserRepository;

import java.nio.channels.SelectionKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CommandFactoryImplTest {
    @Test
    void testNewCommandLogin() throws PasswordVaultException {
        UserRepository repo = mock();
        SelectionKey key = mock();
        CommandFactoryImpl factory = new CommandFactoryImpl(repo);
        Command created = factory.newCommand(key, "login Pesho PeshoPass");
        created.execute();
        verify(repo).loginUser(key, "Pesho", "PeshoPass");
    }

    @Test
    void testNewCommandRegister() throws PasswordVaultException {
        UserRepository repo = mock();
        SelectionKey key = mock();
        CommandFactoryImpl factory = new CommandFactoryImpl(repo);
        Command created = factory.newCommand(key, "register Pesho PeshoPass PeshoPass");
        created.execute();
        verify(repo).registerUser(key, "Pesho", "PeshoPass", "PeshoPass");
    }

    @Test
    void testNewCommandLogout() throws PasswordVaultException {
        UserRepository repo = mock();
        SelectionKey key = mock();
        CommandFactoryImpl factory = new CommandFactoryImpl(repo);
        Command created = factory.newCommand(key, "logout");
        created.execute();
        verify(repo).logoutUser(key);
    }

    @Test
    void testNewCommandAddPassword() throws PasswordVaultException {
        UserRepository repo = mock();
        SelectionKey key = mock();
        CommandFactoryImpl factory = new CommandFactoryImpl(repo);
        Command created = factory.newCommand(key, "add-password facebook dancho@gmail DanchoPass");
        created.execute();
        verify(repo).addPassword(key, "facebook", "dancho@gmail", "DanchoPass");
    }

    @Test
    void testNewCommandRemovePassword() throws PasswordVaultException {
        UserRepository repo = mock();
        SelectionKey key = mock();
        CommandFactoryImpl factory = new CommandFactoryImpl(repo);
        Command created = factory.newCommand(key, "remove-password facebook dancho@gmail");
        created.execute();
        verify(repo).removePassword(key, "facebook", "dancho@gmail");
    }

    @Test
    void testNewCommandRetrieveCredentials() throws PasswordVaultException {
        UserRepository repo = mock();
        SelectionKey key = mock();
        CommandFactoryImpl factory = new CommandFactoryImpl(repo);
        Command created = factory.newCommand(key, "retrieve-credentials facebook dancho@gmail");
        created.execute();
        verify(repo).retrieveCredentials(key, "facebook", "dancho@gmail");
    }

    @Test
    void testNewCommandGeneratePassword() throws PasswordVaultException {
        UserRepository repo = mock();
        SelectionKey key = mock();
        CommandFactoryImpl factory = new CommandFactoryImpl(repo);
        Command created = factory.newCommand(key, "generate-password facebook dancho@gmail");
        created.execute();
        verify(repo).generatePassword(key, "facebook", "dancho@gmail");
    }

    @Test
    void testNewCommandUnrecognised() throws PasswordVaultException {
        UserRepository repo = mock();
        SelectionKey key = mock();
        CommandFactoryImpl factory = new CommandFactoryImpl(repo);
        Command created = factory.newCommand(key, "chill facebook dancho@gmail");
        CommandResult res = created.execute();
        assertEquals(Status.ERROR, res.status(), "ERROR expected for unrecognised command");
    }

    @Test
    void testNewCommandNull() throws PasswordVaultException {
        UserRepository repo = mock();
        SelectionKey key = mock();
        CommandFactoryImpl factory = new CommandFactoryImpl(repo);
        Command created = factory.newCommand(key, null);
        CommandResult res = created.execute();
        assertEquals(Status.ERROR, res.status(), "ERROR expected for null command");
    }


}