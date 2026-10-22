package edu.itc.salesreport.ui.mvp;

import java.util.Objects;

public record RowItem(
        String date,
        String receipt,
        String product,
        String category,
        String quantity,
        String revenue,
        String discount) {

    public RowItem(String date, String receipt, String product, String category, String quantity, String revenue) {
        this(date, receipt, product, category, quantity, revenue, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RowItem other)) return false;
        boolean base = Objects.equals(date, other.date)
                && Objects.equals(receipt, other.receipt)
                && Objects.equals(product, other.product)
                && Objects.equals(category, other.category)
                && Objects.equals(quantity, other.quantity)
                && Objects.equals(revenue, other.revenue);
        if (!base) return false;
        if (this.discount == null || other.discount == null) return true;
        return Objects.equals(this.discount, other.discount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, receipt, product, category, quantity, revenue);
    }
}
