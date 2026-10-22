# MVC vs. MVP Comparison

| Criterion | MVC (ReportView + ReportController) | MVP (FxReportScreen + ReportPresenter) |
|---|---|---|
| **Lines of code (view / controller-presenter)** | 154 / 50 | 127 / 79 |
| **Tests that run without JavaFX** | 4 (Controller) + 9 (Rules) + 4 (Model) = 17 | 2 (Presenter) + 4 (Controller) + 9 (Rules) + 4 (Model) = 19 (all 28 suite tests headless) |
| **Where money and status text are formatted** | `ReportView.java` (inside UI event handler) | `ReportPresenter.java` (pure Java, headless) |
| **Files changed to add a "Discount" column** | 1 (`ReportView.java`) | 3 (`RowItem.java`, `ReportPresenter.java`, `FxReportScreen.java`) |
| **Does the view hold a model reference?** | Yes (`ReportView` holds `SalesModel`) | No (`FxReportScreen` has zero knowledge of `SalesModel`) |
| **Effort for a second screen (web, Swing)** | Moderate (new view must observe `SalesModel` and format all data itself) | Low (new screen only implements `ReportScreen` and renders pre-formatted DTOs) |

---

## Detailed Analysis

### 1. Testability
- **MVC:** The controller (`ReportController`) is cleanly separated and can be tested headlessly using `FakeErrors`. However, the view (`ReportView`) observes `SalesModel` directly and formats strings, numbers, and chart series inside its `modelChanged` method. Because `ReportView` creates and manipulates JavaFX nodes, verifying that status strings or totals are formatted correctly requires a running JavaFX toolkit or TestFX.
- **MVP:** The view (`FxReportScreen`) is completely passive. All formatting logic (currency with commas, basket division, status text composition) lives in `ReportPresenter`. By using `FakeScreen`, 100% of the display text and row formatting can be tested using standard JUnit 5 tests running in under 100 milliseconds without any graphical environment.

### 2. Modifiability & Impact of Schema Changes
- **MVC:** Adding a new column like "Discount" is localized to `ReportView.java` because the view directly accesses the domain record `SaleTransaction.discount()`.
- **MVP:** Adding a new column requires changing the presentation contract: the DTO `RowItem`, the mapping in `ReportPresenter`, and the column definition in `FxReportScreen`. This is a tradeoff: MVP increases strict separation and testability at the cost of additional boilerplate across three files.

### 3. Decoupling & Second Screen Reusability
- **MVC:** Each view holds a direct reference to `SalesModel`. If a Swing or web interface is added, the new view must re-implement the formatting of currency, baskets, and status strings.
- **MVP:** Any new presentation technology (Swing, CLI, or HTML) simply implements `ReportScreen`. The presentation logic, formatting, and event flow are fully shared.
