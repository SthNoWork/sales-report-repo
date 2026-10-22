# Refactoring the Legacy Fat Controller

## 1. Smells Identified and Consequences

### Smell 1: Violation of Single Responsibility Principle (Fat Handler / God Controller)
- **Description:** The `onLoad()` method in `FatReportController` performs UI event handling, file system traversal, CSV parsing, business filtering, revenue math, and UI label updating all in a single method.
- **Consequence:** Massive coupling prevents code reuse (e.g., console or web views cannot reuse the aggregation logic). Any change to business rules, file format, or UI layout requires editing the same monolithic class.

### Smell 2: Missing Input Validation and Swallowed Exceptions
- **Description:** Raw text from `monthField.getText()` is directly resolved into `Path.of("data", ...)`. Any error is caught by `catch (Exception e)` which unconditionally displays `"Error"`.
- **Consequence:** Users receive no actionable feedback for invalid syntax (`2026/09`), invalid months (`2026-13`), missing data directories (`2026-07`), or future dates. Failures are opaque and indistinguishable.

### Smell 3: Primitive Obsession and Lack of Domain Model
- **Description:** Data is represented as raw string arrays (`String[] c = line.split(",")`) with hardcoded positional indices (`c[0]`, `c[6]`, `c[7]`, `c[8]`).
- **Consequence:** Fragile to format changes; no encapsulation of data integrity rules (e.g., records, enums for payment methods, or domain types).

### Smell 4: Silent Data Corruption on Invalid Rows
- **Description:** Raw numbers are parsed and multiplied directly without validating quantity positivity, valid branches, categories, or discount bounds.
- **Consequence:** When an invalid transaction row with negative quantity is encountered (`quantity = -1, unitPrice = 6.50`), the handler calculates `-1 * 6.50 = -6.50` and adds it to the revenue, silently deducting $6.50 from Angkor Mart's actual revenue without raising an error or reporting the corrupted row.

### Smell 5: Direct Coupling to GUI Framework (JavaFX)
- **Description:** The controller directly holds references to `javafx.scene.control.TextField`, `ComboBox`, and `Label`.
- **Consequence:** Impossible to unit test with plain JUnit without starting a full JavaFX desktop environment or graphics context. Automated CI builds without a display cannot execute tests on this logic.

### Smell 6: Blocking I/O on UI Thread
- **Description:** File system operations (`Files.list(dir)` and `Files.readAllLines(f)`) are executed directly inside the UI event handler.
- **Consequence:** Reading hundreds of files causes the UI thread to stall, freezing the desktop interface.

---

## 2. Quantitative and Behavioral Comparison

| Criterion | Fat Controller (`FatReportController`) | After Refactoring (`ReportController` + Model) |
|---|---|---|
| **2026-09 ALL, broken row present** | `Revenue: 26715.40` (wrong, silent deduction) | `USD 26721.90 · 1 rejected` (accurate total + error logged) |
| **Month `2026/09`** | `"Error"` | `"'2026/09' is not a month in the form yyyy-MM"` |
| **Month `2026-07` (missing)** | `"Error"` | `"No sales data found in data\2026-07"` |
| **Lines of code in handler (`onLoad`)** | 20 lines | 1 line (`controller.monthEntered(monthField.getText());`) |
| **Tests possible without JavaFX** | 0 | 26 tests (parser, model, controller, rules all headless) |

---

## 3. Refactored Solution Architecture

The refactored `FatReportController` is reduced to an adapter that simply extracts the raw input text from the UI widget and forwards it to `ReportController.monthEntered(text)`. The business logic, file I/O, domain validation, and aggregation are delegated to `InputRules`, `CsvMonthReader`, `SalesModel`, and `DefaultSalesAggregator`, which are completely decoupled from JavaFX.
