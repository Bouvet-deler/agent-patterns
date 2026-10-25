package com.agentpatterns.agent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

// Verktøy agenten fritt kan velge å kalle – den bestemmer selv om, når og hvordan
// den skal bruke dem, i motsetning til de faste stegene i arbeidsflytmønsteret.
public class FileTools {

    private static final Path FILE = Path.of("file.txt");

    @Tool(description = "Leser det nåværende innholdet i file.txt")
    public String readFile() {
        try {
            return Files.exists(FILE) ? Files.readString(FILE) : "";
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Tool(description = "Overskriver file.txt med det oppgitte innholdet")
    public String writeFile(@ToolParam(description = "Det fullstendige nye innholdet som skal skrives til file.txt") String content) {
        try {
            Files.writeString(FILE, content);
            return "file.txt er oppdatert";
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
