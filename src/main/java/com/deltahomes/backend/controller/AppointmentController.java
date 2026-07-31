package com.deltahomes.backend.controller;

import com.deltahomes.backend.entity.communication.Appointment;
import com.deltahomes.backend.entity.enums.AppointmentStatus;
import com.deltahomes.backend.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<Appointment> bookAppointment(@RequestBody Appointment appointment) {
        return ResponseEntity.ok(appointmentService.bookAppointment(appointment));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Appointment> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        AppointmentStatus status = AppointmentStatus.valueOf(
                request.get("status").toUpperCase());
        return ResponseEntity.ok(appointmentService.updateStatus(id, status));
    }
}
