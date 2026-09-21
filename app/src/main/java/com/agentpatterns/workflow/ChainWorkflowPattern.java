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

        final String userQuery = "Query: %s".formatted(instruction);

        final String prompt =  "Du er en flink assistent som alltid svarer på norsk!" + "\n" + userQuery;

        // Step 1: route the user intent
        final String routerPrompt = """
            %s

            Analyser brukerintensjonen. 
            
            Hvis brukeren kun vil chit chatte
            sett chat til true, ellers false.
            
            Hvis brukeren vil lese filen
            sett read til true, ellers false.

            Hvis brukeren vil editere filen
            sett write til true, ellers false.
            """.formatted(prompt);

        final Route route = chatClient
            .prompt(routerPrompt == null ? "skriv noe tull": routerPrompt)
            .call()
            .entity(Route.class);

        // Step 2: chat route
        if (route != null && route.chat) {
            final String response = chatClient
                    .prompt(userQuery == null ? "skriv noe tull": prompt)
                    .call()
                    .content();
            return response;
        } 

        // Step 3: read route
        if (route != null && route.read) {
            String content = read();
            final String readPrompt = """
                    %s        

                    Under Innhold er det som står skrevet i file.txt.
                    Hvis Innhold er tomt, er fila tom.
                    Innhold: %s
                    """.formatted(prompt, content);
            
            final String response = chatClient
                    .prompt(readPrompt == null ? "skriv noe tull": readPrompt)
                    .call()
                    .content();
            return response;
        }

        // Step 4: write route (prompt chain: draft -> refine)
        if (route != null && route.write) {

            String content = read();
            
            final String readPrompt = """
                %s

                Du skal lese det som står i innhold og fylle content kun med det brukeren etterspør.
                Ber brukeren om å fjerne så skal Innhold være tom string.
                Ber brukeren om å rette skrivefeil skal du kun gjøre det. 

                Innhold: %s
                """.formatted(prompt, content);

            final Draft draft = chatClient
                .prompt(readPrompt == null ? "skriv noe tull" : readPrompt)
                .call()
                .entity(Draft.class); 
            
            final String updatePrompt = """
                %s

                Din oppgave er 
                1. Å fylle ut "content" med det som står under Innhold.
                2. Skriv et kort, naturlig svar i første person til brukeren i "reply"

                ---

                Innhold: 
                %s
                """.formatted(prompt, draft == null ? "": draft.content());
            
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

    private record Draft(String content) {};
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
