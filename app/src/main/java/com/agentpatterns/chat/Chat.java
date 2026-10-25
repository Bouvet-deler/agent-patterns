package com.agentpatterns.chat;

import java.util.Scanner;

import org.springframework.ai.chat.client.ChatClient;

import com.agentpatterns.Pattern;

/**
 * Direkte prompt / samtale:
 * En enkel samtaleutveksling med språkmodellen uten verktøy, arbeidsflyter eller RAG.
 * Fungerer som et referansepunkt for å sammenligne med arbeidsflyter og autonome agenter.
 */
public class Chat implements Pattern {

    private static final String SYSTEM_PROMPT = "Du er en hjelpsom assistent som alltid svarer på norsk.";

    private final ChatClient chatClient;

    public Chat(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public void run(Scanner scanner) {
        System.out.println("Snakk fritt med språkmodellen (ingen verktøy, arbeidsflyter eller RAG – skriv 'avslutt' eller 'exit' for å avslutte).");
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
                    .call()
                    .content();
            System.out.println(response);
        }
    }
}
