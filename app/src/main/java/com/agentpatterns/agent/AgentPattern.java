package com.agentpatterns.agent;

import java.util.Scanner;

import org.springframework.ai.chat.client.ChatClient;

import com.agentpatterns.Pattern;

// Agent: the LLM itself decides whether/when/how to call the read/write tools -
// there's no fixed sequence of steps like in the workflow pattern.
public class AgentPattern implements Pattern {

    private final ChatClient chatClient;
    private  final FileTools fileTools;

    public AgentPattern(ChatClient chatClient, FileTools fileTools) {
        this.chatClient = chatClient;
        this.fileTools = fileTools;
    }

    @Override
    public void run(Scanner scanner) {
        System.out.println("Chat freely - the agent decides when to read/write file.txt ('exit' to quit).");
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Bye!");
                return;
            }
            if (input.isEmpty()) {
                continue;
            }
            String response = chatClient
                    .prompt(input)
                    .tools(fileTools)
                    .call()
                    .content();
            System.out.println(response);
        }
    }
}
