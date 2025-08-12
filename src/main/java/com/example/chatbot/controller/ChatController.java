package com.example.chatbot.controller;

import com.example.chatbot.dto.ChatResponse;
import com.example.chatbot.service.ChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {



    private final ChatService chatService;

    public ChatController(final ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/chat")
    public ChatResponse chat(@RequestParam String query,
                             @RequestParam(required = false) String conversationId) {

       return chatService.chat(conversationId, query);
    }
}
