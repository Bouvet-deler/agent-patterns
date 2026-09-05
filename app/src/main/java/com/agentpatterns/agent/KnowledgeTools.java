package com.agentpatterns.agent;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import com.agentpatterns.rag.InMemoryDocumentStore;
import com.agentpatterns.rag.InMemoryDocumentStore.Document;

/**
 * A Spring AI tool that enables autonomous agents to query the in-memory Chuck Norris facts database.
 */
public class KnowledgeTools {

    private final InMemoryDocumentStore documentStore;

    public KnowledgeTools(InMemoryDocumentStore documentStore) {
        this.documentStore = documentStore;
    }

    @Tool(description = "Search the knowledge base for verified Chuck Norris jokes, facts, and programming lore")
    public String searchKnowledgeBase(
            @ToolParam(description = "Keywords or topic to search for Chuck Norris jokes (e.g. database, compiler, infinity, AI, git)") String query) {
        List<Document> results = documentStore.search(query, 3);
        if (results.isEmpty()) {
            return "No matching Chuck Norris jokes found for: " + query;
        }

        return results.stream()
                .map(doc -> "[" + doc.title() + "]\n" + doc.content())
                .collect(Collectors.joining("\n\n"));
    }
}
