# Password Vault

A simple Java-based password manager server/client application that stores encrypted credentials per user. The project includes a server implementation, a client, password generation/checking, hashing, encryption modules, and unit tests.

## Documentation

- [Architecture and contributor guide](docs/ARCHITECTURE.md): request flow, storage, password services, and adding commands.
- [Current problems](docs/CURRENT_PROBLEMS.md): verified limitations, their impact, and proposed fixes.
- JavaDoc in `src/main/java`: API contracts, argument meanings, return values, exceptions, and resource ownership.

## Build and run

Install JDK 25 and set `JAVA_HOME` to its installation directory. The [Maven Wrapper](https://maven.apache.org/tools/wrapper/) downloads the pinned Maven version on its first run; Maven does not need to be installed separately. Internet access is needed for the initial Maven and dependency downloads.

Build and run all unit tests from the project root:

```powershell
.\mvnw.cmd clean verify
```

On Linux or macOS, use `./mvnw clean verify`. To run just the tests, use `.\mvnw.cmd test` (or `./mvnw test`). Test reports are written to `target/surefire-reports/`.

The build creates `target/password-vault-1.0-SNAPSHOT.jar`, including its runtime dependencies. Start the server from the project root:

```sh
java -jar target/password-vault-1.0-SNAPSHOT.jar
```

In another terminal, start the client:

```sh
java -cp target/password-vault-1.0-SNAPSHOT.jar client.PasswordVaultClient
```

The server listens on `localhost:7777`. It reads `ApiKey` and `ApiSecret` from its working directory for Enzoic authentication and uses `SecretKey`, `users`, and `error.log` there for encryption, persistence, and logging. These runtime files are not packaged into the JAR.

`ApiKey` and `ApiSecret` must contain the corresponding API credential without a trailing newline; the code reads their contents without trimming. A missing `users` file starts an empty vault. If `SecretKey` is absent, the server generates a key and attempts to apply POSIX file permissions, which can fail on Windows. Keep an existing users file together with its matching key; a newly generated key cannot decrypt passwords encrypted with the old key. See [current problems](docs/CURRENT_PROBLEMS.md) before using real credentials.

For IntelliJ IDEA, open the root `pom.xml` as a project (or add it as a Maven project in an existing checkout), select JDK 25, and reload Maven. Dependencies and source roots are configured by Maven.

If IntelliJ reports `JDK isn't specified for module 'password-vault'`, open **File > Project Structure** (`Ctrl+Alt+Shift+S`). Under **Project**, set **SDK** to JDK 25 (use **Add SDK > JDK** to select its installation directory if needed). Under **Modules > password-vault > Dependencies**, set **Module SDK** to **Project SDK**. Apply the settings, then use **Reload All Maven Projects** in the Maven tool window. IntelliJ's SDK selection is separate from `JAVA_HOME`.

## Features
- Register / Login / Logout users
- Store, retrieve and remove website credentials
- Password generation and security checks
- Symmetric encryption and hashing (SHA-256)
- File-based user adapter and logger

## Commands

Type `help` at the client's `Enter command:` prompt to see usage before or after login. Commands are case-sensitive. Use one space between tokens; enclose an argument containing spaces in double quotes. The parser removes all double quotes and does not support escaping literal quotes.

| Command | Session requirement | Result |
| --- | --- | --- |
| `help` | Any state | Lists all commands, syntax, and descriptions. Accepts no arguments. |
| `register <user> <password> <password-repeat>` | Logged out | Creates a vault account with a hashed password; does not log in automatically. |
| `login <user> <password>` | Logged out | Authenticates the connection as a vault account. |
| `logout` | Logged in | Ends the session and keeps the connection open. |
| `retrieve-credentials <website> <user>` | Logged in | Returns the website username and decrypted password separated by a space. |
| `generate-password <website> <user>` | Logged in | Generates, checks, and saves a 16-character password, then returns it. |
| `add-password <website> <user> <password>` | Logged in | Checks the supplied password for exposure, then encrypts and saves it. |
| `remove-password <website> <user>` | Logged in | Removes the matching website entry. |
| `disconnect` | Any state | Closes the client connection locally; no command is sent to the server. |

For registration and login, `<user>` is the vault account name. For website commands, it is the account name on that website. Each vault supports one credential pair per exact website string; adding another username for the same website is rejected. Websites are not normalized, so `example.com` and `https://example.com` are separate keys.

### Example session

The client prefixes server messages with `Response:`. The generated password below is illustrative; the actual value varies and saving requires a successful API check.

```text
Enter command: register demo "Example passphrase 123!" "Example passphrase 123!"
Response: Registration successful!
Enter command: login demo "Example passphrase 123!"
Response: Login successful
Enter command: generate-password example.com demo@example.com
Response: aB3!cD4@eF5#gH6$
Enter command: retrieve-credentials example.com demo@example.com
Response: demo@example.com aB3!cD4@eF5#gH6$
Enter command: remove-password example.com demo@example.com
Response: Password successfully removed
Enter command: logout
Response: Logout successful
Enter command: disconnect
```

### Checking for compromised passwords

Both `add-password` and `generate-password` use the Enzoic checker. The request contains the first ten hexadecimal characters of a locally computed SHA-256 hash, with API credentials in the authorization header. The implementation accepts HTTP 404; otherwise it rejects a matching full hash only when `revealedInExposure` is true. This check does not enforce password length or complexity, and registration does not call it. Network, authentication, or response-parsing failures can produce `Internal server error`; see [current problems](docs/CURRENT_PROBLEMS.md).

## Generate API documentation

After a successful `clean verify`, use the packaged JAR as the dependency classpath:

```sh
javadoc -d target/site/apidocs -sourcepath src/main/java -classpath target/password-vault-1.0-SNAPSHOT.jar -subpackages client:server:exception -Xdoclint:all,-missing
```

Open `target/site/apidocs/index.html` in a browser. The command validates JavaDoc syntax and references while allowing undocumented members such as constants and simple exception constructors. Generated documentation is ignored with the rest of `target/`.

## Project module structure
- `src/main/java` - main source code
    - `client` - NIO client implementation
    - `server`
      - `PasswordVaultServer` - server entry point. A single-threaded NIO server implementation
      - `adapter` - user adapter interfaces and implementations (for file storage and IO operations)
      - `command` - command framework and concrete commands
      - `logger` - error logging classes and interface
      - `model` - domain models (`User`, `WebsitePair`, `Session`)
      - `password` - password verification, generation, hashing, encryption
        - `checker` - Enzoic API interfaces for password checking
        - `encryptor` - password symmetric encryptor implementation
        - `generator` - randomized password generator implementation
        - `hash` - password hashing utilities
      - `repository` - user repository interfaces and implementations
    - `exception` - all kinds of business logic exceptions including those related to incorrect program usage
- `src/test/java` - unit tests organized parallel to the main source packages
- `pom.xml` - Maven dependencies, Java version, test execution, and executable JAR packaging
- `.mvn/wrapper`, `mvnw`, `mvnw.cmd` - Maven Wrapper configuration and launchers
- `target` - generated build output (ignored by Git)
