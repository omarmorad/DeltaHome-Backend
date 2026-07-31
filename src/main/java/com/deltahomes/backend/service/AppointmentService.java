package com.deltahomes.backend.service;

import com.deltahomes.backend.entity.communication.Appointment;
import com.deltahomes.backend.entity.enums.AppointmentStatus;
import com.deltahomes.backend.exception.BusinessException;
import com.deltahomes.backend.exception.ResourceNotFoundException;
import com.deltahomes.backend.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public Appointment bookAppointment(Appointment appointment) {
        appointment.setStatus(AppointmentStatus.PENDING);
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment updateStatus(UUID appointmentId, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        // Validate state transitions
        if (appointment.getStatus() == AppointmentStatus.PENDING &&
                (newStatus == AppointmentStatus.ACCEPTED ||
                 newStatus == AppointmentStatus.REJECTED)) {
            appointment.setStatus(newStatus);
        } else if (appointment.getStatus() == AppointmentStatus.ACCEPTED &&
                   newStatus == AppointmentStatus.COMPLETED) {
            appointment.setStatus(newStatus);
        } else if (appointment.getStatus() == AppointmentStatus.PENDING &&
                   newStatus == AppointmentStatus.CANCELLED) {
            appointment.setStatus(newStatus);
        } else {
            throw new BusinessException(
                "Cannot transition from " + appointment.getStatus() + " to " + newStatus);
        }

        return appointmentRepository.save(appointment);
    }
}
