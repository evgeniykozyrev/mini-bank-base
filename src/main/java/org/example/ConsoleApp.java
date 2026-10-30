package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;

public class ConsoleApp {

    private final BufferedReader in;
    private final PrintStream out;
    private final CommandProcessor processor;

    public ConsoleApp(AccountRepository repository, BufferedReader in, PrintStream out) {
        this.in = in;
        this.out = out;
        this.processor = new CommandProcessor(repository, out);
    }

    public void run() {
        try {
            while (true) {
                out.print("> ");
                out.flush();
                String line = in.readLine();
                if (line == null || !processor.execute(line)) {
                    return;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
