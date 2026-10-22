package edu.itc.salesreport.web;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = ReportWebController.class, properties = "salesreport.data-dir=target/test-data")
@Import(SalesWebApp.class)
class ReportWebControllerTest {

    @Autowired
    MockMvcTester mvc;

    @BeforeAll
    static void setUpTestData() throws IOException {
        Path dir = Files.createDirectories(Path.of("target", "test-data", "2026-09"));
        String header = "branch,date,receipt_no,sku,product_name,category,quantity,unit_price,discount,payment_method";
        Files.writeString(dir.resolve("PNH-2026-09-01.csv"), header + "\n"
                + "PNH,2026-09-01,PNH-000001,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR\n");
        Files.writeString(dir.resolve("REP-2026-09-01.csv"), header + "\n"
                + "REP,2026-09-01,REP-000001,SKU-4003,Sunscreen SPF50,Personal Care,3,7.50,1.50,CASH\n");
    }

    @Test
    void rendersSummaryOfOneBranch() {
        assertThat(mvc.get().uri("/report?month=2026-09&branch=PNH"))
                .hasStatusOk()
                .hasViewName("report")
                .bodyText().contains("Revenue USD 13.00", "Jasmine Rice 5kg");
    }

    @Test
    void rendersErrorForInvalidMonth() {
        assertThat(mvc.get().uri("/report?month=2026-13&branch=ALL"))
                .hasStatusOk()
                .hasViewName("report")
                .bodyText().contains("'2026-13' is not a month in the form yyyy-MM");
    }
}
