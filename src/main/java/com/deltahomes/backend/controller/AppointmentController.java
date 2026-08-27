package com.deltahomes.backend.controller;

import com.deltahomes.backend.dto.appointment.AppointmentDtos;
import com.deltahomes.backend.dto.common.ApiResponse;
import com.deltahomes.backend.dto.summary.AppointmentSummary;
import com.deltahomes.backend.entity.communication.Appointment;
import com.deltahomes.backend.entity.enums.AppointmentStatus;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.service.AppointmentService;
import com.deltahomes.backend.service.UserContext;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserContext userContext;

    public AppointmentController(AppointmentService appointmentService, UserContext userContext) {
        this.appointmentService = appointmentService;
        this.userContext = userContext;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppointmentSummary>>> index(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) AppointmentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        User user = userContext.currentUser(principal);
        return ResponseEntity.ok(ApiResponse.page(appointmentService.index(user, status, pageable)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Appointment>> bookAppointment(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody AppointmentDtos.CreateAppointmentRequest request) {
        User user = userContext.currentUser(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(appointmentService.bookAppointment(user, request)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Appointment>> updateAppointmentStatus(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentDtos.UpdateAppointmentStatusRequest request) {
        User user = userContext.currentUser(principal);
        return ResponseEntity.ok(ApiResponse.ok(appointmentService.updateStatus(id, request.status(), user)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Appointment>> updateAppointmentStatusLegacy(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        User user = userContext.currentUser(principal);
        AppointmentStatus status = AppointmentStatus.valueOf(
                request.get("status").toUpperCase());
        return ResponseEntity.ok(ApiResponse.ok(appointmentService.updateStatus(id, status, user)));
    }
}
