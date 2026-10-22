# Angkor Mart — Monthly Sales Report System (Lab-02)

This repository contains the running implementation of the Angkor Mart monthly sales reporting system using **Model-View-Controller (MVC)** and **Model-View-Presenter (MVP)** patterns in **Java 25** and **JavaFX 25.0.4**, along with a **Spring Boot 4.1.1** web reporting application.

---

## Commands to Run per Task

### Task 0 · Setup & Sample Data Generation
Compile the project and generate deterministic daily CSV sales files for `2026-08` and `2026-09`:
```bash
mvn compile
java -cp target/classes edu.itc.salesreport.ingest.SalesDataGenerator data
```
- **Generated output:**
  - `2026-08`: 93 files, 3,720 rows
  - `2026-09`: 90 files, 3,600 rows

---

### Task 1 · Observable Model & Console View
Run the toolkit-free console view to inspect monthly aggregates (`ALL` and `PNH` branch):
```bash
java -cp target/classes edu.itc.salesreport.ui.console.ConsoleReportView data
```
Run the `SalesModel` unit tests:
```bash
mvn test -Dtest=SalesModelTest
```

---

### Task 2 · JavaFX Desktop View (MVC Table & Chart)
Launch the JavaFX MVC Desktop application (shows 3,600 rows in TableView and 6 category bars in BarChart):
```bash
mvn javafx:run
```

---

### Task 3 · Controller & Input Validation
Launch the application with interactive controller validation:
```bash
mvn javafx:run
```
- **Test inputs in the UI:**
  - Type `2026/09` in Month field and press **Enter** ➔ Displays: `'2026/09' is not a month in the form yyyy-MM`
  - Type `2099-01` and press **Enter** ➔ Displays: `2099-01 is in the future`
  - Select branch **PNH** ➔ Filters table and category chart to PNH (Revenue: `$8455.90`, Receipts: `489`)

Run the input rules and controller unit tests:
```bash
mvn test -Dtest=InputRulesTest,ReportControllerTest
```

---

### Task 4 · Headless Test Suite & Architecture Verification
Run the complete automated test suite (all 28 tests run completely headless without opening a display or window):
```bash
mvn test
```
**Tests included:**
- `CsvTransactionParserTest` (8 tests)
- `InputRulesTest` (9 tests)
- `ReportControllerTest` (4 tests)
- `SalesModelTest` (4 tests)
- `ReportPresenterTest` (2 tests)
- `UiIndependenceTest` (1 architecture test verifying zero UI toolkit imports in core packages)

---

### Task 5 · MVP Variant (Passive View)
Run the Model-View-Presenter (MVP) variant where `FxReportScreen` is completely decoupled from `SalesModel` and driven by `ReportPresenter`:
```bash
mvn javafx:run "-Djavafx.mainClass=edu.itc.salesreport.ui.fx.ReportMvpApp"
```
Run the presenter tests (verifying formatted currency and status text headless):
```bash
mvn test -Dtest=ReportPresenterTest
```

---

### ★ Challenge · Spring MVC Web View
Prove UI-independence by running the Spring Boot web interface over the same, unchanged model.

1. **Install domain library to local repository:**
   ```bash
   mvn install -DskipTests
   ```
2. **Build and package `sales-web`:**
   ```bash
   cd sales-web
   mvn package -DskipTests
   ```
3. **Run the web application:**
   ```bash
   java -jar target/sales-web-0.2.0-SNAPSHOT.jar --salesreport.data-dir=../data
   ```
4. **Access in browser or terminal:**
   - Browser: [http://localhost:8080/report?month=2026-09&branch=PNH](http://localhost:8080/report?month=2026-09&branch=PNH)
   - Terminal:
     ```bash
     curl "http://localhost:8080/report?month=2026-09&branch=PNH"
     ```
5. **Run the Web MVC tests (`MockMvcTester`):**
   ```bash
   mvn test
   ```

---

## Full Project Verification
To verify the entire repository in one go (clean check):
```bash
# Verify desktop sales-report (headless)
mvn -q verify

# Verify sales-web
cd sales-web
mvn -q verify
```
