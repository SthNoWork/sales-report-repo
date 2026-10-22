package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.PaymentMethod;
import edu.itc.salesreport.model.SaleTransaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvTransactionParserTest {

    private final CsvTransactionParser parser = new CsvTransactionParser();

    @Test
    void canonicalRowParsesCorrectly() throws InvalidRowException {
        String line = "PNH,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR";
        SaleTransaction t = parser.parse(line);

        assertEquals("PNH", t.branch());
        assertEquals(LocalDate.of(2026, 9, 1), t.date());
        assertEquals("PNH-000123", t.receiptNo());
        assertEquals("SKU-1001", t.sku());
        assertEquals("Jasmine Rice 5kg", t.productName());
        assertEquals("Grocery", t.category());
        assertEquals(2, t.quantity());
        assertEquals(new BigDecimal("6.50"), t.unitPrice());
        assertEquals(new BigDecimal("0.00"), t.discount());
        assertEquals(PaymentMethod.KHQR, t.paymentMethod());
        assertEquals(new BigDecimal("13.00"), t.revenue());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "XYZ,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR", // unknown branch
            "PNH,2026-09-32,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR", // bad date
            "PNH,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,0,6.50,0.00,KHQR",  // quantity 0
            "PNH,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,15.00,KHQR", // discount exceeds amount
            "PNH,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Toys,2,6.50,0.00,KHQR",    // unknown category
            "PNH,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,BITCOIN", // unknown payment
            "PNH,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00"        // missing fields
    })
    void rejectsInvalidRows(String invalidLine) {
        assertThrows(InvalidRowException.class, () -> parser.parse(invalidLine));
    }
}
