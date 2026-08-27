package com.deltahomes.backend.service;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Resolves user-facing messages in the request's locale.
 *
 * <p>Locale is determined by Spring's {@code LocaleResolver} (see
 * {@code LocaleConfig}): the {@code Accept-Language} header, overridable with
 * {@code ?lang=ar|en}. Default is Arabic (ar-EG) — the platform's primary audience.</p>
 *
 * <p>Convention: message keys ARE the English literals used in the service layer,
 * so callers can keep throwing plain English text and get automatic translation —
 * an unknown key simply falls back to itself (English).</p>
 */
@Service
public class I18nService {

    private final MessageSource messageSource;

    public I18nService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /** Translates a message literal into the current request locale. */
    public String t(String messageOrKey) {
        if (messageOrKey == null || messageOrKey.isBlank()) {
            return messageOrKey;
        }
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(messageOrKey, null, messageOrKey, locale);
    }

    /** Current request locale ("ar-EG" / "en-US"). */
    public Locale currentLocale() {
        return LocaleContextHolder.getLocale();
    }

    /** True when the current request locale is Arabic. */
    public boolean isArabic() {
        return "ar".equalsIgnoreCase(LocaleContextHolder.getLocale().getLanguage());
    }
}
