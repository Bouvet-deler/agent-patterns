package com.agentpatterns.rag;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.agentpatterns.rag.InMemoryDocumentStore.Document;

class InMemoryDocumentStoreTest {

    private InMemoryDocumentStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryDocumentStore();
    }

    @Test
    void searchWithKeywordsReturnsRelevantDocuments() {
        List<Document> results = store.search("compiler bugs", 2);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.getFirst().title().contains("Compilers")
                || results.getFirst().content().contains("compiler"));
    }

    @Test
    void searchWithEmptyQueryReturnsDefaultDocuments() {
        List<Document> results = store.search("", 2);
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void searchWithNoMatchReturnsEmptyList() {
        List<Document> results = store.search("xyznonexistentword12345", 3);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}
