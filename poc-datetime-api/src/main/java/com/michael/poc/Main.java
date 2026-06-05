package com.michael.poc;

import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

/**
 * Demonstra como usar APIs java time.
 * https://docs.oracle.com/en/java/javase/25/docs//api/java.base/java/time/package-summary.html
 * -LocalDate: date only, no time or zone. Good for birthdays, deadlines.
 * -LocalTime: time only, no date or zone. Good for schedules, alarms.
 * -LocalDateTime: date + time, still no zone. Fine for local events.
 * -Instant: UTC timestamp. Use this for logging, DBs, inter-system communication.
 * -ZonedDateTime: date + time + zone. Needed for anything crossing timezones.
 * -Duration: time-based interval (hours, minutes, seconds, nanos).
 * -Period: date-based interval (years, months, days).
 * -DateTimeFormatter: parse and format date/time strings.
 * -TemporalAdjusters: navigate to "next Monday", "end of month", etc.
 * -Comparisons: isBefore/isAfter/between with ChronoUnit.
 */
@Slf4j
public class Main {

    public static void main(String[] args) {

        exampleLocalDate();
        exampleLocalTime();
        exampleLocalDateTime();
        exampleInstant();
        exampleZonedDateTime();
        exampleDuration();
        examplePeriod();
        exampleDateTimeFormatting();
        exampleTemporalAdjustments();
        exampleComparisons();
        exampleSystemDateTime();
        exampleLocalToZoned();
        exampleAgeCalculation();
        exampleLegacyDateConversion();
        exampleDaysOfWeek();
    }

    private static void exampleLocalDate() {
        var today = LocalDate.now();
        log.info("Hoje: {}", today);

        LocalDate specificDate = LocalDate.of(2026, 4, 16);
        log.info("Data específica: {}", specificDate);

        LocalDate parsed = LocalDate.parse("2026-04-16"); // mesma data, só construção diferente
        log.info("Data parseada: {}", parsed);

        LocalDate nextWeek = today.plusDays(7);
        log.info("Próxima semana: {}", nextWeek);

        LocalDate lastMonth = today.minusMonths(1);
        log.info("Mês passado: {}", lastMonth);

        log.info("Ano: {}, Mês: {}, Dia: {}",
                today.getYear(), today.getMonth(), today.getDayOfMonth());
        log.info("Dia da semana: {}", today.getDayOfWeek());
        log.info("É ano bissexto: {}", today.isLeapYear());
    }

    private static void exampleLocalTime() {
        LocalTime now = LocalTime.now();
        log.info("Agora: {}", now);

        LocalTime specificTime = LocalTime.of(14, 30, 45);
        log.info("Hora específica: {}", specificTime);

        LocalTime parsed = LocalTime.parse("14:30:45");
        log.info("Hora parseada: {}", parsed);

        LocalTime midnight = LocalTime.MIDNIGHT;
        log.info("Meia noite: {}", midnight);

        LocalTime oneHourLater = now.plusHours(1);
        log.info("Uma hora depois: {}", oneHourLater);

        LocalTime minus30 = now.minusMinutes(30);
        log.info("30 minutos antes: {}", minus30);

        log.info("Hora: {}, Minuto: {}, Segundo: {}",
                now.getHour(), now.getMinute(), now.getSecond());
    }

    private static void exampleLocalDateTime() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Agora: {}", now);

        LocalDateTime specific = LocalDateTime.of(2026, 4, 16, 14, 30, 45);
        log.info("Específico: {}", specific);

        LocalDateTime combined = LocalDate.of(2026, 4, 16).atTime(14, 30, 45);
        log.info("Combinado: {}", combined);

        LocalDateTime tomorrow = now.plusDays(1);
        log.info("Amanhã no mesmo horário: {}", tomorrow);

        LocalDateTime twoHoursLater = now.plusHours(2);
        log.info("Duas horas depois: {}", twoHoursLater);

        LocalDate dateOnly = now.toLocalDate();
        log.info("Só a data: {}", dateOnly);

        LocalTime timeOnly = now.toLocalTime();
        log.info("Só a hora: {}", timeOnly);

