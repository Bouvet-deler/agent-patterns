package com.agentpatterns.agent;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import com.agentpatterns.rag.InMemoryDocumentStore;
import com.agentpatterns.rag.InMemoryDocumentStore.Document;

/**
 * Et Spring AI-verktøy som lar autonome agenter søke i den interne Chuck Norris-kunnskapsbasen.
 */
public class KnowledgeTools {

    private final InMemoryDocumentStore documentStore;

    public KnowledgeTools(InMemoryDocumentStore documentStore) {
        this.documentStore = documentStore;
    }

    @Tool(description = "Søk i kunnskapsbasen etter bekreftede Chuck Norris-vitser, fakta og programmeringsvisdom")
    public String searchKnowledgeBase(
            @ToolParam(description = "Nøkkelord eller tema for å søke etter Chuck Norris-vitser (f.eks. database, kompilator, uendelighet, AI, git)") String query) {
        List<Document> results = documentStore.search(query, 3);
        if (results.isEmpty()) {
            return "Fant ingen matchende Chuck Norris-vitser for: " + query;
        }

        return results.stream()
                .map(doc -> "[" + doc.title() + "]\n" + doc.content())
                .collect(Collectors.joining("\n\n"));
    }
}
