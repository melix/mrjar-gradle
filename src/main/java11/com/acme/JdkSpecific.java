package com.acme;

public class JdkSpecific {

    public String getMessage() {
        return "This is the Java 11 version of the class. Magic number = " + Shared.magic();
    }

    public static void main(String... args) {
        System.out.println(new JdkSpecific().getMessage());
    }
}
