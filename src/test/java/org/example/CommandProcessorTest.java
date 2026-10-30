package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandProcessorTest {

    @TempDir
    Path dir;

    private Path file;
    private ByteArrayOutputStream buffer;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        file = dir.resolve("accounts.txt");
        buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        processor = new CommandProcessor(new TxtAccountRepository(file), out);
    }

    private String output() {
        String text = buffer.toString(StandardCharsets.UTF_8);
        buffer.reset();
        return text;
    }

    @Test
    void helpListsAllCommands() {
        assertTrue(processor.execute("help"));
        String text = output();
        for (String cmd : List.of("help", "create", "list", "show", "exit")) {
            assertTrue(text.contains(cmd), "help не содержит " + cmd);
        }
    }

    @Test
    void listWithoutFilePrintsNoAccounts() {
        processor.execute("list");
        assertEquals("Счетов пока нет", output().trim());
        assertFalse(Files.exists(file));
    }

    @Test
    void createWritesAccountToFile() throws IOException {
        processor.execute("create Иван");

        assertTrue(output().contains("1 | Иван | 0.00"));
        assertEquals(List.of("1;Иван;0.00"), Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    void createKeepsOwnerWithSpaces() throws IOException {
        processor.execute("create Иван Иванов");
        assertEquals(List.of("1;Иван Иванов;0.00"), Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    void createWithoutOwnerPrintsErrorAndDoesNotCreateFile() {
        processor.execute("create");
        assertTrue(output().startsWith("Ошибка"));
        assertFalse(Files.exists(file));
    }

    @Test
    void createRejectsSeparatorInOwner() {
        processor.execute("create Иван;100");
        assertTrue(output().startsWith("Ошибка"));
        assertFalse(Files.exists(file));
    }

    @Test
    void listPrintsAllAccounts() {
        processor.execute("create Иван");
        processor.execute("create Мария");
        output();

        processor.execute("list");
        String[] lines = output().trim().split("\\R");
        assertArrayEquals(new String[]{"1 | Иван | 0.00", "2 | Мария | 0.00"}, lines);
    }

    @Test
    void showPrintsAccount() {
        processor.execute("create Иван");
        processor.execute("create Мария");
        output();

        processor.execute("show 2");
        assertEquals("2 | Мария | 0.00", output().trim());
    }

    @Test
    void showWithNonNumericIdPrintsError() {
        processor.execute("show abc");
        assertTrue(output().startsWith("Ошибка"));
    }

    @Test
    void unknownCommandPrintsError() {
        assertTrue(processor.execute("foo"));
        assertEquals("Ошибка: неизвестная команда 'foo'. Введите help", output().trim());
    }

    @Test
    void emptyInputIsIgnored() {
        assertTrue(processor.execute("   "));
        assertEquals("", output());
    }

    @Test
    void exitReturnsFalse() {
        assertFalse(processor.execute("exit"));
    }
}
