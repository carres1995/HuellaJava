package com.huellas.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private DateUtil() {}

    public static String formatDate(LocalDate date) {
        return (date == null) ? "" : date.format(DATE_FORMATTER);
    }


    public static String formatDateTime(LocalDateTime dateTime) {
        return (dateTime == null) ? "" : dateTime.format(DATETIME_FORMATTER);
    }

    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Convierte un String (dd/MM/yyyy HH:mm) a LocalDateTime.
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}
