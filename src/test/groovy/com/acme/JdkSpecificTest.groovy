package com.acme

import spock.lang.Specification
import spock.lang.Subject

class JdkSpecificTest extends Specification {
    @Subject
    private final JdkSpecific tool = new JdkSpecific()

    def "returns a JDK specific message"() {
        println("Current runtime version : ${System.getProperty("java.version")}")

        expect:
        tool.message == "This is the generic version of the class. Magic number = 42"
    }
}
