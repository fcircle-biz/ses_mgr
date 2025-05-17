package jp.co.example.sesapp.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日付・時刻操作のユーティリティクラス.
 */
public final class DateTimeUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Tokyo");

    private DateTimeUtils() {
        // ユーティリティクラスなのでインスタンス化を防止
    }

    /**
     * 現在の日時を取得します.
     *
     * @return 現在のLocalDateTime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(DEFAULT_ZONE_ID);
    }

    /**
     * 現在の日付を取得します.
     *
     * @return 現在のLocalDate
     */
    public static LocalDate today() {
        return LocalDate.now(DEFAULT_ZONE_ID);
    }

    /**
     * LocalDateTimeをフォーマットして文字列にします.
     *
     * @param dateTime フォーマットするLocalDateTime
     * @return フォーマットされた日時文字列（yyyy/MM/dd HH:mm:ss）
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "";
    }

    /**
     * LocalDateをフォーマットして文字列にします.
     *
     * @param date フォーマットするLocalDate
     * @return フォーマットされた日付文字列（yyyy/MM/dd）
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    /**
     * 文字列をLocalDateTimeに変換します.
     *
     * @param dateTimeStr 日時を表す文字列（yyyy/MM/dd HH:mm:ss）
     * @return 変換されたLocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return dateTimeStr != null && !dateTimeStr.isEmpty() ?
                LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER) : null;
    }

    /**
     * 文字列をLocalDateに変換します.
     *
     * @param dateStr 日付を表す文字列（yyyy/MM/dd）
     * @return 変換されたLocalDate
     */
    public static LocalDate parseDate(String dateStr) {
        return dateStr != null && !dateStr.isEmpty() ?
                LocalDate.parse(dateStr, DATE_FORMATTER) : null;
    }

    /**
     * java.util.DateをLocalDateTimeに変換します.
     *
     * @param date 変換するDate
     * @return 変換されたLocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return date != null ?
                date.toInstant().atZone(DEFAULT_ZONE_ID).toLocalDateTime() : null;
    }

    /**
     * LocalDateTimeをjava.util.Dateに変換します.
     *
     * @param localDateTime 変換するLocalDateTime
     * @return 変換されたDate
     */
    public static Date toDate(LocalDateTime localDateTime) {
        return localDateTime != null ?
                Date.from(localDateTime.atZone(DEFAULT_ZONE_ID).toInstant()) : null;
    }

    /**
     * LocalDateをjava.util.Dateに変換します.
     *
     * @param localDate 変換するLocalDate
     * @return 変換されたDate
     */
    public static Date toDate(LocalDate localDate) {
        return localDate != null ?
                Date.from(localDate.atStartOfDay(DEFAULT_ZONE_ID).toInstant()) : null;
    }
}