# Current problems

Reviewed against the source on 2026-10-29. These are existing implementation problems and limitations, not fixes delivered by the documentation update. Entries below identify the relevant code, practical impact, and proposed next steps. The review did not validate API credentials or contact the live exposure service.

## Secrets and password handling

### Runtime secrets and vault data are tracked in Git

`git ls-files` lists `ApiKey`, `ApiSecret`, `SecretKey`, `users`, and `error.log`. The [ignore rules](../.gitignore) do not exclude them. Anyone with repository access can read the tracked key material and serialized vault data. This review did not inspect their values or determine whether the API credentials remain valid.

Proposed fix: remove runtime files from tracking, add ignore rules, and provide a setup path using example configuration without secrets. Rotate any real credentials or keys that have been exposed. Encryption-key replacement must migrate existing ciphertext; simply deleting the key would make old passwords unreadable. Removing files from the latest commit alone does not remove them from Git history.

### Commands and generated encryption keys are printed

[PasswordVaultServer.handleRead](../src/main/java/server/PasswordVaultServer.java) prints the complete client input, including plaintext passwords in `register`, `login`, and `add-password`. [SymmetricEncryptorImpl.generateSecretKey](../src/main/java/server/password/encryptor/SymmetricEncryptorImpl.java) prints the generated encryption key in Base64. Console capture can therefore contain credentials and decryption key material.

Proposed fix: remove key printing and log only non-sensitive command metadata. Add checks that password and key values do not appear in captured output.

### Password storage lacks salt, a work factor, and explicit authenticated encryption

[SHA256PasswordHasher](../src/main/java/server/password/hash/SHA256PasswordHasher.java) stores deterministic SHA-256 account hashes without per-user salts or a configurable work factor. Identical account passwords therefore produce identical stored hashes. [Rijndael](../src/main/java/server/password/encryptor/algorithm/Rijndael.java) requests only `AES`, leaving mode and padding to the provider, and stores no IV or authentication tag. [SymmetricEncryptorImpl](../src/main/java/server/password/encryptor/SymmetricEncryptorImpl.java) uses one raw key file for all vaults.

Proposed fix: introduce a password-specific account hashing scheme and an explicit authenticated encryption format with versioned parameters and key handling. Preserve a migration path for existing account hashes and ciphertext. This is a storage-format change, not a comment-only correction.

## Network and session lifecycle

### The protocol has no framing and assumes complete reads and writes

The [client](../src/main/java/client/PasswordVaultClient.java) and [server](../src/main/java/server/PasswordVaultServer.java) each use a 4096-byte buffer and single read/write calls. TCP does not preserve application message boundaries: one command can arrive in fragments, several can arrive together, and writes can transfer fewer bytes than requested. A large client command can overflow its buffer; a large response can overflow the server buffer. Multiline help rules out treating every newline as a complete response without a defined encoding.

Proposed fix: define request and response framing, maximum sizes, accumulation across reads, and write queues that drain remaining bytes. Include status as a structured response field and exercise fragmentation and partial writes in integration tests.

### Local sockets carry plaintext and client failures can stop the server

Connections are bound to localhost but use plain `SocketChannel`, with no TLS layer. Login passwords and decrypted website passwords are sent as plaintext bytes. I/O failures from a client can propagate out of `handleRead()` or `writeClientOutput()` to the `start()` catch block and terminate the selector loop instead of closing only the affected connection.

Proposed fix: define the intended local trust boundary and transport protection before supporting remote access. Handle connection-specific I/O failures within the loop, close the affected channel, and keep serving other clients.

### Disconnect leaves a repository session behind

On end-of-stream, [PasswordVaultServer.handleRead](../src/main/java/server/PasswordVaultServer.java) closes the channel and cancels its selection key. It does not remove the key from the session map owned by [AbstractUserRepository](../src/main/java/server/repository/AbstractUserRepository.java). Repeated authenticated connections can leave retained keys and user references. The entry point also has no shutdown hook calling `stop()`, and `stop()` does not explicitly close each accepted client channel.

Proposed fix: add an idempotent connection-cleanup operation that removes session state and closes the channel. Use it for disconnects, failed requests, and shutdown, with lifecycle integration tests.

## External service and persistence

### Password checks block every connected client and handle API errors generically

[APIPasswordChecker](../src/main/java/server/password/checker/api/APIPasswordChecker.java) calls `HttpClient.send()` synchronously on the server's only selector thread. Neither [the request builder](../src/main/java/server/password/checker/api/PasswordsApiRequestBuilder.java) nor the default HTTP client configures an explicit timeout. A slow API call delays all clients. Only HTTP 404 receives special handling; other statuses are parsed as candidate JSON, so authentication failures, rate limits, and malformed responses can become generic internal errors. Interrupted requests are wrapped without restoring the thread's interrupt status.

Proposed fix: set connection/request timeouts, validate status codes and response fields, preserve interrupts, and return clear service-availability errors. Move blocking work away from the selector thread with defined synchronization for mutable repository state.

### Saving rewrites the only users file and has no rollback

[FileUserAdapter.saveUsers](../src/main/java/server/adapter/FileUserAdapter.java) truncates and serializes directly to the target file. A crash or write failure can leave incomplete data. Repository mutations occur before saving, so a failed save can leave memory changed even though the client receives an error. Loading uses Java object deserialization with no input filter or explicit top-level schema validation; it expects a trusted, compatible file. File access and API calls also run on the selector thread.

Proposed fix: use a temporary file followed by atomic replacement where supported, backups, and a defined recovery/rollback policy. Restrict deserialization or introduce a versioned storage format with validation. [AbstractUserRepository.generatePassword](../src/main/java/server/repository/AbstractUserRepository.java) also calls `saveChanges()` after `addPassword()` already saved, causing an avoidable second full write.

### First-run key creation can fail on Windows

[SymmetricEncryptorImpl.persistSecretKey](../src/main/java/server/password/encryptor/SymmetricEncryptorImpl.java) calls `Files.setPosixFilePermissions()` after writing the key. A filesystem without POSIX attribute support can reject that call; the default constructor wraps the failure and startup stops even though the file may already exist. API credential files must also exist at startup, and their contents are read without trimming.

Proposed fix: detect filesystem capabilities, apply supported access controls, and report setup failures with the affected file and recovery instructions. Define and validate the credential-file format. Keep the key and users data together when changing working directories or restoring a backup.

## Input handling and verification

### Command parsing accepts malformed quoting and counts extra spaces as arguments

[CommandFactoryImpl.getCommandArguments](../src/main/java/server/command/factory/CommandFactoryImpl.java) splits on literal spaces, retains empty tokens, removes all double quotes, and accepts unmatched quotes. For example, `help ` produces an extra empty argument, and `login  user password` changes the argument count. Literal quotes cannot be represented through an escape syntax. The client also calls `Scanner.nextLine()` without checking for console EOF.

Proposed fix: specify whitespace and quote rules, reject malformed input with actionable messages, and handle console EOF cleanly. Add focused parser cases before changing compatibility.

### Unit coverage does not verify the complete application

The [test suite](../src/test/java/server) covers commands, repositories, adapters, and password utilities, with the API checker using a mocked HTTP client. It contains no client/server socket integration tests. Passing unit tests does not verify message framing, disconnect cleanup, slow-service behavior, or startup on different filesystems.

Proposed fix: add controlled integration tests with temporary files and fake password services, then cover the lifecycle and transport failure cases above. Keep live credentials and real vault data out of tests.
