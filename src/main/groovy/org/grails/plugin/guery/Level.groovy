package org.grails.plugin.guery

/**
 * Created by unrza249 on 11.12.17.
 */
enum Level {
    ALL(100),
    RULE(80),
    RULESET(60),
    POLICY(40),
    INSTANCE(20),
    OFF(0)

    private final int value

    Level(int value) {
        this.value = value
    }

    public String toString() {
        return name() + " = " + value
    }
}