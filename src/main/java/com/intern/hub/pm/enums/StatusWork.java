package com.intern.hub.pm.enums;

public enum StatusWork {
    CHUA_BAT_DAU("Chưa bắt đầu"),
    DANG_THUC_HIEN("Đang thực hiện"),
    TRE_HAN("Trễ hạn"),
    CAN_CHINH_SUA("Cần chỉnh sửa"),
    CHO_DUYET("Chờ duyệt"),
    HOAN_THANH("Hoàn thành"),
    DA_HUY("Đã hủy");

    private final String label;

    StatusWork(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
