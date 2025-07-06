package com.example.zayn.model;

public class ChatPreview {
    public String chatId;
    public String otherUserId;
    public String otherUsername;
    public String otherProfilePictureUrl;
    public String lastMessageText;
    public long lastMessageTimestamp;

    public ChatPreview(String chatId, String otherUserId, String otherUsername, String otherProfilePictureUrl, String lastMessageText, long lastMessageTimestamp) {
        this.chatId = chatId;
        this.otherUserId = otherUserId;
        this.otherUsername = otherUsername;
        this.otherProfilePictureUrl = otherProfilePictureUrl;
        this.lastMessageText = lastMessageText;
        this.lastMessageTimestamp = lastMessageTimestamp;
    }
} 