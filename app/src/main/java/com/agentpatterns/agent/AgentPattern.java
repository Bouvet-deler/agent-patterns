package com.agentpatterns.agent;

import java.util.Scanner;

import org.springframework.ai.chat.client.ChatClient;

import com.agentpatterns.Pattern;

// Agent: the LLM itself decides whether/when/how to call the available tools
// (reading/writing file.txt, querying knowledge base) - there's no fixed
// sequence of steps like in the workflow or standalone RAG patterns.
public class AgentPattern implements Pattern {

    private final ChatClient chatClient;
    private final FileTools fileTools;
    private final KnowledgeTools knowledgeTools;

    public AgentPattern(ChatClient chatClient, FileTools fileTools, KnowledgeTools knowledgeTools) {
        this.chatClient = chatClient;
        this.fileTools = fileTools;
        this.knowledgeTools = knowledgeTools;
    }

    public AgentPattern(ChatClient chatClient, FileTools fileTools) {
        this(chatClient, fileTools, null);
    }

    @Override
    public void run(Scanner scanner) {
        System.out.println("Chat freely - the agent decides when to read/write file.txt or query the knowledge base ('exit' to quit).");
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
            var promptSpec = chatClient.prompt(input);
            if (knowledgeTools != null) {
                promptSpec = promptSpec.tools(fileTools, knowledgeTools);
            } else {
                promptSpec = promptSpec.tools(fileTools);
            }
            String response = promptSpec.call().content();
            System.out.println(response);
        }
    }
}
