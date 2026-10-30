package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleAppTest {

    @TempDir
    Path dir;

    private String run(Path file, String input) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        new ConsoleApp(new TxtAccountRepository(file), new BufferedReader(new StringReader(input)), out).run();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void showsPromptAndDoesNotTouchFileOnStart() {
        Path file = dir.resolve("accounts.txt");
        String output = run(file, "exit\n");

        assertTrue(output.startsWith("> "));
        assertFalse(Files.exists(file));
    }

    @Test
    void stopsOnExitAndIgnoresFollowingCommands() {
        Path file = dir.resolve("accounts.txt");
        run(file, "exit\ncreate Иван\n");

        assertFalse(Files.exists(file));
    }

    @Test
    void fullSession() {
        Path file = dir.resolve("accounts.txt");
        String output = run(file, "list\ncreate Иван\ncreate Мария\nlist\nshow 1\nfoo\nexit\n");

        assertTrue(output.contains("Счетов пока нет"));
        assertTrue(output.contains("2 | Мария | 0.00"));
        assertTrue(output.contains("Ошибка: неизвестная команда 'foo'. Введите help"));
        assertEquals(7, output.split("> ", -1).length - 1, "приглашение перед каждой командой");
    }

    @Test
    void stopsWhenInputEnds() {
        assertDoesNotThrow(() -> run(dir.resolve("accounts.txt"), "list\n"));
    }
}
