package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Account(long id, String owner, BigDecimal balance) {

    public Account {
        balance = balance.setScale(2, RoundingMode.HALF_UP);
    }
}
