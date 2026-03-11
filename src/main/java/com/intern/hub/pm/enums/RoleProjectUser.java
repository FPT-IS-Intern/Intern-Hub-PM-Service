package com.intern.hub.pm.enums;

public enum RoleProjectUser {

    DEV("DEV"),
    PM("PM"),
    DESIGN("DESIGN"),
    QA("QA");

    private final String label;

    RoleProjectUser(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
