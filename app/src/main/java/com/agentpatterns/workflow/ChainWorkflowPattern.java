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
@SuppressWarnings("null")
public class ChainWorkflowPattern implements Pattern {

    private static final Path FILE = Path.of("file.txt");
    private static final String SYSTEM_PROMPT = "Du er en flink assistent som alltid svarer på norsk!";

    private final ChatClient chatClient;

    public ChainWorkflowPattern(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public void run(Scanner scanner) {
        System.out.println("Skriv en instruksjon for file.txt ('les'/'read' for å se den, 'avslutt'/'exit' for å avslutte).");
        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("q") || input.equalsIgnoreCase("avslutt")) {
                System.out.println("Ha det bra!");
                return;
            } else if (!input.isEmpty()) {
                System.out.println(callChainWorkflow(input));
            }
        }
    }

    /**
     * Kontrollflyt-metode som koordinerer arbeidsflyten:
     * 1. Ruting: Analyserer brukerens intensjon (chat, lese eller skrive).
     * 2. Forgrening: Kaller det tilhørende steget basert på ruten.
     */
    @Nonnull
    private String callChainWorkflow(String instruction) {
        Route route = routeIntent(instruction);
        if (route == null) {
            return "Noe gikk galt under ruting av forespørselen.";
        }

        if (route.chat()) {
            return handleChat(instruction);
        }
        if (route.read()) {
            return handleRead(instruction);
        }
        if (route.write()) {
            return handleWrite(instruction);
        }

        return "Noe gikk galt: ukjent rute.";
    }

    // Steg 1: Ruting (intensjonsklassifisering)
    private Route routeIntent(String instruction) {
        return chatClient.prompt()
                .system("""
                        Du er en assistent som ruter brukerhenvendelser.

                        Analyser brukerens intensjon og sett nøyaktig én av disse til true:
                        - chat: hvis brukeren vil prate løst
                        - read: hvis brukeren vil lese eller se innholdet i file.txt
                        - write: hvis brukeren vil endre, legge til eller slette i file.txt
                        """)
                .user(instruction)
                .call()
                .entity(Route.class);
    }

    // Steg 2: Enkel chat-rute
    private String handleChat(String instruction) {
        String response = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(instruction)
                .call()
                .content();

        return response != null ? response : "Ingen respons fra modellen.";
    }

    // Steg 3: Lese-rute (injiserer filinnhold i system-prompt)
    private String handleRead(String instruction) {
        String content = read();

        String response = chatClient.prompt()
                .system("""
                        %s

                        Nåværende innhold i file.txt:
                        \"\"\"
                        %s
                        \"\"\"

                        Svar på brukerens henvendelse basert på dette innholdet.
                        Hvis innholdet er tomt, informer brukeren om at filen er tom.
                        """.formatted(SYSTEM_PROMPT, content))
                .user(instruction)
                .call()
                .content();

        return response != null ? response : "Ingen respons fra modellen.";
    }

    // Steg 4: Skrive-rute som demonstrerer sekvensiell prompt-kjede (Prompt Chaining)
    private String handleWrite(String instruction) {
        String currentContent = read();

        // Kjedesteg 4a: Modellen analyserer ønsket endring og lager et utkast (Draft)
        Draft draft = chatClient.prompt()
                .system("""
                        %s

                        Nåværende innhold i file.txt:
                        \"\"\"
                        %s
                        \"\"\"

                        Oppgave:
                        Les innholdet og fyll 'content' kun med det brukeren ber om.
                        Hvis brukeren ber om å tømme eller fjerne innholdet, skal 'content' være en tom streng.
                        Hvis brukeren ber om å rette skrivefeil, skal du kun rette disse.
                        """.formatted(SYSTEM_PROMPT, currentContent))
                .user(instruction)
                .call()
                .entity(Draft.class);

        String draftedContent = draft != null && draft.content() != null ? draft.content() : "";

        // Kjedesteg 4b: Neste prompt tar utkastet, kvalitetssikrer det og formulerer svar til brukeren
        FileUpdate update = chatClient.prompt()
                .system("""
                        %s

                        Foreslått innhold fra forrige steg i kjeden:
                        \"\"\"
                        %s
                        \"\"\"

                        Oppgave:
                        1. Fyll ut 'content' med det endelige innholdet som skal lagres i filen.
                        2. Skriv et kort, naturlig svar i første person til brukeren i 'reply'.
                        """.formatted(SYSTEM_PROMPT, draftedContent))
                .user(instruction)
                .call()
                .entity(FileUpdate.class);

        // Kjedesteg 4c: Deterministisk kode lagrer til fil og returnerer responsen
        if (update != null && update.content() != null) {
            write(update.content());
            return update.reply() != null ? update.reply() : "Filen ble oppdatert.";
        }

        return "Noe gikk galt under oppdatering av filen.";
    }

    private record Route(@Nonnull boolean chat, @Nonnull boolean read, @Nonnull boolean write) {}

    private record Draft(String content) {}

    private record FileUpdate(String content, String reply) {}

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
