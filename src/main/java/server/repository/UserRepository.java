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

/**
 * Account, session, and credential operations used by server commands.
 * Account usernames identify vault owners; website usernames identify credentials
 * inside a vault. Credential operations require an authenticated connection key.
 * The default implementation stores at most one credential pair per website.
 */
public interface UserRepository extends Closeable {
    /**
     * Registers an account and persists it without logging the connection in.
     *
     * @param selectionKey connection identity, which must not already be logged in
     * @param user new vault account name
     * @param password account password to hash
     * @param passwordRepeat confirmation of the same password
     * @throws UserAlreadyExistsException if the account name is already registered
     * @throws UserLoggedInException if the connection is already authenticated
     * @throws PasswordsNotMatchException if the password and confirmation differ
     */
    void registerUser(SelectionKey selectionKey, String user, String password, String passwordRepeat)
        throws UserAlreadyExistsException, UserLoggedInException, PasswordsNotMatchException;

    /**
     * Authenticates an existing account and associates it with the connection.
     *
     * @param selectionKey connection identity for the new session
     * @param user vault account name
     * @param password account password to verify
     * @throws UserNotExistsException if the account is unknown
     * @throws IncorrectPasswordException if the password does not match
     * @throws UserLoggedInException if this connection is already authenticated
     */
    void loginUser(SelectionKey selectionKey, String user, String password)
        throws UserNotExistsException, IncorrectPasswordException,
        UserLoggedInException;

    /**
     * Removes the authenticated session without closing the network connection.
     *
     * @param selectionKey connection identity to log out
     * @throws UserNotLoggedInException if the connection has no authenticated session
     */
    void logoutUser(SelectionKey selectionKey) throws UserNotLoggedInException;

    /**
     * Checks a password, encrypts it, and persists a new website entry.
     *
     * @param selectionKey authenticated connection identity
     * @param website exact website key; no URL normalization is performed
     * @param user username for the website, not the vault account
     * @param password plaintext website password
     * @throws PasswordNotSecureException if the configured checker rejects the password
     * @throws UserNotLoggedInException if the connection is unauthenticated
     * @throws WebsiteExistingException if the vault already contains this website
     */
    void addPassword(SelectionKey selectionKey, String website, String user, String password)
        throws PasswordNotSecureException, UserNotLoggedInException, WebsiteExistingException;

    /**
     * Generates a password and saves it through the same checks as {@link #addPassword}.
     *
     * @param selectionKey authenticated connection identity
     * @param website new website key
     * @param userName username for the website
     * @return the generated plaintext password after successful storage
     * @throws UserNotLoggedInException if the connection is unauthenticated
     * @throws WebsiteExistingException if the website is already stored
     * @throws PasswordNotSecureException if the checker rejects the generated value
     */
    String generatePassword(SelectionKey selectionKey, String website, String userName)
        throws UserNotLoggedInException, WebsiteExistingException, PasswordNotSecureException;

    /**
     * Looks up a website entry, verifies its username, and decrypts the password.
     *
     * @param selectionKey authenticated connection identity
     * @param website exact website key to find
     * @param userName expected website username
     * @return website username, a space, and the decrypted password
     * @throws UserNotLoggedInException if the connection is unauthenticated
     * @throws WebsiteUsernameNotExistsException if the stored website username differs
     * @throws WebsiteNotExistsException if the website is not stored
     */
    String retrieveCredentials(SelectionKey selectionKey, String website, String userName)
        throws UserNotLoggedInException, WebsiteUsernameNotExistsException, WebsiteNotExistsException;

    /**
     * Removes a matching website entry and persists the updated vault.
     *
     * @param selectionKey authenticated connection identity
     * @param website exact website key to remove
     * @param userName expected website username
     * @throws WebsiteUsernameNotExistsException if the stored website username differs
     * @throws UserNotLoggedInException if the connection is unauthenticated
     * @throws WebsiteNotExistsException if the website is not stored
     */
    void removePassword(SelectionKey selectionKey, String website, String userName)
        throws WebsiteUsernameNotExistsException, UserNotLoggedInException, WebsiteNotExistsException;

    /** Persists the current users and encrypted credentials through the configured adapter. */
    void saveChanges();
}
