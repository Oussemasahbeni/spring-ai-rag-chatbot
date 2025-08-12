package com.example.chatbot.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "coptimo_plans")
public class CoptimoPlan {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    public CoptimoPlan() {

    }

    public CoptimoPlan(final String name, final String description) {
        this.name = name;
        this.description = description;
    }



    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String plan) {
        this.name = plan;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
