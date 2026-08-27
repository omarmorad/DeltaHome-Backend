package com.deltahomes.backend.security;

import com.deltahomes.backend.service.I18nService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Emits the SAME unified error envelope as {@code GlobalExceptionHandler}:
 * {@code { "success": false, "status": 401, "error": "...", "timestamp": ... }}
 * with the message localized per the request locale (Accept-Language / ?lang=).
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final I18nService i18n;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper, I18nService i18n) {
        this.objectMapper = objectMapper;
        this.i18n = i18n;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), Map.of(
                "success", false,
                "status", HttpServletResponse.SC_UNAUTHORIZED,
                "error", i18n.t("Authentication is required to access this resource"),
                "timestamp", OffsetDateTime.now()
        ));
    }
}
