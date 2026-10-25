package com.agentpatterns.agent;

import java.util.Scanner;

import org.springframework.ai.chat.client.ChatClient;

import com.agentpatterns.Pattern;

// Agent: Språkmodellen bestemmer selv om/når/hvordan den skal kalle de tilgjengelige verktøyene
// (lese/skrive til file.txt, søke i kunnskapsbasen) – det er ingen fast sekvens av steg
// slik som i arbeidsflyten eller den klassiske RAG-implementasjonen.
public class AgentPattern implements Pattern {

    private static final String SYSTEM_PROMPT = """
            Du er en autonom assistent som alltid svarer på norsk.
            Du har tilgang til verktøy for å lese og oppdatere file.txt, samt søke i kunnskapsbasen etter Chuck Norris-vitser og fakta.
            Bruk verktøyene aktivt når det er nødvendig for å besvare brukerens henvendelse.
            """;

    private final ChatClient chatClient;
    private final FileTools fileTools;
    private final KnowledgeTools knowledgeTools;

    public AgentPattern(ChatClient chatClient, FileTools fileTools, KnowledgeTools knowledgeTools) {
        this.chatClient = chatClient;
        this.fileTools = fileTools;
        this.knowledgeTools = knowledgeTools;
    }

    @Override
    public void run(Scanner scanner) {
        System.out.println("Snakk fritt – agenten bestemmer selv når den skal lese/skrive til file.txt eller søke i kunnskapsbasen ('avslutt' eller 'exit' for å avslutte).");
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
            String response = chatClient.prompt()
                                        .system(SYSTEM_PROMPT)
                                        .user(input)
                                        .tools(fileTools, knowledgeTools)
                                        .call()
                                        .content();
            System.out.println(response);
        }
    }
}
