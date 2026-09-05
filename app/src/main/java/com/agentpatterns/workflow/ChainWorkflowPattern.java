package com.agentpatterns.workflow;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import org.springframework.ai.chat.client.ChatClient;

import com.agentpatterns.Pattern;

import jakarta.annotation.Nonnull;

// Workflow pattern combining routing (intent classification) and prompt
// chaining (fixed multi-step pipeline). The code hardcodes the sequence; the
// LLM only fills in content or classification decisions at each step.
public class ChainWorkflowPattern implements Pattern {

    private static final Path FILE = Path.of("file.txt");

    private final ChatClient chatClient;

    public ChainWorkflowPattern(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public void run(Scanner scanner) {
        System.out.println("Type an instruction for file.txt ('read' to view it, 'exit' to quit).");
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Bye!");
                return;
            } else if (!input.isEmpty()) {
                System.out.println(callChainWorkflow(input));
            }
        }
    }

    @Nonnull
    private String callChainWorkflow(String instruction) {
        // Step 1: route the user intent
        final String routerPrompt = """
            Instruction: %s
            Analyse the user intent. 
            
            If the user only wants to chit chat
            then chat is true, else false.
            
            If the user wants to read only
            then read is true, else false.

            If the user wants to edit the file
            then write is true, else false.
            """.formatted(instruction);
        final Route route = chatClient
            .prompt(routerPrompt == null ? "skriv noe tull": routerPrompt)
            .call()
            .entity(Route.class);

        // Step 2: chat route
        if (route != null && route.chat) {
            final String chatPrompt = """
                    Bare svar, ingeting annet
                    Content: %s
                    """;
            final String response = chatClient
                    .prompt(chatPrompt == null ? "skriv noe tull": chatPrompt)
                    .call()
                    .content();
            return response;
        } 

        // Step 3: read route
        if (route != null && route.read) {
            String content = read();
            final String readPrompt = """
                    Read back to the user what the content is.
                    Content: %s
                    """.formatted(content);
            final String response = chatClient
                    .prompt(readPrompt == null ? "skriv noe tull": readPrompt)
                    .call()
                    .content();
            return response;
        }

        // Step 4: write route (prompt chain: draft -> refine)
        if (route != null && route.write) {
            String content = read();
            final String draftPrompt = """
                Current contents of file.txt:
                ---
                %s
                ---
                Instruction: %s
                Produce the FULL new contents of file.txt. Respond with only the file
                contents, no commentary or code fences. If the instruction asks to clear
                or remove all text, respond with a completely empty response.
                """.formatted(content, instruction);
            final String draft = chatClient
                .prompt(draftPrompt == null ? "Skriv noe tull" : draftPrompt)
                .call()
                .content();

            final String updatePrompt = """
                Proofread and lightly polish the following draft file contents (fix
                typos, improve clarity), keeping the meaning and format intact. The
                text may legitimately be empty - if so, keep it empty.
                ---
                %s
                ---
                Also write a short, natural, first-person reply to the user
                acknowledging what you just did, matching their tone. The user's
                original instruction was: "%s"
                """.formatted(draft, instruction);
            FileUpdate update = chatClient
                .prompt(updatePrompt == null ? "skriv noe tull" : updatePrompt)
                .call()
                .entity(FileUpdate.class);
            if (update != null) {
                write(update.content());
                return update.reply();
            }
        }        
        return "Noe gikk galt";
    }

    private record Route(@Nonnull boolean chat, @Nonnull boolean read, @Nonnull boolean write){}

    private record FileUpdate(String content, String reply) {
    }

    private String read() {
        try {
            return Files.exists(FILE) ? Files.readString(FILE) : "";
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
