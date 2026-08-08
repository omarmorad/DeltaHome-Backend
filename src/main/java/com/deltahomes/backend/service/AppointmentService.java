package com.deltahomes.backend.service;

import com.deltahomes.backend.dto.appointment.AppointmentDtos;
import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.dto.summary.AppointmentSummary;
import com.deltahomes.backend.entity.communication.Appointment;
import com.deltahomes.backend.entity.enums.AppointmentStatus;
import com.deltahomes.backend.entity.enums.UserRole;
import com.deltahomes.backend.entity.property.Property;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.exception.BusinessException;
import com.deltahomes.backend.exception.ResourceNotFoundException;
import com.deltahomes.backend.repository.AppointmentRepository;
import com.deltahomes.backend.repository.PropertyRepository;
import com.deltahomes.backend.util.PageUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PropertyRepository propertyRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PropertyRepository propertyRepository) {
        this.appointmentRepository = appointmentRepository;
        this.propertyRepository = propertyRepository;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<AppointmentSummary> index(User user, AppointmentStatus status, Pageable pageable) {
        Page<AppointmentSummary> page = appointmentRepository.searchIndex(
                user.getId(),
                status,
                PageUtils.normalizeSort(pageable))
            .map(this::toSummary);
        return PaginatedResponse.from(page);
    }

    @Transactional
    public Appointment bookAppointment(User customer, AppointmentDtos.CreateAppointmentRequest request) {
        if (!request.requestedSlot().isAfter(OffsetDateTime.now())) {
            throw new BusinessException("requestedSlot must be in the future");
        }
        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property", request.propertyId()));

        Appointment appointment = new Appointment();
        appointment.setProperty(property);
        appointment.setCustomer(customer);
        appointment.setOwner(property.getOwner());
        appointment.setRequestedSlot(request.requestedSlot());
        appointment.setNote(request.note());
        appointment.setStatus(AppointmentStatus.PENDING);
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment updateStatus(UUID appointmentId, AppointmentStatus newStatus, User actor) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        // Only the customer, the owner (property owner) or an admin may change the status.
        boolean isCustomer = appointment.getCustomer().getId().equals(actor.getId());
        boolean isOwner = appointment.getOwner().getId().equals(actor.getId());
        if (!isCustomer && !isOwner && actor.getRole() != UserRole.ADMIN) {
            throw new BusinessException("You are not allowed to update this appointment");
        }

        // Validate state transitions
        if (appointment.getStatus() == AppointmentStatus.PENDING &&
                (newStatus == AppointmentStatus.ACCEPTED ||
                 newStatus == AppointmentStatus.REJECTED ||
                 newStatus == AppointmentStatus.CANCELLED)) {
            appointment.setStatus(newStatus);
        } else if (appointment.getStatus() == AppointmentStatus.ACCEPTED &&
                   (newStatus == AppointmentStatus.COMPLETED ||
                    newStatus == AppointmentStatus.CANCELLED)) {
            appointment.setStatus(newStatus);
        } else {
            throw new BusinessException(
                "Cannot transition from " + appointment.getStatus() + " to " + newStatus);
        }

        return appointmentRepository.save(appointment);
    }

    private AppointmentSummary toSummary(Appointment a) {
        return new AppointmentSummary() {
            @Override public UUID getId() { return a.getId(); }
            @Override public String getStatus() { return a.getStatus() != null ? a.getStatus().name() : null; }
            @Override public OffsetDateTime getRequestedSlot() { return a.getRequestedSlot(); }
            @Override public String getNote() { return a.getNote(); }
            @Override public OffsetDateTime getCreatedAt() { return a.getCreatedAt(); }
            @Override public UUID getPropertyId() { return a.getProperty() != null ? a.getProperty().getId() : null; }
            @Override public String getPropertyTitle() { return a.getProperty() != null ? a.getProperty().getTitle() : null; }
            @Override public String getCustomerName() { return a.getCustomer() != null ? a.getCustomer().getName() : null; }
            @Override public String getOwnerName() { return a.getOwner() != null ? a.getOwner().getName() : null; }
        };
    }
}
