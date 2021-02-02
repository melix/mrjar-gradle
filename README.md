# Building a MRJAR with Gradle

WARNING: Before doing this, we strongly recommend to read this [blog post](https://blog.gradle.org/mrjars) which highlights the consequences of your decision.

# Cross-compilation

Run `./gradlew run` and Gradle will:

   - compile classes from `src/main/java` with a Java 8 compiler
   - compile classes from `src/main/java11` with a Java 11 compiler

It will automatically download the right JDKs for you if you don't have them.

There's a dependency between the Java 11 classes and the Java 8 classes too, showing that it's not an issue.
