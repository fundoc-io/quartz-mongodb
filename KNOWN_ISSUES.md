# Known Issues

## Groovy Test Compilation

Currently, the Groovy test compilation has Java version compatibility issues with the current environment. This is a
known issue with the Groovy Maven plugin version and the Java runtime version.

### Workaround

To build the JAR without running tests:

```bash
mvn clean compile jar:jar
```

To skip tests entirely:

```bash
mvn clean package -DskipTests
```

### Future Fix

The Groovy test compilation can be fixed by:

1. Updating Groovy and gmavenplus plugin versions
2. Using a compatible Java runtime version
3. Migrating Groovy tests to Java if needed

This does not affect the main library functionality, which compiles successfully.
