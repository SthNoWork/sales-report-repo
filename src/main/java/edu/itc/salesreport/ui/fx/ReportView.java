package edu.itc.salesreport.ui.fx;

import edu.itc.salesreport.model.SaleTransaction;
import edu.itc.salesreport.ui.ErrorDisplay;
import edu.itc.salesreport.ui.ReportController;
import edu.itc.salesreport.ui.SalesEvent;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.SalesModelListener;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Function;

/** MVC view: observes SalesModel, forwards gestures to the controller. */
public final class ReportView implements SalesModelListener, ErrorDisplay {
    private final SalesModel model;
    private final BorderPane root = new BorderPane();
    private final TextField monthField = new TextField("2026-09");
    private final ComboBox<String> branchBox = new ComboBox<>();
    private final TableView<SaleTransaction> table = new TableView<>();
    private final BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
    private final Label status = new Label("No month loaded");
    private final Label error = new Label();

    public ReportView(SalesModel model) {
        this.model = model;
        branchBox.getItems().setAll("ALL", "PNH", "REP", "BTB");
        branchBox.setValue("ALL");

        table.getColumns().setAll(List.of(
                column("Date", t -> t.date().toString()),
                column("Receipt", SaleTransaction::receiptNo),
                column("Product", SaleTransaction::productName),
                column("Category", SaleTransaction::category),
                column("Qty", t -> Integer.toString(t.quantity())),
                column("Revenue", t -> t.revenue().toString()),
                column("Discount", t -> t.discount().toString())
        ));

        chart.setTitle("Revenue by category (USD)");
        chart.setLegendVisible(false);
        chart.setAnimated(false);

        error.setStyle("-fx-text-fill: firebrick;");

        HBox filters = new HBox(10, new Label("Month"), monthField, new Label("Branch"), branchBox);
        filters.setPadding(new Insets(10));
        filters.setAlignment(Pos.CENTER_LEFT);

        SplitPane split = new SplitPane(table, chart);
        split.setDividerPositions(0.6);

        VBox bottom = new VBox(5, status, error);
        bottom.setPadding(new Insets(10));

        root.setTop(filters);
        root.setCenter(split);
        root.setBottom(bottom);

        model.addListener(this);
    }

    public void bind(ReportController controller) {
        monthField.setOnAction(e -> controller.monthEntered(monthField.getText()));
        branchBox.setOnAction(e -> controller.branchChosen(branchBox.getValue()));
    }

    public Parent root() {
        return root;
    }

    public TextField monthField() {
        return monthField;
    }

    public ComboBox<String> branchBox() {
        return branchBox;
    }

    public Label statusLabel() {
        return status;
    }

    public Label errorLabel() {
        return error;
    }

    public TableView<SaleTransaction> table() {
        return table;
    }

    public BarChart<String, Number> chart() {
        return chart;
    }

    @Override
    public void modelChanged(SalesEvent event) {
        branchBox.setValue(model.branch());
        table.getItems().setAll(model.visibleTransactions());

        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        model.summary().revenueByCategory().forEach((cat, rev) ->
                series.getData().add(new XYChart.Data<>(cat, rev)));
        chart.getData().add(series);

        var s = model.summary();
        String statusText = switch (event) {
            case SalesEvent.MonthLoaded m ->
                "%s: %d rows, %d rejected · revenue USD %s · receipts %d".formatted(
                        m.month(), m.rows(), m.rejected(), s.revenue(), s.receipts());
            case SalesEvent.BranchSelected b ->
                "%s · revenue USD %s · receipts %d".formatted(
                        b.branch(), s.revenue(), s.receipts());
        };
        if (!model.rejectedRows().isEmpty()) {
            statusText += "\nrejected row: " + model.rejectedRows().getFirst();
        }
        status.setText(statusText);
    }

    @Override
    public void showError(String message) {
        error.setText(message);
    }

    @Override
    public void clearError() {
        error.setText("");
    }

    // records have no JavaFX properties: adapt each column with a lambda
    private static TableColumn<SaleTransaction, String> column(String title, Function<SaleTransaction, String> value) {
        var col = new TableColumn<SaleTransaction, String>(title);
        col.setCellValueFactory(c -> new ReadOnlyStringWrapper(value.apply(c.getValue())));
        return col;
    }
}
