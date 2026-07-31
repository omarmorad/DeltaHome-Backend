package com.deltahomes.backend.service;

import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.dto.summary.NotificationSummary;
import com.deltahomes.backend.entity.communication.Notification;
import com.deltahomes.backend.entity.enums.NotificationType;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.repository.NotificationRepository;
import com.deltahomes.backend.util.PageUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public PaginatedResponse<NotificationSummary> index(User user, String q, NotificationType type,
                                                        Boolean isRead, Pageable pageable) {
        Page<NotificationSummary> page = notificationRepository.searchIndex(
                user.getId(),
                q == null ? "" : q.trim(),
                type == null ? null : type.name(),
                isRead,
                PageUtils.normalizeSort(pageable));
        return PaginatedResponse.from(page);
    }

    public long countUnread(User user) {
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }
}
