package com.acme;

public class JdkSpecific {
     public static void main(String... args) {
    	 var magic = Shared.magic();
         System.out.println("This is the Java 10 version of the class. Magic number = " + magic);
     }
}
