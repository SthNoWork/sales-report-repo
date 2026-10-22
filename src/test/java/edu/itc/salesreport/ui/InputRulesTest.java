package edu.itc.salesreport.ui;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InputRulesTest {

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-09-27T03:00:00Z"),
            ZoneId.of("Asia/Phnom_Penh"));

    @Test
    void validTrimmedMonth() {
        assertEquals(new Validation.Valid<>(YearMonth.of(2026, 9)),
                InputRules.month("  2026-09  ", clock));
    }

    @Test
    void emptyMonth() {
        assertEquals(new Validation.Invalid<>("Enter a month as yyyy-MM, e.g. 2026-09"),
                InputRules.month("", clock));
    }

    @Test
    void blankMonth() {
        assertEquals(new Validation.Invalid<>("Enter a month as yyyy-MM, e.g. 2026-09"),
                InputRules.month("   ", clock));
    }

    @Test
    void slashFormatMonth() {
        assertEquals(new Validation.Invalid<>("'2026/09' is not a month in the form yyyy-MM"),
                InputRules.month("2026/09", clock));
    }

    @Test
    void singleDigitMonth() {
        assertEquals(new Validation.Invalid<>("'2026-9' is not a month in the form yyyy-MM"),
                InputRules.month("2026-9", clock));
    }

    @Test
    void textMonth() {
        assertEquals(new Validation.Invalid<>("'Sept' is not a month in the form yyyy-MM"),
                InputRules.month("Sept", clock));
    }

    @Test
    void nextMonthIsFuture() {
        assertEquals(new Validation.Invalid<>("2026-10 is in the future"),
                InputRules.month("2026-10", clock));
    }

    @Test
    void distantFutureMonth() {
        assertEquals(new Validation.Invalid<>("2099-01 is in the future"),
                InputRules.month("2099-01", clock));
    }

    @Test
    void branchNormalisationAndRejection() {
        assertEquals(new Validation.Valid<>("PNH"), InputRules.branch("pnh"));
        assertEquals(new Validation.Valid<>("REP"), InputRules.branch("  rep  "));
        assertEquals(new Validation.Valid<>("BTB"), InputRules.branch("BTB"));
        assertEquals(new Validation.Valid<>("ALL"), InputRules.branch("all"));
        assertEquals(new Validation.Invalid<>("Unknown branch 'XYZ': use ALL, PNH, REP or BTB"),
                InputRules.branch("XYZ"));
        assertEquals(new Validation.Invalid<>("Unknown branch '': use ALL, PNH, REP or BTB"),
                InputRules.branch(""));
    }
}
