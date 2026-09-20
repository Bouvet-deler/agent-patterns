package com.agentpatterns.chat;

import java.util.Scanner;

import org.springframework.ai.chat.client.ChatClient;

import com.agentpatterns.Pattern;

/**
 * Direct Prompting / Chat Pattern:
 * A simple conversational exchange with the LLM without tools, workflows, or RAG.
 * Serves as the foundational baseline to contrast with agentic workflows and autonomous agents.
 */
public class Chat implements Pattern {

    private final ChatClient chatClient;

    public Chat(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public void run(Scanner scanner) {
        System.out.println("Chat freely with the LLM (no tools, workflows, or RAG - 'exit' to quit).");
        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("q")) {
                System.out.println("Bye!");
                return;
            }
            if (input.isEmpty()) {
                continue;
            }

            String response = chatClient.prompt(input).call().content();
            System.out.println(response);
        }
    }
}
