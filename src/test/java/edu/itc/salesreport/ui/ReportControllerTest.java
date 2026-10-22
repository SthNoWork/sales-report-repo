package edu.itc.salesreport.ui;

import edu.itc.salesreport.TestData;
import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** No JavaFX here: the view is replaced by a recording fake. */
class ReportControllerTest {

    static final class FakeErrors implements ErrorDisplay {
        final List<String> shown = new ArrayList<>();

        @Override
        public void showError(String message) {
            shown.add(message);
        }

        @Override
        public void clearError() {
            shown.add("<cleared>");
        }
    }

    @TempDir
    Path dataDir;

    private final FakeErrors errors = new FakeErrors();
    private final List<SalesEvent> events = new ArrayList<>();
    private SalesModel model;
    private ReportController controller;

    @BeforeEach
    void setUp() throws IOException {
        TestData.writeSample(dataDir);
        var parser = new CsvTransactionParser();
        var reader = new CsvMonthReader(parser);
        var aggregator = new DefaultSalesAggregator();
        model = new SalesModel(reader, aggregator);
        model.addListener(events::add);
        Clock clock = Clock.fixed(Instant.parse("2026-09-27T03:00:00Z"), ZoneId.of("Asia/Phnom_Penh"));
        controller = new ReportController(model, dataDir, clock, errors);
    }

    @Test
    void invalidMonthNeverReachesModel() {
        controller.monthEntered("09/2026");
        assertTrue(events.isEmpty());
        assertEquals(List.of("'09/2026' is not a month in the form yyyy-MM"), errors.shown);
    }

    @Test
    void validMonthLoadsModelAndClearsError() {
        controller.monthEntered("2026-09");
        assertEquals(1, events.size());
        assertEquals(List.of("<cleared>"), errors.shown);
    }

    @Test
    void missingFolderBecomesMessage() {
        controller.monthEntered("2026-07");
        assertTrue(events.isEmpty());
        assertEquals(1, errors.shown.size());
        assertTrue(errors.shown.getFirst().startsWith("No sales data found in "));
    }

    @Test
    void branchFilterNarrowsRows() {
        controller.monthEntered("2026-09");
        controller.branchChosen("rep");
        assertEquals(1, model.visibleTransactions().size());
        assertEquals("REP", model.branch());
    }
}
