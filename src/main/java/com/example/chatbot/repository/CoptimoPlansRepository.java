package com.example.chatbot.repository;

import com.example.chatbot.model.CoptimoPlan;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoptimoPlansRepository extends JpaRepository<CoptimoPlan, UUID> {}