        var truncated = now.truncatedTo(ChronoUnit.HOURS); // zera minutos e segundos
        log.info("Truncado para hora: {}", truncated);
    }

    private static void exampleInstant() {
        Instant now = Instant.now();
        log.info("Agora (UTC): {}", now);

        Instant epoch = Instant.EPOCH;
        log.info("Epoch: {}", epoch);

        Instant fromEpochSeconds = Instant.ofEpochSecond(1640995200);
        log.info("De epoch seconds: {}", fromEpochSeconds);
        // LocalDate d = Instant.ofEpochSecond(0).atZone(ZoneId.systemDefault()).toLocalDate();

        Instant inOneHour = now.plus(Duration.ofHours(1));
        log.info("Uma hora depois: {}", inOneHour);

        Instant fiveMinsAgo = now.minus(Duration.ofMinutes(5));
        log.info("Cinco minutos antes: {}", fiveMinsAgo);

        log.info("Epoch em milissegundos: {}", now.toEpochMilli());
    }

    private static void exampleZonedDateTime() {
        ZonedDateTime utc = ZonedDateTime.now(ZoneId.of("UTC"));
        log.info("UTC: {}", utc);

        ZonedDateTime saoPaulo = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
        log.info("São Paulo: {}", saoPaulo);

        ZonedDateTime utcConverted = saoPaulo.withZoneSameInstant(ZoneId.of("UTC"));
        log.info("São Paulo convertido para UTC: {}", utcConverted);

        log.info("Total de fusos disponíveis: {}", ZoneId.getAvailableZoneIds().size()); // ~600 fusos, só mostrando a quantidade

        log.info("Offset do fuso: {}", saoPaulo.getOffset());
        log.info("ID do fuso: {}", saoPaulo.getZone());
    }

    private static void exampleDuration() {
        Duration oneHour = Duration.ofHours(1);
        log.info("Uma hora: {}", oneHour);

        Duration thirtyMinutes = Duration.ofMinutes(30);
        log.info("Trinta minutos: {}", thirtyMinutes);

        Duration nanoSeconds = Duration.ofNanos(1000000000);
        log.info("Nanosegundos: {}", nanoSeconds);

        Duration betweenTimes = Duration.between(LocalTime.of(10, 0), LocalTime.of(15, 30));
        log.info("Entre 10:00 e 15:30: {}", betweenTimes);

        log.info("Segundos em uma hora: {}", oneHour.getSeconds());
        log.info("Total de nanos: {}", oneHour.toNanos());

        Duration combined = oneHour.plus(thirtyMinutes);
        log.info("Uma hora + 30 minutos: {}", combined);

        log.info("Uma hora é zero? {}", oneHour.isZero());
        log.info("Uma hora é negativa? {}", oneHour.isNegative());
    }

    private static void examplePeriod() {
        Period oneMonth = Period.ofMonths(1);
        log.info("Um mês: {}", oneMonth);

        Period twoYears = Period.ofYears(2);
        log.info("Dois anos: {}", twoYears);

        Period threeWeeks = Period.ofWeeks(3);
        log.info("Três semanas: {}", threeWeeks);

        Period combined = Period.of(1, 2, 15); // 1 ano, 2 meses, 15 dias
        log.info("Combinado (1a 2m 15d): {}", combined);

        Period between = Period.between(LocalDate.of(2025, 1, 1), LocalDate.of(2026, 4, 16));
        log.info("Entre 2025-01-01 e 2026-04-16: {}", between);
        log.info("Anos: {}, Meses: {}, Dias: {}",
                between.getYears(), between.getMonths(), between.getDays());

        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate afterPeriod = startDate.plus(combined);
        log.info("2025-01-01 + (1a 2m 15d) = {}", afterPeriod);
    }

    private static void exampleDateTimeFormatting() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 4, 16, 14, 30, 45);

        log.info("ISO_DATE_TIME: {}", dateTime.format(DateTimeFormatter.ISO_DATE_TIME));
        log.info("ISO_LOCAL_DATE: {}", dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE));
        log.info("ISO_LOCAL_TIME: {}", dateTime.format(DateTimeFormatter.ISO_LOCAL_TIME));

        DateTimeFormatter brazilFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        log.info("Formato brasil: {}", dateTime.format(brazilFmt));

        DateTimeFormatter verboseFmt = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        log.info("Formato verboso: {}", dateTime.format(verboseFmt));

        DateTimeFormatter isoMillisFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        log.info("Formato ISO+millis: {}", dateTime.format(isoMillisFmt));

        var ptBrFmt = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", Locale.of("pt", "BR"));
        log.info("Formato PT-BR: {}", dateTime.format(ptBrFmt));

        String dateString = "16/04/2026 14:30:45";
        LocalDateTime parsed = LocalDateTime.parse(dateString, brazilFmt);
        log.info("Parseado da string: {}", parsed);
    }

    private static void exampleTemporalAdjustments() {
        LocalDate today = LocalDate.now();
        log.info("Hoje: {}", today);

        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        log.info("Fim do mês: {}", endOfMonth);

        LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        log.info("Primeiro dia do mês: {}", firstDayOfMonth);

        LocalDate endOfYear = today.with(TemporalAdjusters.lastDayOfYear());
        log.info("Fim do ano: {}", endOfYear);

        LocalDate nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        log.info("Próxima segunda: {}", nextMonday);

        LocalDate previousFriday = today.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
        log.info("Sexta anterior: {}", previousFriday);

        // TODO: testar implementação de adjuster customizado
    }

    private static void exampleComparisons() {
        LocalDate date1 = LocalDate.of(2026, 4, 10);
        LocalDate date2 = LocalDate.of(2026, 4, 16);
        LocalDate date3 = LocalDate.of(2026, 4, 16);

        log.info("date1.isBefore(date2): {}", date1.isBefore(date2));
        log.info("date2.isAfter(date1): {}", date2.isAfter(date1));
        log.info("date2.equals(date3): {}", date2.equals(date3));
        log.info("date1.compareTo(date2): {}", date1.compareTo(date2));
        log.info("Dias entre date1 e date2: {}", ChronoUnit.DAYS.between(date1, date2));

        LocalDate earliest = date1.isBefore(date2) ? date1 : date2;
        log.info("Mais antiga: {}", earliest);

        var today = LocalDate.now();
        log.info("Hoje é fim de semana? {}", today.getDayOfWeek() == DayOfWeek.SATURDAY
                || today.getDayOfWeek() == DayOfWeek.SUNDAY);
    }

    private static void exampleSystemDateTime() {
        ZoneId systemZone = ZoneId.systemDefault();
        log.info("Fuso do sistema: {}", systemZone);

        ZonedDateTime systemNow = ZonedDateTime.now();
        log.info("Agora no sistema (com fuso): {}", systemNow);

        LocalDateTime localNow = LocalDateTime.now();
        log.info("Agora no sistema (local): {}", localNow);

        Instant instant = systemNow.toInstant();
        log.info("Como instant UTC: {}", instant);
    }

    private static void exampleLocalToZoned() {
        // sem informação de fuso
        LocalDateTime local = LocalDateTime.of(2026, 4, 17, 10, 30, 0);
        log.info("Local (sem fuso): {}", local);

        // atribui um fuso — diz que esse datetime aconteceu em São Paulo
        var saoPaulo = ZoneId.of("America/Sao_Paulo");
        ZonedDateTime zoned = local.atZone(saoPaulo);
        log.info("Com fuso (São Paulo): {}", zoned);

        // converte para UTC para ver o offset aplicado
        ZonedDateTime asUtc = zoned.withZoneSameInstant(ZoneId.of("UTC"));
        log.info("Mesmo instante em UTC: {}", asUtc);

        log.info("Offset naquele momento: {}", zoned.getOffset());
    }

    private static void exampleAgeCalculation() {
        var birthdate = LocalDate.of(1995, 8, 23);
        var today = LocalDate.now();
        var age = Period.between(birthdate, today);
        log.info("Data de nascimento: {}", birthdate);
        log.info("Idade: {} anos, {} meses, {} dias", age.getYears(), age.getMonths(), age.getDays());

        long totalDaysAlive = ChronoUnit.DAYS.between(birthdate, today);
        log.info("Total de dias vividos: {}", totalDaysAlive);
    }

    private static void exampleLegacyDateConversion() {
        // Date legado -> LocalDate (comum ao integrar com libs antigas)
        java.util.Date legacyDate = new java.util.Date();
        var converted = legacyDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        log.info("Date legado: {}", legacyDate);
        log.info("Convertido para LocalDate: {}", converted);

        // LocalDate -> Date legado (menos comum, mas às vezes necessário)
        var backToLegacy = java.util.Date.from(
                LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        log.info("De volta ao Date legado: {}", backToLegacy);
    }

    private static void exampleDaysOfWeek() {
        var today = LocalDate.now();
        var dayOfWeek = today.getDayOfWeek();
        log.info("Dia da semana hoje: {}", dayOfWeek);

        // valor numérico ISO: segunda=1, domingo=7
        log.info("Valor numérico do dia: {}", dayOfWeek.getValue());

        log.info("É segunda? {}", dayOfWeek == DayOfWeek.MONDAY);
        log.info("É sexta? {}", dayOfWeek == DayOfWeek.FRIDAY);

        // próxima ocorrência de cada dia a partir de hoje
        for (DayOfWeek dia : DayOfWeek.values()) {
            var proxima = today.with(TemporalAdjusters.nextOrSame(dia));
            log.info("Próxima {}: {}", dia, proxima);
        }

        // quantos dias faltam para sexta
        long diasParaSexta = ChronoUnit.DAYS.between(today, today.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY)));
        log.info("Dias até sexta: {}", diasParaSexta);
    }
}
