package com.deltahomes.backend.security;

import com.deltahomes.backend.service.I18nService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Emits the SAME unified error envelope as {@code GlobalExceptionHandler}:
 * {@code { "success": false, "status": 403, "error": "...", "timestamp": ... }}
 * with the message localized per the request locale (Accept-Language / ?lang=).
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final I18nService i18n;

    public RestAccessDeniedHandler(ObjectMapper objectMapper, I18nService i18n) {
        this.objectMapper = objectMapper;
        this.i18n = i18n;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), Map.of(
                "success", false,
                "status", HttpServletResponse.SC_FORBIDDEN,
                "error", i18n.t("You do not have permission to perform this action"),
                "timestamp", OffsetDateTime.now()
        ));
    }
}
