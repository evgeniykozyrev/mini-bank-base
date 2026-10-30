package org.example;

import java.io.PrintStream;
import java.util.List;
import java.util.Optional;

public class CommandProcessor {

    private final AccountRepository repository;
    private final PrintStream out;

    public CommandProcessor(AccountRepository repository, PrintStream out) {
        this.repository = repository;
        this.out = out;
    }

    public boolean execute(String input) {
        String line = input.trim();
        if (line.isEmpty()) {
            return true;
        }
        String[] parts = line.split("\\s+", 2);
        String command = parts[0];
        String argument = parts.length > 1 ? parts[1].trim() : "";

        switch (command) {
            case "help" -> help();
            case "create" -> create(argument);
            case "list" -> list();
            case "show" -> show(argument);
            case "exit" -> {
                out.println("Завершение работы");
                return false;
            }
            default -> out.println("Ошибка: неизвестная команда '" + command + "'. Введите help");
        }
        return true;
    }

    private void help() {
        out.println("Доступные команды:");
        out.println("  help              - список команд");
        out.println("  create <владелец> - создать счёт");
        out.println("  list              - список счетов");
        out.println("  show <id>         - показать счёт");
        out.println("  exit              - выход");
    }

    private void create(String owner) {
        if (owner.isEmpty()) {
            out.println("Ошибка: укажите владельца");
            return;
        }
        try {
            Account account = repository.create(owner);
            out.println("Создан счёт: " + format(account));
        } catch (IllegalArgumentException e) {
            out.println("Ошибка: " + e.getMessage());
        }
    }

    private void list() {
        List<Account> accounts = repository.findAll();
        if (accounts.isEmpty()) {
            out.println("Счетов пока нет");
            return;
        }
        accounts.forEach(a -> out.println(format(a)));
    }

    private void show(String argument) {
        long id;
        try {
            id = Long.parseLong(argument);
        } catch (NumberFormatException e) {
            out.println("Ошибка: id должен быть числом");
            return;
        }
        Optional<Account> account = repository.findById(id);
        out.println(account.map(this::format).orElse("Счёт " + id + " не найден"));
    }

    private String format(Account account) {
        return account.id() + " | " + account.owner() + " | " + account.balance().toPlainString();
    }
}
