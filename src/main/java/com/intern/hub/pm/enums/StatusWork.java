package com.intern.hub.pm.enums;

public enum StatusWork {
    CHUA_BAT_DAU("Chưa bắt đầu"),
    DANG_THUC_HIEN("Đang thực hiện"),
    CHO_DUYET("Chờ duyệt"),
    DA_DUYET("Đã duyệt"),
    HOAN_THANH("Hoàn thành"),
    TU_CHOI("Đã từ chối"),
    DA_XOA("Đã xóa"),
    QUA_HAN("Qúa hạn");

    private final String label;

    StatusWork(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
