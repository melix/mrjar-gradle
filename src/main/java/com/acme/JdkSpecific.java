package com.acme;

public class JdkSpecific {
     public static void main(String... args) {
         System.out.println("This is the generic version of the class. Magic number = " + Shared.magic());
     }
}
