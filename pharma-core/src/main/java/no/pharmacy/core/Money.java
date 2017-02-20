package no.pharmacy.core;

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
        return "kr " + (cents/100.0);
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
        return new Money(cents*100/amount);
    }

}
