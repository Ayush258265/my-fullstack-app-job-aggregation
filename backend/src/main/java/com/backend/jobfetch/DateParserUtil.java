package com.backend.jobfetch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.Instant;

@Slf4j
@Component
public class DateParserUtil {

    /**
     * Tries multiple known date formats used across job APIs
     * (Arbeitnow, Remotive, RemoteOK) and falls back to today's date
     * if nothing matches.
     */
    public LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return LocalDate.now();
        }

        // 1. Full ISO datetime, e.g. "2024-01-15T10:30:00"
        try {
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME).toLocalDate();
        } catch (Exception ignored) {}

        // 2. ISO datetime with offset, e.g. "2024-01-15T10:30:00+00:00"
        try {
            return OffsetDateTime.parse(dateStr).toLocalDate();
        } catch (Exception ignored) {}

        // 3. Plain ISO date, e.g. "2024-01-15"
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
        } catch (Exception ignored) {}

        // 4. Epoch seconds, e.g. "1705315800"
        try {
            long timestamp = Long.parseLong(dateStr);
            return Instant.ofEpochSecond(timestamp).atZone(ZoneOffset.UTC).toLocalDate();
        } catch (Exception ignored) {}

        log.warn("Could not parse date string: {}", dateStr);
        return LocalDate.now();
    }
}