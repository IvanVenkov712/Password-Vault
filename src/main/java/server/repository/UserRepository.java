package server.repository;

import exception.PasswordNotSecureException;
import exception.PasswordsNotMatchException;
import exception.UserAlreadyExistsException;
import exception.UserLoggedInException;
import exception.UserNotExistsException;
import exception.UserNotLoggedInException;
import exception.WebsiteUsernameNotExistsException;
import exception.WebsiteExistingException;
import exception.WebsiteNotExistsException;
import exception.IncorrectPasswordException;

import java.io.Closeable;
import java.nio.channels.SelectionKey;

public interface UserRepository extends Closeable {
    void registerUser(SelectionKey selectionKey, String user, String password, String passwordRepeat)
        throws UserAlreadyExistsException, UserLoggedInException, PasswordsNotMatchException;

    void loginUser(SelectionKey selectionKey, String user, String password)
        throws UserNotExistsException, IncorrectPasswordException,
        UserLoggedInException;

    void logoutUser(SelectionKey selectionKey) throws UserNotLoggedInException;

    void addPassword(SelectionKey selectionKey, String website, String user, String password)
        throws PasswordNotSecureException, UserNotLoggedInException, WebsiteExistingException;

    String generatePassword(SelectionKey selectionKey, String website, String userName)
        throws UserNotLoggedInException, WebsiteExistingException, PasswordNotSecureException;

    String retrieveCredentials(SelectionKey selectionKey, String website, String userName)
        throws UserNotLoggedInException, WebsiteUsernameNotExistsException, WebsiteNotExistsException;

    void removePassword(SelectionKey selectionKey, String website, String userName)
        throws WebsiteUsernameNotExistsException, UserNotLoggedInException, WebsiteNotExistsException;

    void saveChanges();
}
