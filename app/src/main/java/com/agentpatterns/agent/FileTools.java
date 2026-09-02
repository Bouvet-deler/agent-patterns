package com.agentpatterns.agent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

// Tools the agent can freely choose to call - it decides whether, when and how
// to use them, unlike the fixed steps in the workflow pattern.
public class FileTools {

    private static final Path FILE = Path.of("file.txt");

    @Tool(description = "Read the current contents of file.txt")
    public String readFile() {
        try {
            return Files.exists(FILE) ? Files.readString(FILE) : "";
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Tool(description = "Overwrite file.txt with the given content")
    public String writeFile(@ToolParam(description = "The full new contents of file.txt") String content) {
        try {
            Files.writeString(FILE, content);
            return "file.txt updated";
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
