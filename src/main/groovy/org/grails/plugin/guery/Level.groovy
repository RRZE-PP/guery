package org.grails.plugin.guery

/**
 * Created by unrza249 on 11.12.17.
 */
enum Level {
    ALL(100),
    RULE(75),
    RULESET(50),
    POLICY(25),
    OFF(0)

    private final int value

    Level(int value) {
        this.value = value
    }

    public String toString() {
        return name() + " = " + value
    }

    public matches(Level otherLevel) {
        otherLevel != null && otherLevel.value >= this.value
    }
}