package edu.itc.salesreport.ui;

import java.time.Clock;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Set;

/** Pure input rules: no UI toolkit, no I/O, clock injected. */
public final class InputRules {
    private static final Set<String> BRANCHES = Set.of(SalesModel.ALL, "PNH", "REP", "BTB");

    private InputRules() {}

    public static Validation<YearMonth> month(String text, Clock clock) {
        if (text == null || text.isBlank()) {
            return new Validation.Invalid<>("Enter a month as yyyy-MM, e.g. 2026-09");
        }
        String stripped = text.strip();
        YearMonth parsed;
        try {
            parsed = YearMonth.parse(stripped);
        } catch (DateTimeParseException e) {
            return new Validation.Invalid<>("'" + stripped + "' is not a month in the form yyyy-MM");
        }
        YearMonth now = YearMonth.now(clock);
        if (parsed.isAfter(now)) {
            return new Validation.Invalid<>(parsed + " is in the future");
        }
        return new Validation.Valid<>(parsed);
    }

    public static Validation<String> branch(String text) {
        if (text == null || text.isBlank()) {
            return new Validation.Invalid<>("Unknown branch '': use ALL, PNH, REP or BTB");
        }
        String stripped = text.strip().toUpperCase();
        if (!BRANCHES.contains(stripped)) {
            return new Validation.Invalid<>("Unknown branch '" + stripped + "': use ALL, PNH, REP or BTB");
        }
        return new Validation.Valid<>(stripped);
    }
}
