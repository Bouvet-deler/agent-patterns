package com.agentpatterns.baseline;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import com.agentpatterns.Pattern;

// Plain, deterministic file I/O with no LLM involved - the baseline to contrast
// against the workflow and agent patterns.
public class BaselinePattern implements Pattern {

    private static final Path FILE = Path.of("file.txt");

    @Override
    public void run(Scanner scanner) {
        System.out.println("Commands: 'read', 'write <text>', 'exit'");
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Bye!");
                return;
            } else if (input.equalsIgnoreCase("read")) {
                System.out.println(read());
            } else if (input.toLowerCase().startsWith("write ")) {
                write(input.substring("write ".length()));
                System.out.println("Written.");
            } else {
                System.out.println("Unknown command. Use 'read', 'write <text>', or 'exit'.");
            }
        }
    }

    private String read() {
        try {
            return Files.exists(FILE) ? Files.readString(FILE) : "(file.txt does not exist yet)";
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void write(String content) {
        try {
            Files.writeString(FILE, content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
