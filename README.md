# Password Vault

A simple Java-based password manager server/client application that stores encrypted credentials per user. The project includes a server implementation, a client, password generation/checking, hashing, encryption modules, and unit tests.

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

For IntelliJ IDEA, open the root `pom.xml` as a project (or add it as a Maven project in an existing checkout), select JDK 25, and reload Maven. Dependencies and source roots are configured by Maven.

If IntelliJ reports `JDK isn't specified for module 'password-vault'`, open **File > Project Structure** (`Ctrl+Alt+Shift+S`). Under **Project**, set **SDK** to JDK 25 (use **Add SDK > JDK** to select its installation directory if needed). Under **Modules > password-vault > Dependencies**, set **Module SDK** to **Project SDK**. Apply the settings, then use **Reload All Maven Projects** in the Maven tool window. IntelliJ's SDK selection is separate from `JAVA_HOME`.

## Features
- Register / Login / Logout users
- Store, retrieve and remove website credentials
- Password generation and security checks
- Symmetric encryption and hashing (SHA-256)
- File-based user adapter and logger

## Commands
#### Help
- `help`
- lists all available commands with their syntax and descriptions; available before and after login
- enclose arguments containing spaces in double quotes

#### Registering in the system
- `register <user> <password> <password-repeat>`
- registers a new user, and the server *(in a file containing the passwords of all users)* saves the password in hashed form and creates a new file to store all passwords *(encrypted so that they can be returned to the user, but not saved in clear form)* of this user

#### Login to the system
- `login <user> <password>`
- login user

#### Exiting the system
- `logout`
- logout user

#### Retrieving a password for a given site
- `retrieve-credentials <website> <user>`
- returns the username and password of the user with which they registered on the given site

#### Generating a password
- `generate-password <website> <user>`
- generates and saves the user's password for a given site, also specifies the username with which the registration on the site will be made

#### Add password
- `add-password <website> <user> <password>`
- adds the password provided by the user, and the server checks whether this password is secure via the REST API, and if so, saves it

#### Remove password
- `remove-password <website> <user>`
- removes the user's saved password for a given site

#### Disconnect
- `disconnect` - terminates the connection between the client and the server

### Example of valid input data and responses

``` bash
> login pesho peshopass
> Login successful

> generate-password facebook.com pesho@gmail.com
> _rrR~S>k$[8+Ps/x2WyaFv

> retrieve-credentials facebook.com pesho@gmail.com
> _rrR~S>k$[8+Ps/x2WyaFv

> remove-password facebook.com pesho@gmail.com
> Password successfully removed

> logout
> Logout successful

> disconnect
> Disconnected
```

### Checking for compromised passwords

When adding a new password by a user for a given site (`add-password facebook.com pesho@gmail.com P@ssw0rd`), the server checks whether the password has been compromised and if so, returns an appropriate error.
For this purpose was used the following [API](https://www.enzoic.com/docs-passwords-api/).
Requests to the REST API require authentication via an API key, which can be obtained by registering [here](https://www.enzoic.com/free-trial-2/).

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
