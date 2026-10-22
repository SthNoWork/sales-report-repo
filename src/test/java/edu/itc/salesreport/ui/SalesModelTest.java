package edu.itc.salesreport.ui;

import edu.itc.salesreport.TestData;
import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SalesModelTest {

    @TempDir
    Path dataDir;

    private SalesModel model;
    private List<SalesEvent> events;

    @BeforeEach
    void setUp() throws IOException {
        TestData.writeSample(dataDir);
        var parser = new CsvTransactionParser();
        var reader = new CsvMonthReader(parser);
        var aggregator = new DefaultSalesAggregator();
        model = new SalesModel(reader, aggregator);
        events = new ArrayList<>();
        model.addListener(events::add);
    }

    @Test
    void loadFiresMonthLoadedAndCalculatesChainRevenue() throws IOException {
        model.load(dataDir, YearMonth.of(2026, 9));

        assertEquals(1, events.size());
        assertEquals(new SalesEvent.MonthLoaded(YearMonth.of(2026, 9), 3, 1), events.getFirst());
        assertEquals(new BigDecimal("52.00"), model.summary().revenue());
    }

    @Test
    void selectingBranchTwiceFiresOnceAndGivesRevenueAndBasket() throws IOException {
        model.load(dataDir, YearMonth.of(2026, 9));
        events.clear();

        model.selectBranch("PNH");
        model.selectBranch("PNH");

        assertEquals(1, events.size());
        assertEquals(new SalesEvent.BranchSelected("PNH"), events.getFirst());
        assertEquals(new BigDecimal("31.00"), model.summary().revenue());
        assertEquals(new BigDecimal("31.00"), model.summary().averageBasket());
    }

    @Test
    void failedLoadThrowsNoSuchFileExceptionFiresNothingAndKeepsOldData() throws IOException {
        model.load(dataDir, YearMonth.of(2026, 9));
        events.clear();

        assertThrows(NoSuchFileException.class, () -> model.load(dataDir, YearMonth.of(2026, 7)));

        assertTrue(events.isEmpty());
        assertEquals(3, model.visibleTransactions().size());
    }

    @Test
    void removedListenerIsNotCalled() throws IOException {
        model.load(dataDir, YearMonth.of(2026, 9));
        List<SalesEvent> removedListenerEvents = new ArrayList<>();
        SalesModelListener listener = removedListenerEvents::add;
        model.addListener(listener);

        model.removeListener(listener);
        model.selectBranch("REP");

        assertTrue(removedListenerEvents.isEmpty());
    }
}
