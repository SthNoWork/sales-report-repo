# Lab-02: Model View Controller (MVC) Design Pattern

## Reused Lab-01 Inputs
- **Team Repository and Roles:** Fallback used (`sales-report-repo`). Collaborator: `hengrith`.
- **Quality Attribute Scenarios:** Fallback used:
  1. *Testability Scenario:* A new reporting metric or display column is added and tested in under 1 second without running JavaFX or opening a window.
  2. *Modifiability Scenario:* A new presentation view (e.g. CLI, Spring MVC web page) is added in under 2 days without modifying domain models or CSV parsing logic.
- **C4 Container Diagram:** Fallback used: "Report viewer (JavaFX desktop)".
- **ADR Template:** Title, Status, Context, Decision, Consequences.

---

## File Deliverables Summary

| File Path | Task | Author | Description |
|---|---|---|---|
| `pom.xml` | Task 0 | hengrith | Maven POM configured with Java 25, OpenJFX 25.0.4, JUnit 6.1.3 BOM |
| `.gitignore` | Task 0 | hengrith | Git ignore configuration ignoring `target/` and `data/` |
| `src/main/java/edu/itc/salesreport/model/PaymentMethod.java` | Task 0 | hengrith | Enum for payment methods (`CASH`, `CARD`, `KHQR`) |
| `src/main/java/edu/itc/salesreport/model/SaleTransaction.java` | Task 0 | hengrith | Canonical domain record representing a line item and its `revenue()` |
| `src/main/java/edu/itc/salesreport/model/ProductTotal.java` | Task 0 | hengrith | Domain record for aggregated product totals |
| `src/main/java/edu/itc/salesreport/model/BranchSummary.java` | Task 0 | hengrith | Immutable summary of monthly sales metrics and average basket |
| `src/main/java/edu/itc/salesreport/ingest/InvalidRowException.java` | Task 0 | hengrith | Exception holding rejected line and failure reason |
| `src/main/java/edu/itc/salesreport/ingest/TransactionParser.java` | Task 0 | hengrith | Functional parser interface |
| `src/main/java/edu/itc/salesreport/ingest/CsvTransactionParser.java` | Task 0 | hengrith | Strict CSV parser enforcing validation rules and bounds |
| `src/main/java/edu/itc/salesreport/ingest/SalesDataGenerator.java` | Task 0 | hengrith | Deterministic seeded sample data generator |
| `src/main/java/edu/itc/salesreport/ingest/MonthData.java` | Task 1 | hengrith | Record containing parsed transactions and rejected line messages |
| `src/main/java/edu/itc/salesreport/ingest/CsvMonthReader.java` | Task 1 | hengrith | Reader listing daily branch CSV files and collecting parse failures |
| `src/main/java/edu/itc/salesreport/engine/SalesAggregator.java` | Task 1 | hengrith | Interface for summarizing transactions |
| `src/main/java/edu/itc/salesreport/engine/DefaultSalesAggregator.java` | Task 1 | hengrith | Aggregator computing totals, categories, payments, and top 10 products |
| `src/main/java/edu/itc/salesreport/ui/SalesEvent.java` | Task 1 | hengrith | Sealed events `MonthLoaded` and `BranchSelected` |
| `src/main/java/edu/itc/salesreport/ui/SalesModelListener.java` | Task 1 | hengrith | Listener interface for model changes |
| `src/main/java/edu/itc/salesreport/ui/SalesModel.java` | Task 1 | hengrith | Toolkit-free observable MVC model |
| `src/main/java/edu/itc/salesreport/ui/console/ConsoleReportView.java` | Task 1 | hengrith | Headless console observer view |
| `src/test/java/edu/itc/salesreport/TestData.java` | Task 1 | hengrith | Canonical sample test dataset generator |
| `src/test/java/edu/itc/salesreport/ui/SalesModelTest.java` | Task 1 | hengrith | 4 unit tests verifying model event firing and calculations |
| `src/main/java/edu/itc/salesreport/ui/ErrorDisplay.java` | Task 2 | hengrith | Toolkit-free contract for displaying and clearing errors |
| `src/main/java/edu/itc/salesreport/ui/fx/ReportView.java` | Task 2 | hengrith | JavaFX MVC view observing `SalesModel` |
| `src/main/java/edu/itc/salesreport/ui/fx/ReportApp.java` | Task 2 | hengrith | JavaFX Application entry point for MVC |
| `lab02/screenshots/mvc-viewer.png` | Task 2 | hengrith | Screenshot of running MVC desktop report viewer |
| `src/main/java/edu/itc/salesreport/ui/Validation.java` | Task 3 | hengrith | Sealed validation result (`Valid`, `Invalid`) |
| `src/main/java/edu/itc/salesreport/ui/InputRules.java` | Task 3 | hengrith | Toolkit-free pure input parsing and validation rules |
| `src/main/java/edu/itc/salesreport/ui/ReportCommand.java` | Task 3 | hengrith | Sealed command hierarchy (`LoadMonth`, `SelectBranch`) |
| `src/main/java/edu/itc/salesreport/ui/ReportController.java` | Task 3 | hengrith | Toolkit-free controller converting inputs to commands and handling errors |
| `lab02/screenshots/validation.png` | Task 3 | hengrith | Screenshot of desktop viewer showing validation error message |
| `src/test/java/edu/itc/salesreport/ingest/CsvTransactionParserTest.java` | Task 4 | hengrith | 8 tests verifying parser validation and rejection rules |
| `src/test/java/edu/itc/salesreport/ui/InputRulesTest.java` | Task 4 | hengrith | 9 tests verifying input rules with fixed Clock |
| `src/test/java/edu/itc/salesreport/ui/ReportControllerTest.java` | Task 4 | hengrith | 4 tests verifying controller logic using FakeErrors |
| `src/test/java/edu/itc/salesreport/UiIndependenceTest.java` | Task 4 | hengrith | Architecture test ensuring core packages contain no UI imports |
| `src/main/java/edu/itc/salesreport/ui/fx/legacy/FatReportController.java` | Task 4 | hengrith | Legacy fat controller refactored into a thin adapter |
| `lab02/refactoring.md` | Task 4 | hengrith | Analysis of 6 code smells, consequences, and before/after metrics |
| `src/main/java/edu/itc/salesreport/ui/mvp/RowItem.java` | Task 5 | hengrith | Passive formatted table item DTO |
| `src/main/java/edu/itc/salesreport/ui/mvp/ReportScreen.java` | Task 5 | hengrith | Passive view interface for MVP |
| `src/main/java/edu/itc/salesreport/ui/mvp/ReportPresenter.java` | Task 5 | hengrith | MVP Presenter formatting currency and status strings headlessly |
| `src/main/java/edu/itc/salesreport/ui/fx/FxReportScreen.java` | Task 5 | hengrith | Passive JavaFX screen with zero knowledge of domain model |
| `src/main/java/edu/itc/salesreport/ui/fx/ReportMvpApp.java` | Task 5 | hengrith | JavaFX Application entry point for MVP |
| `src/test/java/edu/itc/salesreport/ui/mvp/ReportPresenterTest.java` | Task 5 | hengrith | 2 tests verifying presenter formatted outputs headlessly |
| `lab02/mvc-vs-mvp.md` | Task 5 | hengrith | Quantitative and architectural comparison table |
| `lab02/adr-002-presentation-pattern.md` | Task 5 | hengrith | Architecture Decision Record selecting MVP |
| `lab02/README.md` | Task 5 | hengrith | Lab deliverables documentation |

---

## Change Log

| Date | Author | Description |
|---|---|---|
| 2026-10-22 | hengrith | Completed Lab-02 Tasks 0–5: Implemented toolkit-free SalesModel, CsvMonthReader, DefaultSalesAggregator, JavaFX ReportView, ReportController with InputRules validation, headless unit tests (28 tests), refactored FatReportController, implemented MVP variant with ReportPresenter and FxReportScreen, generated screenshots, and completed architectural comparison and ADR. |
