package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TxtAccountRepositoryTest {

    @TempDir
    Path dir;

    private Path file;
    private AccountRepository repository;

    @BeforeEach
    void setUp() {
        file = dir.resolve("accounts.txt");
        repository = new TxtAccountRepository(file);
    }

    @Test
    void findAllReturnsEmptyListWhenFileMissingAndDoesNotCreateIt() {
        assertTrue(repository.findAll().isEmpty());
        assertFalse(Files.exists(file));
    }

    @Test
    void createMakesFileWithZeroBalance() throws IOException {
        Account account = repository.create("Иван");

        assertEquals(1, account.id());
        assertEquals(new BigDecimal("0.00"), account.balance());
        assertEquals(List.of("1;Иван;0.00"), Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    void createAssignsIdOneGreaterThanLast() throws IOException {
        repository.create("Иван");
        repository.create("Мария");
        Account third = repository.create("Пётр");

        assertEquals(3, third.id());
        assertEquals(List.of("1;Иван;0.00", "2;Мария;0.00", "3;Пётр;0.00"),
                Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    void createContinuesFromLastIdInExistingFile() throws IOException {
        Files.writeString(file, "1;А;0.00\n5;Б;10.00\n", StandardCharsets.UTF_8);

        assertEquals(6, repository.create("В").id());
    }

    @Test
    void findByIdReturnsAccount() {
        repository.create("Иван");
        repository.create("Мария");

        Account found = repository.findById(2).orElseThrow();
        assertEquals("Мария", found.owner());
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        repository.create("Иван");
        assertTrue(repository.findById(42).isEmpty());
    }

    @Test
    void readsDataWrittenByAnotherRepositoryInstance() {
        repository.create("Иван");
        assertEquals(1, new TxtAccountRepository(file).findAll().size());
    }

    @Test
    void fileIsReleasedAfterEachOperation() throws IOException {
        repository.create("Иван");
        repository.findAll();
        Files.delete(file);
        assertFalse(Files.exists(file));
    }

    @Test
    void createRejectsSeparatorInOwner() {
        assertThrows(IllegalArgumentException.class, () -> repository.create("Иван;100"));
        assertFalse(Files.exists(file));
    }

    @Test
    void findAllReadsAccountsFromExistingFile() throws IOException {
        Files.writeString(file, "7;Мария Петрова;12.50\n", StandardCharsets.UTF_8);

        assertEquals(List.of(new Account(7, "Мария Петрова", new BigDecimal("12.50"))), repository.findAll());
    }

    @Test
    void existingLinesStayUnchangedAfterCreate() throws IOException {
        Files.writeString(file, "3;Пётр;100.05\n", StandardCharsets.UTF_8);

        repository.create("Иван");

        assertEquals(List.of("3;Пётр;100.05", "4;Иван;0.00"), Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    void findAllRejectsMalformedLine() throws IOException {
        Files.writeString(file, "1;Иван\n", StandardCharsets.UTF_8);

        assertThrows(IllegalArgumentException.class, repository::findAll);
    }
}
