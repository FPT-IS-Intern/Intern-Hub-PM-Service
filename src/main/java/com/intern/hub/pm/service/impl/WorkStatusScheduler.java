package com.intern.hub.pm.service.impl;

import com.intern.hub.pm.enums.StatusWork;
import com.intern.hub.pm.enums.WorkItemType;
import com.intern.hub.pm.model.WorkItem;
import com.intern.hub.pm.repository.WorkItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkStatusScheduler {

    private final WorkItemRepository workItemRepository;

    @Scheduled(fixedDelayString = "${pm.status-scheduler.fixed-delay-ms:60000}")
    public void syncStatuses() {
        LocalDateTime now = LocalDateTime.now();

        for (WorkItem workItem : workItemRepository.findAll()) {
            StatusWork nextStatus = resolveStatus(workItem, now);
            if (nextStatus != null && nextStatus != workItem.getStatus()) {
                workItem.setStatus(nextStatus);
                workItem.setUpdatedAt(now);
                workItemRepository.save(workItem);
            }
        }
    }

    private StatusWork resolveStatus(WorkItem workItem, LocalDateTime now) {
        if (workItem.getStatus() == StatusWork.HOAN_THANH || workItem.getStatus() == StatusWork.DA_HUY) {
            return null;
        }
        if (workItem.getEndDate() != null
                && now.isAfter(workItem.getEndDate())
                && (workItem.getStatus() == StatusWork.CHUA_BAT_DAU
                || workItem.getStatus() == StatusWork.DANG_THUC_HIEN
                || workItem.getStatus() == StatusWork.CAN_CHINH_SUA)) {
            return StatusWork.TRE_HAN;
        }
        if (workItem.getStartDate() != null
                && (workItem.getStatus() == StatusWork.CHUA_BAT_DAU || workItem.getStatus() == StatusWork.CAN_CHINH_SUA)
                && !now.isBefore(workItem.getStartDate())) {
            return StatusWork.DANG_THUC_HIEN;
        }
        if (workItem.getType() == WorkItemType.PROJECT
                && workItem.getStatus() == StatusWork.TRE_HAN
                && workItem.getEndDate() != null
                && now.isBefore(workItem.getEndDate())) {
            return StatusWork.DANG_THUC_HIEN;
        }
        return null;
    }
}
