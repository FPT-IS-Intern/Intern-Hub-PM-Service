package com.intern.hub.pm.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/teams")
@Tag(name = "Thành viên team", description = "Các thao tác quản lý thành viên trong dự án team")
@SecurityRequirement(name = "Bearer")
public class TeamMemberController {

}
