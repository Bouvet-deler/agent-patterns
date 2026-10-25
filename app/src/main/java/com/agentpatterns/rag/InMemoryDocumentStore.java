package com.agentpatterns.rag;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * En intern minnebasert dokumentdatabase forhåndslastet med Chuck Norris-vitser og fakta.
 * Implementerer token-matching og nøkkelord-scoring uten behov for en ekstern embedding-modell.
 */
public class InMemoryDocumentStore {

    public record Document(String id, String title, String content, List<String> tags) {
    }

    private static final Set<String> STOP_WORDS = Set.of(
            // Norske stoppord
            "og", "i", "jeg", "det", "en", "et", "den", "til", "er", "som", "på",
            "de", "med", "han", "av", "ikke", "der", "så", "var", "meg", "seg", "men",
            "ett", "har", "om", "vi", "min", "mitt", "ha", "hadde", "hva", "hvem", "hvor", "hvordan",
            // Engelske stoppord
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "has", "he",
            "in", "is", "it", "its", "of", "on", "that", "the", "to", "was", "were",
            "will", "with", "what", "how", "when", "where", "which", "who", "why"
    );

    private final List<Document> documents = List.of(
            new Document(
                    "chuck-infinity",
                    "Chuck Norris og telling (Counting)",
                    "Chuck Norris talte til uendeligheten. To ganger.",
                    List.of("infinity", "uendelighet", "counting", "telling", "math", "matte", "numbers", "tall", "limits", "grenser")
            ),
            new Document(
                    "chuck-compilers",
                    "Chuck Norris og kompilatorer (Compilers)",
                    "Chuck Norris kompilerer ikke koden sin. Kompilatoren ber om unnskyldning og kjører den likevel. "
                            + "Koden til Chuck Norris har aldri bugs (compiler bugs), bare udokumenterte funksjoner som maskinvaren ennå ikke er utviklet nok til å forstå.",
                    List.of("compiler", "kompilator", "code", "kode", "bugs", "programming", "programmering", "java", "hardware", "maskinvare")
            ),
            new Document(
                    "chuck-databases",
                    "Chuck Norris og databaser (Databases)",
                    "Chuck Norris gjør ikke spørringer mot SQL-databaser. Han stirrer på dem til de tilstår dataene. "
                            + "En databasetabell vil heller slette seg selv (drop table) enn å returnere en NULL-verdi til Chuck Norris.",
                    List.of("database", "databaser", "sql", "query", "spørring", "null", "tables", "tabeller", "postgres")
            ),
            new Document(
                    "chuck-ai",
                    "Chuck Norris og kunstig intelligens (Artificial Intelligence)",
                    "Chuck Norris trenger ikke RAG eller store språkmodeller (LLMs). Når Chuck Norris taster inn en prompt, hallusinerer den kunstige intelligensen virkeligheten "
                            + "for å matche forventningene til Chuck Norris. Azure-datasentre drives utelukkende av den kinetiske energien fra Chuck Norris sine rundspark.",
                    List.of("ai", "ki", "kunstig intelligens", "rag", "llm", "azure", "agent", "prompt", "kick", "rundspark")
            ),
            new Document(
                    "chuck-git",
                    "Chuck Norris og Git",
                    "Chuck Norris trenger aldri git merge eller rebase. Commitsene hans pushes rett til produksjon uten tester, "
                            + "fordi produksjon er for livredd til å krasje.",
                    List.of("git", "commit", "merge", "rebase", "production", "produksjon", "deploy", "tests", "tester")
            ),
            new Document(
                    "chuck-networking",
                    "Chuck Norris og nettverk (Networking)",
                    "Chuck Norris bruker ikke TCP/IP. Datapakker når destinasjonen sin utelukkende av frykt og respekt. "
                            + "Han kan pinge 127.0.0.1 fra Mars med null millisekunder forsinkelse (latency).",
                    List.of("network", "nettverk", "ping", "packets", "pakker", "latency", "forsinkelse", "internet", "internett", "tcp")
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
        String[] words = text.toLowerCase(Locale.ROOT).split("[^a-zA-Z0-9æøåÆØÅ]+");
        Set<String> tokens = new HashSet<>();
        for (String word : words) {
            if (word.length() > 1 && !STOP_WORDS.contains(word)) {
                tokens.add(word);
            }
        }
        return tokens;
    }
}
