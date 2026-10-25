package com.agentpatterns.rag;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;

import com.agentpatterns.Pattern;
import com.agentpatterns.rag.InMemoryDocumentStore.Document;

/**
 * Klassisk RAG (Retrieval-Augmented Generation):
 * Applikasjonskoden henter deterministisk relevante dokumenter fra en intern
 * minnedatabase og injiserer dem som forankret kontekst i prompten før modellen kalles.
 * Modellen velger eller kaller ikke verktøy selv; Java-koden styrer hele dataflyten.
 */
public class RagPattern implements Pattern {

    private final ChatClient chatClient;
    private final InMemoryDocumentStore documentStore;

    public RagPattern(ChatClient chatClient, InMemoryDocumentStore documentStore) {
        this.chatClient = chatClient;
        this.documentStore = documentStore;
    }

    @Override
    @SuppressWarnings("null")
    public void run(Scanner scanner) {
        System.out.println("Spør om Chuck Norris-vitser eller fakta ('avslutt' eller 'exit' for å avslutte).");
        System.out.println("Kontekst hentes deterministisk fra den interne Chuck Norris-kunnskapsbasen.");

        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("q") || input.equalsIgnoreCase("avslutt")) {
                System.out.println("Ha det bra!");
                return;
            }
            if (input.isEmpty()) {
                continue;
            }

            // 1. Hent (Retrieve): Applikasjonskoden spør den interne dokumentbasen
            List<Document> matches = documentStore.search(input, 3);

            String context = matches.isEmpty()
                    ? "Ingen relevante vitser funnet."
                    : matches.stream()
                            .map(doc -> "Tittel: " + doc.title() + "\nVits/Fakta: " + doc.content())
                            .collect(Collectors.joining("\n---\n"));

            // 2. Berik (Augment): Koden injiserer den hentede konteksten direkte i prompten
            String augmentedPrompt = """
                    Du er en vittig assistent som deler Chuck Norris-vitser og historier. Du svarer alltid på norsk!
                    Svar nøyaktig på brukerens forespørsel ved å KUN bruke den oppgitte konteksten nedenfor.
                    Hvis det ikke finnes noen relevant vits i konteksten, skal du humoristisk si at Chuck Norris ikke har avslørt den hemmeligheten for denne databasen ennå.

                    Kontekst:
                    ---
                    %s
                    ---

                    Brukerspørsmål: %s
                    """.formatted(context, input);

            // 3. Generer (Generate): LLM produserer det forankrede svaret
            String response = chatClient.prompt(augmentedPrompt).call().content();
            if (response != null) {
                System.out.println(response);
            }
        }
    }
}
