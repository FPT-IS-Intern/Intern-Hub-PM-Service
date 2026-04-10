package com.intern.hub.pm.feign;

import com.intern.hub.pm.feign.model.WalletApiResponse;
import com.intern.hub.pm.config.FeignConfiguration;
import com.intern.hub.pm.feign.model.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "wallet", url = "${services.wl.url:${WL_SERVICE_URL:http://localhost:9000}}", configuration = FeignConfiguration.class)
public interface WalletInternalFeignClient {

    @GetMapping("/wl/internal/wallet/status")
    WalletApiResponse<WalletStatusResponse> getWalletStatus(
            @RequestParam("userId") Long userId);

    @PostMapping("/wl/internal/project/check-and-lock")
    WalletApiResponse<Boolean> checkAndLockProject(
            @RequestParam("userId") Long userId,
            @RequestBody WalletTokenRequest request);

    @PostMapping("/wl/internal/task/check-and-lock")
    WalletApiResponse<Boolean> checkAndLockTask(
            @RequestParam("userId") Long userId,
            @RequestBody WalletTokenTaskRequest request);

    @PutMapping("/wl/internal/project/edit/token")
    WalletApiResponse<Void> editProjectTokens(
            @RequestParam("userId") Long userId,
            @RequestBody WalletWorkItemRequest request);

    @PutMapping("/wl/internal/tasks/locked-tokens")
    WalletApiResponse<Void> editTaskTokens(
            @RequestParam("userId") Long userId,
            @RequestBody WalletEditTaskRequest request);

    @PostMapping("/wl/internal/transaction/project")
    WalletApiResponse<Void> saveTransactionProject(
            @RequestBody WalletTransactionProjectRequest request);

    @PostMapping("/wl/internal/transaction/team")
    WalletApiResponse<Void> saveTransactionModule(
            @RequestBody WalletTransactionModuleRequest request);

    @PostMapping("/wl/internal/transaction/task")
    WalletApiResponse<Void> saveTransactionTask(
            @RequestBody WalletTransactionTaskRequest request);

    @PostMapping("/wl/internal/transaction/task/update")
    WalletApiResponse<Void> updateTransactionTask(
            @RequestBody WalletTransactionTaskRequest request);

    @PostMapping("/wl/internal/transaction/browse")
    WalletApiResponse<Void> browseWork(
            @RequestBody WalletBrowseWorkRequest request);

    @DeleteMapping("/wl/internal/project/locked-tokens")
    WalletApiResponse<Void> recalculateTokensOfWork(
            @RequestParam("userId") Long userId,
            @RequestBody WalletWorkItemRequest request);

    @DeleteMapping("/wl/internal/task/locked-tokens")
    WalletApiResponse<Void> recalculateTokensOfTask(
            @RequestParam("userId") Long userId,
            @RequestBody WalletEditTaskRequest request);
}
