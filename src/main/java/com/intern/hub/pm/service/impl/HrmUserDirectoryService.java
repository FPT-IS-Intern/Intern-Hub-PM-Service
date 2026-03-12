package com.intern.hub.pm.service.impl;

import com.intern.hub.library.common.exception.InternalErrorException;
import com.intern.hub.library.common.exception.NotFoundException;
import com.intern.hub.pm.feign.HrmInternalFeignClient;
import com.intern.hub.pm.feign.model.HrmUserClientModel;
import feign.FeignException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HrmUserDirectoryService {

    private final HrmInternalFeignClient hrmInternalFeignClient;

    public HrmUserClientModel requireById(Long userId) {
        try {
            var response = hrmInternalFeignClient.getUserByIdInternal(userId);
            if (response == null || response.data() == null) {
                throw new NotFoundException("hrm.user.not.found", "Không tìm thấy user trong HRM");
            }
            return response.data();
        } catch (FeignException.NotFound ex) {
            throw new NotFoundException("hrm.user.not.found", "Không tìm thấy user trong HRM");
        } catch (Exception ex) {
            log.error("Failed to fetch HRM user by id {}", userId, ex);
            throw new InternalErrorException("hrm.user.fetch.error", "Không thể lấy thông tin user từ HRM");
        }
    }

    public Map<Long, HrmUserClientModel> findByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        try {
            var response = hrmInternalFeignClient.getUsersByIdsInternal(userIds);
            if (response == null || response.data() == null) {
                return Map.of();
            }
            return response.data().stream()
                    .collect(Collectors.toMap(HrmUserClientModel::userId, Function.identity()));
        } catch (Exception ex) {
            log.error("Failed to fetch HRM users by ids {}", userIds, ex);
            throw new InternalErrorException("hrm.user.fetch.error", "Không thể lấy danh sách user từ HRM");
        }
    }
}
