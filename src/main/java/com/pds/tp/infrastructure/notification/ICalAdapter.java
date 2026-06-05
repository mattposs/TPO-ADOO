package com.pds.tp.infrastructure.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Adapter that renders Scrim notifications as RFC 5545 iCalendar payload.
 */
@Slf4j
@Component
public class ICalAdapter implements Notifier {

    private static final String ICAL_VERSION = "2.0";
    private static final DateTimeFormatter ICAL_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    @Override
    public void sendNotification(String calendarEmail, String message) {
        try {
            String icalContent = buildICalEvent(calendarEmail, message);
            exportAndNotify(calendarEmail, icalContent);
            log.info("[ICAL] Evento enviado a {} - {}", calendarEmail, message);
        } catch (Exception ex) {
            log.error("[ICAL] Error al enviar evento", ex);
            throw new NotificationDeliveryException("iCal export failed", ex);
        }
    }

    private String buildICalEvent(String userEmail, String eventDetails) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventTime = now.plusHours(2);

        return "BEGIN:VCALENDAR\n" +
                "VERSION:" + ICAL_VERSION + "\n" +
                "PRODID:-//eSports Scrims//EN\n" +
                "CALSCALE:GREGORIAN\n" +
                "METHOD:PUBLISH\n" +
                "BEGIN:VEVENT\n" +
                "UID:" + UUID.randomUUID() + "@scrims.local\n" +
                "DTSTAMP:" + now.format(ICAL_DATE_FORMATTER) + "\n" +
                "DTSTART:" + eventTime.format(ICAL_DATE_FORMATTER) + "\n" +
                "DTEND:" + eventTime.plusHours(1).format(ICAL_DATE_FORMATTER) + "\n" +
                "SUMMARY:Scrim eSports - " + eventDetails + "\n" +
                "DESCRIPTION:" + eventDetails + "\n" +
                "ATTENDEE:mailto:" + userEmail + "\n" +
                "END:VEVENT\n" +
                "END:VCALENDAR";
    }

    private void exportAndNotify(String calendarEmail, String icalContent) {
        // In production this can call Google Calendar API / Microsoft Graph / CalDAV.
        log.debug("[ICAL] Content for {}:\n{}", calendarEmail, icalContent);
    }
}


