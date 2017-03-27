package no.pharmacy.core;

import java.math.BigDecimal;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = "cents")
public class Money {

    private static final Money ZERO = new Money(0);
    private int cents;

    private Money(int cents) {
        this.cents = cents;
    }

    public static Money inCents(int cents) {
        return new Money(cents);
    }

    public Money plusCents(int cents) {
        return new Money(this.cents + cents);
    }

    @Override
    public String toString() {
        return "kr " + format();
    }

    public Money plus(Money other) {
        return new Money(cents + other.cents);
    }

    public Money minus(Money other) {
        return new Money(cents - other.cents);
    }

    public static Money zero() {
        return ZERO;
    }

    public boolean isGreaterThan(Money o) {
        return cents > o.cents;
    }

    public Money times(int times) {
        return new Money(cents*times);
    }

    public Money percent(int amount) {
        return new Money(cents*amount/100);
    }

    public static Money from(BigDecimal decimal) {
        return decimal != null ? new Money(decimal.movePointRight(2).intValue()) : null;
    }

    public BigDecimal toBigDecimal() {
        return new BigDecimal(cents).scaleByPowerOfTen(-2);
    }

    public static Money from(String amount) {
        return amount != null && !amount.isEmpty() ? from(new BigDecimal(amount)) : null;
    }

    public String format() {
        return String.valueOf(cents/100.0);
    }

}
