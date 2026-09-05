package com.agentpatterns.rag;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;

import com.agentpatterns.Pattern;
import com.agentpatterns.rag.InMemoryDocumentStore.Document;

/**
 * Classic RAG (Retrieval-Augmented Generation):
 * The application code deterministically retrieves relevant documents from an in-memory
 * database and injects them as grounded context into the prompt before calling the LLM.
 * The model does not select or invoke tools; Java code controls the entire retrieval flow.
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
        System.out.println("Ask for Chuck Norris jokes or facts ('exit' to quit).");
        System.out.println("Context is deterministically retrieved from the in-memory Chuck Norris knowledge store.");

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

            // 1. Retrieve: Application code queries the in-memory document store
            List<Document> matches = documentStore.search(input, 3);

            String context = matches.isEmpty()
                    ? "No specific jokes found."
                    : matches.stream()
                            .map(doc -> "Title: " + doc.title() + "\nJoke/Fact: " + doc.content())
                            .collect(Collectors.joining("\n---\n"));

            // 2. Augment: Code injects retrieved context directly into the prompt
            String augmentedPrompt = """
                    You are a witty assistant sharing Chuck Norris jokes and lore.
                    Answer the user's request accurately using ONLY the provided context below.
                    If no relevant joke is found in the context, humorously state that Chuck Norris has not revealed that secret to this database yet.

                    Context:
                    ---
                    %s
                    ---

                    User Question: %s
                    """.formatted(context, input);

            // 3. Generate: LLM produces the grounded response
            String response = chatClient.prompt(augmentedPrompt).call().content();
            if (response != null) {
                System.out.println(response);
            }
        }
    }
}
