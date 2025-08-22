package com.example.chatbot.service;


import com.example.chatbot.dto.ChatResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class AdvancedChatService {

    private static final Logger logger = LoggerFactory.getLogger(AdvancedChatService.class);
    private static final double SIMILARITY_THRESHOLD = 0.75;
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public AdvancedChatService(ChatClient.Builder builder,
                       JdbcChatMemoryRepository jdbcChatMemoryRepository,
                       final VectorStore vectorStore,
                       @Value("classpath:/prompts/system-message.st") Resource systemMessageResource) {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(10)
                .build();

        this.chatClient = builder
                .defaultSystem(systemMessageResource)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();

        this.vectorStore = vectorStore;


    }

    public ChatResponse chat(String conversationId, String userQuery) {


        final String finalConversationId;
        if (conversationId == null || conversationId.isBlank()) {
            finalConversationId = UUID.randomUUID().toString();
        } else {
            finalConversationId = conversationId;
        }
            DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                    .vectorStore(vectorStore)
                    .similarityThreshold(SIMILARITY_THRESHOLD)
                    .topK(1)
                    // The filterExpression ensures that only documents associated with the current conversationId
                    // are considered during the semantic cache lookup. This maintains context isolation between
                    // different user sessions and prevents returning cached answers from unrelated conversations.
                    .filterExpression(()-> new FilterExpressionBuilder()
                            .eq("conversationId", finalConversationId)
                            .build())
                    .build();
            List<Document> documents = retriever.retrieve(new Query(userQuery));





        if (!documents.isEmpty()) {
            Document mostSimilarDoc = documents.getFirst();
            String cachedAnswer = (String) mostSimilarDoc.getMetadata().get("answer");
            logger.info("Semantic cache HIT. Returning cached answer for question: '{}'", userQuery);
            return new ChatResponse(cachedAnswer, conversationId);
        }


        var responseContent = getResponseFromLlm(userQuery, finalConversationId);

        saveInVectorStore(userQuery, responseContent, finalConversationId);
        return new ChatResponse(responseContent, finalConversationId);
    }



    private String getResponseFromLlm(final String userQuery, final String finalConversationId) {

        QueryTransformer queryTransformer =
                RewriteQueryTransformer.builder().chatClientBuilder(this.chatClient.mutate()).build();

        Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.7)
                        .topK(3)
                        .vectorStore(vectorStore)
                        .build())
                .queryTransformers(queryTransformer)
                .build();


        String responseContent = chatClient.prompt()
                .user(userQuery)
                .advisors(retrievalAugmentationAdvisor)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, finalConversationId))
                .call()
                .content();
        if(responseContent == null || responseContent.isBlank()) {
            responseContent = "Sorry, I don't know how to answer that.";
        }
        return responseContent;
    }

    private void saveInVectorStore(final String userQuery, final String responseContent, final String finalConversationId) {
        Document qaDocument = new Document(userQuery, Map.of("answer", responseContent,"conversationId", finalConversationId));
        vectorStore.add(List.of(qaDocument));
    }
}
