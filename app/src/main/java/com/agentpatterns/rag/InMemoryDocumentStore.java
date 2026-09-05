package com.agentpatterns.rag;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * An in-memory document database preloaded with Chuck Norris jokes and facts.
 * Implements token-matching and keyword-scoring search without requiring an external embedding model.
 */
public class InMemoryDocumentStore {

    public record Document(String id, String title, String content, List<String> tags) {
    }

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "has", "he",
            "in", "is", "it", "its", "of", "on", "that", "the", "to", "was", "were",
            "will", "with", "what", "how", "when", "where", "which", "who", "why"
    );

    private final List<Document> documents = List.of(
            new Document(
                    "chuck-infinity",
                    "Chuck Norris and Counting",
                    "Chuck Norris counted to infinity. Twice.",
                    List.of("infinity", "counting", "math", "numbers", "limits")
            ),
            new Document(
                    "chuck-compilers",
                    "Chuck Norris and Compilers",
                    "Chuck Norris does not compile his code. The compiler apologizes and runs it anyway. "
                            + "Chuck Norris's code never has bugs, only undocumented features that hardware is not yet evolved enough to comprehend.",
                    List.of("compiler", "code", "bugs", "programming", "java", "hardware")
            ),
            new Document(
                    "chuck-databases",
                    "Chuck Norris and Databases",
                    "Chuck Norris doesn't query SQL databases. He stares at them until they confess the data. "
                            + "A database table will drop itself before returning a NULL value to Chuck Norris.",
                    List.of("database", "sql", "query", "null", "tables", "postgres")
            ),
            new Document(
                    "chuck-ai",
                    "Chuck Norris and Artificial Intelligence",
                    "Chuck Norris does not need RAG or LLMs. When Chuck Norris inputs a prompt, the AI hallucinates reality "
                            + "to match Chuck Norris's expectations. Azure datacenters run entirely on the kinetic energy of Chuck Norris roundhouse kicks.",
                    List.of("ai", "rag", "llm", "azure", "agent", "prompt", "kick")
            ),
            new Document(
                    "chuck-git",
                    "Chuck Norris and Git",
                    "Chuck Norris never needs git merge or rebase. His commits simply push directly to production without tests, "
                            + "because production is too terrified to crash.",
                    List.of("git", "commit", "merge", "rebase", "production", "deploy", "tests")
            ),
            new Document(
                    "chuck-networking",
                    "Chuck Norris and Networking",
                    "Chuck Norris doesn't use TCP/IP. Packets reach their destination purely out of fear and respect. "
                            + "He can ping 127.0.0.1 from Mars with zero millisecond latency.",
                    List.of("network", "ping", "packets", "latency", "internet", "tcp")
            )
    );

    /**
     * Search documents using keyword and token matching score.
     *
     * @param query      The natural language search query.
     * @param maxResults Maximum number of documents to return.
     * @return Matching documents sorted by relevance score.
     */
    public List<Document> search(String query, int maxResults) {
        if (query == null || query.isBlank()) {
            return documents.stream().limit(maxResults).toList();
        }

        Set<String> queryTokens = extractTokens(query);
        if (queryTokens.isEmpty()) {
            return documents.stream().limit(maxResults).toList();
        }

        record ScoredDocument(Document document, int score) {
        }

        return documents.stream()
                .map(doc -> new ScoredDocument(doc, calculateScore(doc, queryTokens)))
                .filter(sd -> sd.score() > 0)
                .sorted((a, b) -> Integer.compare(b.score(), a.score()))
                .limit(maxResults)
                .map(sd -> sd.document())
                .toList();
    }

    public List<Document> getAllDocuments() {
        return documents;
    }

    private int calculateScore(Document doc, Set<String> queryTokens) {
        int score = 0;
        String lowerTitle = doc.title().toLowerCase(Locale.ROOT);
        String lowerContent = doc.content().toLowerCase(Locale.ROOT);

        for (String token : queryTokens) {
            // Higher weight for matches in tags and title
            for (String tag : doc.tags()) {
                if (tag.equalsIgnoreCase(token)) {
                    score += 5;
                }
            }
            if (lowerTitle.contains(token)) {
                score += 4;
            }
            if (lowerContent.contains(token)) {
                score += 1;
            }
        }
        return score;
    }

    private Set<String> extractTokens(String text) {
        String[] words = text.toLowerCase(Locale.ROOT).split("[^a-zA-Z0-9]+");
        Set<String> tokens = new HashSet<>();
        for (String word : words) {
            if (word.length() > 1 && !STOP_WORDS.contains(word)) {
                tokens.add(word);
            }
        }
        return tokens;
    }
}
