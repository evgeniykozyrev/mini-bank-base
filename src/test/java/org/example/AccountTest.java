package org.example;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void balanceIsNormalizedToTwoDecimals() {
        assertEquals("0.00", new Account(1, "Иван", BigDecimal.ZERO).balance().toPlainString());
        assertEquals("12.50", new Account(1, "Иван", new BigDecimal("12.5")).balance().toPlainString());
    }

    @Test
    void balanceIsRoundedHalfUp() {
        assertEquals(new BigDecimal("1.01"), new Account(1, "Иван", new BigDecimal("1.005")).balance());
    }
}
