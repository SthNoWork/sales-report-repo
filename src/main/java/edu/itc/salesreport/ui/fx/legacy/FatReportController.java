package edu.itc.salesreport.ui.fx.legacy;

import edu.itc.salesreport.ui.ReportController;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Refactored legacy controller: UI glue that delegates input events to ReportController.
 */
public class FatReportController {
    private final TextField monthField;
    private final ReportController controller;

    public FatReportController(TextField monthField, ComboBox<String> branchBox, Label status, ReportController controller) {
        this.monthField = monthField;
        this.controller = controller;
    }

    public FatReportController(TextField monthField, ReportController controller) {
        this.monthField = monthField;
        this.controller = controller;
    }

    public void onLoad() {
        controller.monthEntered(monthField.getText());
    }
}
