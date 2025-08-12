package com.example.chatbot.init;

import com.example.chatbot.model.CoptimoPlan;
import com.example.chatbot.repository.CoptimoPlansRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class RagDataInitializer   implements CommandLineRunner {


    private final CoptimoPlansRepository coptimoPlansRepository;
    private final VectorStore vectorStore;

    @Value("classpath:/rag/faq-tunisian-business.md")
    private Resource faqResource;

    public RagDataInitializer(final CoptimoPlansRepository coptimoPlansRepository, final VectorStore vectorStore) {
        this.coptimoPlansRepository = coptimoPlansRepository;
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(final String... args)  {

        // 1. Define the Coptimo plans
        List<CoptimoPlan> plans = List.of(
                new CoptimoPlan("Accounting Management",
                        "Accounting management is an essential service offered on Coptimo, which consists of regularly recording all your company's financial transactions (purchases, sales, expenses, etc.) in accordance with Tunisian legislation. This allows you to prepare your financial statements and tax returns. You can order this service directly via the Coptimo platform."),
                new CoptimoPlan("Company Creation (SARL/SUARL)",
                        "Coptimo offers a complete support service for the creation of your company to simplify this process for you. Our experts handle administrative steps such as registration with the National Business Registry (RNE), obtaining a tax ID, and publication in the JORT. You can find more details and order this service directly in the 'Services' section of the platform."),
                new CoptimoPlan("VAT Declaration",
                        "The VAT declaration is a service offered by Coptimo to help you meet your tax obligations. We handle the preparation and submission of your monthly or quarterly VAT declarations, ensuring they comply with Tunisian legislation. You can order this service directly via the Coptimo platform."),
                new CoptimoPlan("Tax Declaration",
                        "This service helps you prepare and submit your corporate or income tax declarations, in compliance with Tunisian legislation."),
                new CoptimoPlan("Payroll Management",
                        "Coptimo offers complete payroll management for your employees, including salary calculation, management of social and tax declarations, and generation of pay slips."),
                new CoptimoPlan("Tax Consulting",
                        "Our experts support you in optimizing your taxation, answering your questions, and helping you comply with your legal obligations.")
        );

        this.coptimoPlansRepository.saveAll(plans);


        List<Document> dbDocuments = coptimoPlansRepository.findAll().stream()
                .map(plan -> new Document(
                        plan.getDescription(),
                        Map.of(
                                "id", plan.getId(),
                                "plan", plan.getName(),
                                "description", plan.getDescription()
                        )
                ))
                .toList();
        List<Document> documents = new ArrayList<>(dbDocuments);


        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true)
                .withIncludeCodeBlock(false)
                .withIncludeBlockquote(false)
                .build();
        MarkdownDocumentReader markdownReader = new MarkdownDocumentReader(faqResource,config);
        List<Document> mdDocuments = markdownReader.get();
        documents.addAll(mdDocuments);

        vectorStore.add(documents);


    }
}
