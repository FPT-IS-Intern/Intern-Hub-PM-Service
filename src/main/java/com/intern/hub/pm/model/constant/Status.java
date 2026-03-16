package com.intern.hub.pm.model.constant;

import lombok.Getter;

@Getter
public enum Status {

    ACTIVE("Đang hoạt động"),
    DELETED("Đã xoá");

    private final String label;

    Status(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
