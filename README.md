# Spring AI Chatbot Coptimo

A powerful AI-driven chatbot built with Spring Boot, designed to assist users with Tunisian business FAQs and Coptimo plan recommendations. This project leverages RAG (Retrieval-Augmented Generation) and integrates with custom data sources for enhanced responses.

## Features

- **AI Chatbot**: Interacts with users, answers questions, and provides recommendations.
- **RAG Integration**: Uses retrieval-augmented generation for context-aware answers.
- **Coptimo Plan Support**: Recommends business plans based on user queries.
- **FAQ Knowledge Base**: Answers Tunisian business-related questions from a curated FAQ.
- **RESTful API**: Easily integrate with other applications via HTTP endpoints.



## Getting Started

### Prerequisites
- Java 21+
- Maven
- Docker (optional)

### Setup & Run

1. **Clone the repository**
   ```bash
   git clone <repo-url>
   cd spring-ai-chatbot-coptimo
   ```
2. **Build the project**
   ```bash
   ./mvnw clean install
   ```
3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```
   Or use Docker Compose:
   ```bash
   docker compose up
   ```

### Configuration
- Edit `src/main/resources/application.yml` for custom settings.
- Update FAQ or prompts in `src/main/resources/rag/faq-tunisian-business.md` and `src/main/resources/prompts/system-message.st`.


## Extending the Bot
- Add new FAQs to the markdown file.
- Implement new business logic in the `service` package.
- Add new endpoints in `controller/ChatController.java`.


## License

This project is licensed under the MIT License.

## Contact

For questions or support, open an issue or contact the maintainer.

