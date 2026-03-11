package com.intern.hub.pm.enums;

public enum WorkItemType {
    PROJECT("Dự án"),
    MODULE("Module"),
    TASK ("Nhiệm vụ");

    private final String label;

    WorkItemType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
