package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class TxtAccountRepository implements AccountRepository {
    private static final Logger logger = LoggerFactory.getLogger(TxtAccountRepository.class);

    private static final String SEPARATOR = ";";

    private final Path file;

    public TxtAccountRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Account> findAll() {
        List<Account> accounts = new ArrayList<>();
        if (!Files.exists(file)) {
            return accounts;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    accounts.add(parse(line));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать файл " + file, e);
        }
        logger.debug("Прочитано счетов: {}", accounts.size());
        return accounts;
    }

    @Override
    public Account create(String owner) {
        if (owner.contains(SEPARATOR)) {
            throw new IllegalArgumentException("Имя владельца не может содержать '" + SEPARATOR + "'");
        }
        List<Account> accounts = findAll();
        long nextId = accounts.isEmpty() ? 1 : accounts.get(accounts.size() - 1).id() + 1;
        Account account = new Account(nextId, owner, BigDecimal.ZERO);

        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(toLine(account));
            writer.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось записать файл " + file, e);
        }
        logger.debug("Создан счёт {}", toLine(account));
        return account;
    }

    private static Account parse(String line) {
        String[] parts = line.split(SEPARATOR, -1);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Некорректная строка счёта: " + line);
        }
        return new Account(Long.parseLong(parts[0].trim()), parts[1], new BigDecimal(parts[2].trim()));
    }

    private static String toLine(Account account) {
        return account.id() + SEPARATOR + account.owner() + SEPARATOR + account.balance().toPlainString();
    }
}
