package com.example.zayn.model;

public class ChatSummary {
    public String otherUserId;
    public String lastMessage;
    public long timestamp;

    public ChatSummary() {}
    public ChatSummary(String otherUserId, String lastMessage, long timestamp) {
        this.otherUserId = otherUserId;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }
} 