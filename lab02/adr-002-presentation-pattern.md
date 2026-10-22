# ADR-002: Presentation Pattern Selection for Report Viewer

## Status
Accepted

## Context
In Lab-02, the Angkor Mart engineering team evaluated two architectural patterns for the desktop and upcoming reporting interfaces:
1. **Model-View-Controller (MVC):** The view directly observes the domain `SalesModel` and queries domain entities (`SaleTransaction`, `BranchSummary`) to format table cells, status strings, and chart data. The controller handles input validation and dispatches commands to the model.
2. **Model-View-Presenter (MVP - Passive View):** The view (`FxReportScreen`) is completely decoupled from the domain model and contains only dumb widgets and property setters. The presenter (`ReportPresenter`) handles user gestures, orchestrates model queries, formats currency and status text into strings and passive DTOs (`RowItem`), and pushes them into the view.

We evaluated both patterns against our Lab-01 Quality Attribute scenarios:
- **Testability Scenario:** "A developer modifies or adds display formatting rules (e.g., currency formatting, status summaries), and verifies them in under 1 second in an automated headless CI pipeline without starting a JavaFX display toolkit or windowing server."
- **Modifiability Scenario:** "A new reporting client (e.g., Spring MVC web page or CLI) is added in under 2 days without duplicating formatting logic or modifying core domain models."

## Decision
We select **Model-View-Presenter (MVP) with Passive View** as the primary presentation architecture for the Angkor Mart desktop application starting in Lab-03, while maintaining the pure, toolkit-free `SalesModel` as the common observable domain model.

## Consequences
### Positive
- **100% Headless Testability of UI Logic:** Formatted text (currency with thousand separators, basket averages, and status lines) can be tested entirely with plain JUnit tests via `FakeScreen` in less than 0.1s. No `Platform.startup`, display server, or TestFX is required.
- **Strict Decoupling:** The UI widgets have zero knowledge of the domain model. Changes to domain records do not ripple directly into widget hierarchies.
- **Multi-Screen Reuse:** A secondary screen (such as console, Swing, or web) can reuse `ReportPresenter` by implementing the simple 4-method `ReportScreen` interface.

### Negative / Trade-offs
- **Additional Indirection & Boilerplate:** Adding a new column requires updating `RowItem`, the presenter mapper, and the view, compared to modifying only the view in MVC.
- **Dual Pattern Maintenance:** Web frameworks like Spring Boot naturally adopt MVC for request/response dispatching; however, desktop and rich-client interfaces will use MVP for presentation and MVC for request handling.
