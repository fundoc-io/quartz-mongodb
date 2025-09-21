# Contributing

This project uses a very minimalistic approach to contributions:

* Fork it
* Create a branch
* Make your changes
* Commit it with an explanation of [
  *why* this change is necessary](https://chris.beams.io/posts/git-commit/#why-not-how)
* Make sure all tests pass
* Submit a pull request
* Be patient

## Prerequisites

This project uses Maven for building and dependency management.
A [compatible JDK version](https://maven.apache.org/users/index.html#system-requirements) is required (Java 8 or
higher).

## Running Tests

To run the tests, you need a MongoDB instance running. The easiest way is to use the embedded MongoDB support:

``` shell
mvn test
```

For development, you can start MongoDB manually and then run:

``` shell
mvn compile test-compile
mvn test
```
