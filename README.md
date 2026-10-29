# Password Vault

A simple Java-based password manager server/client application that stores encrypted credentials per user. The project includes a server implementation, a client, password generation/checking, hashing, encryption modules, and unit tests.

## Features
- Register / Login / Logout users
- Store, retrieve and remove website credentials
- Password generation and security checks
- Symmetric encryption and hashing (SHA-256)
- File-based user adapter and logger

## Commands
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
- `src` - main source code
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
- `test` - unit tests organized parallel to `src`. Around 66% line coverage
