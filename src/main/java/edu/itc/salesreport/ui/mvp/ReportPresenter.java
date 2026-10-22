package edu.itc.salesreport.ui.mvp;

import edu.itc.salesreport.model.SaleTransaction;
import edu.itc.salesreport.ui.InputRules;
import edu.itc.salesreport.ui.SalesEvent;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.SalesModelListener;
import edu.itc.salesreport.ui.Validation;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import java.util.Locale;

/** MVP presenter: handles input and pushes formatted data to passive screen. */
public final class ReportPresenter implements SalesModelListener {
    private final SalesModel model;
    private final ReportScreen screen;
    private final Path dataDir;
    private final Clock clock;

    public ReportPresenter(SalesModel model, ReportScreen screen, Path dataDir, Clock clock) {
        this.model = model;
        this.screen = screen;
        this.dataDir = dataDir;
        this.clock = clock;
        model.addListener(this);
    }

    public void monthEntered(String text) {
        switch (InputRules.month(text, clock)) {
            case Validation.Valid(var month) -> {
                try {
                    model.load(dataDir, month);
                    screen.showError("");
                } catch (IOException e) {
                    screen.showError("No sales data for " + month);
                }
            }
            case Validation.Invalid(var message) -> screen.showError(message);
        }
    }

    public void branchChosen(String text) {
        switch (InputRules.branch(text)) {
            case Validation.Valid(var branch) -> {
                model.selectBranch(branch);
                screen.showError("");
            }
            case Validation.Invalid(var message) -> screen.showError(message);
        }
    }

    @Override
    public void modelChanged(SalesEvent event) {
        var s = model.summary();
        List<RowItem> rows = model.visibleTransactions().stream()
                .map(t -> new RowItem(
                        t.date().toString(),
                        t.receiptNo(),
                        t.productName(),
                        t.category(),
                        Integer.toString(t.quantity()),
                        money(t.revenue()),
                        money(t.discount())))
                .toList();
        screen.showRows(rows);
        screen.showCategoryRevenue(s.revenueByCategory());
        screen.showStatus("%s · %d receipts · revenue USD %s · basket USD %s"
                .formatted(s.branch(), s.receipts(), money(s.revenue()), money(s.averageBasket())));
        screen.showError("");
    }

    public static String money(BigDecimal amount) {
        return String.format(Locale.US, "%,.2f", amount);
    }
}
