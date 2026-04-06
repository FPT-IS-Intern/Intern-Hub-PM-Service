package com.intern.hub.pm.model.constant;

public enum StatusWork {
    NOT_STARTED("Chưa bắt đầu"),
    IN_PROGRESS("Đang thực hiện"),
    OVERDUE("Trễ hạn"),
    NEEDS_REVISION("Cần sửa lại"),
    REJECTED("Từ chối nhận"),
    PENDING_REVIEW("Chờ duyệt"),
    COMPLETED("Hoàn thành"),
    CANCELED("Đã hủy"),
    QUIT("Không làm nữa");

    private final String label;

    StatusWork(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
