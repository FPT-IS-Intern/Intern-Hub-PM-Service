package com.intern.hub.pm.feign;

import com.intern.hub.pm.feign.model.WalletApiResponse;
import com.intern.hub.pm.config.FeignConfiguration;
import com.intern.hub.pm.feign.model.WalletTokenRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "wallet", url = "${services.wl.url:${WL_SERVICE_URL:http://localhost:9000}}", configuration = FeignConfiguration.class)
public interface WalletInternalFeignClient {

        @PostMapping("/wl/internal/project/check-token")
        WalletApiResponse<Boolean> checkTokenForProject(
                        @RequestParam("userId") Long userId,
                        @RequestBody WalletTokenRequest request);
}
