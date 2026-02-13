package com.gxl.plancore.checkin.interfaces.controller;

import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gxl.plancore.checkin.application.dto.CheckInResult;
import com.gxl.plancore.checkin.application.dto.CheckInStreakResult;
import com.gxl.plancore.checkin.application.service.CheckInApplicationService;
import com.gxl.plancore.checkin.interfaces.dto.CheckInRequest;
import com.gxl.plancore.checkin.interfaces.dto.CheckInResponse;
import com.gxl.plancore.checkin.interfaces.dto.CheckInStreakResponse;
import com.gxl.plancore.common.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 打卡控制器
 */
@RestController
@RequestMapping("/api/v1/check-in")
public class CheckInController {

    private static final Logger log = LoggerFactory.getLogger(CheckInController.class);

    private final CheckInApplicationService checkInApplicationService;

    public CheckInController(CheckInApplicationService checkInApplicationService) {
        this.checkInApplicationService = checkInApplicationService;
    }

    /**
     * 打卡
     * POST /api/v1/check-in
     * 幂等：当日重复打卡返回相同结果
     */
    @PostMapping
    public ApiResponse<CheckInResponse> checkIn(
            HttpServletRequest httpRequest,
            @Valid @RequestBody CheckInRequest request) {
        log.info("收到打卡请求: date={}", request.getDate());

        String userId = (String) httpRequest.getAttribute("userId");

        CheckInResult result = checkInApplicationService.checkIn(userId, request.getDate());

        CheckInResponse response = new CheckInResponse(
                result.getDate(),
                DateTimeFormatter.ISO_INSTANT.format(result.getCheckedAt()),
                result.getConsecutiveDays()
        );

        return ApiResponse.success(response);
    }

    /**
     * 查询打卡连续天数
     * GET /api/v1/check-in/streak
     */
    @GetMapping("/streak")
    public ApiResponse<CheckInStreakResponse> queryStreak(HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");

        log.info("收到查询打卡连续天数请求: userId={}", userId);

        CheckInStreakResult result = checkInApplicationService.queryStreak(userId);

        CheckInStreakResponse response = new CheckInStreakResponse(
                result.getConsecutiveDays(),
                result.getLastCheckInDate()
        );

        return ApiResponse.success(response);
    }
}
