package edu.itc.salesreport.engine;

import edu.itc.salesreport.ingest.CsvTransactionParser;
import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.PaymentMethod;
import edu.itc.salesreport.model.ProductTotal;
import edu.itc.salesreport.model.SaleTransaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class DefaultSalesAggregator implements SalesAggregator {

    private record ProductKey(String sku, String name) {}

    @Override
    public BranchSummary summarize(String branch, List<SaleTransaction> rows) {
        BigDecimal revenue = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal discounts = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (var t : rows) {
            revenue = revenue.add(t.revenue());
            discounts = discounts.add(t.discount());
        }
        revenue = revenue.setScale(2, RoundingMode.HALF_UP);
        discounts = discounts.setScale(2, RoundingMode.HALF_UP);

        long receipts = rows.stream()
                .map(SaleTransaction::receiptNo)
                .distinct()
                .count();

        Map<String, BigDecimal> revenueByCategory = new TreeMap<>();
        for (String cat : CsvTransactionParser.CATEGORIES) {
            revenueByCategory.put(cat, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
        for (var t : rows) {
            revenueByCategory.merge(t.category(), t.revenue(), BigDecimal::add);
        }

        Map<PaymentMethod, BigDecimal> revenueByPayment = new EnumMap<>(PaymentMethod.class);
        for (PaymentMethod pm : PaymentMethod.values()) {
            revenueByPayment.put(pm, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
        for (var t : rows) {
            revenueByPayment.merge(t.paymentMethod(), t.revenue(), BigDecimal::add);
        }

        Map<ProductKey, BigDecimal> productRevenue = new HashMap<>();
        Map<ProductKey, Long> productQuantity = new HashMap<>();

        for (var t : rows) {
            ProductKey key = new ProductKey(t.sku(), t.productName());
            productRevenue.merge(key, t.revenue(), BigDecimal::add);
            productQuantity.merge(key, (long) t.quantity(), Long::sum);
        }

        List<ProductTotal> topProducts = productRevenue.entrySet().stream()
                .map(e -> new ProductTotal(
                        e.getKey().sku(),
                        e.getKey().name(),
                        e.getValue().setScale(2, RoundingMode.HALF_UP),
                        productQuantity.get(e.getKey())))
                .sorted(Comparator.comparing(ProductTotal::revenue).reversed()
                        .thenComparing(ProductTotal::sku))
                .limit(10)
                .toList();

        return new BranchSummary(
                branch,
                revenue,
                discounts,
                receipts,
                revenueByCategory,
                topProducts,
                revenueByPayment);
    }
}
