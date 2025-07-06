package com.example.zayn.model;

public class Message {
    public String messageId;
    public String senderId;
    public String text;
    public long timestamp;

    public Message() {}

    public Message(String messageId, String senderId, String text, long timestamp) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
    }

    public Message(String messageId, String text, long timestamp) {
        this.messageId = messageId;
        this.text = text;
        this.timestamp = timestamp;
    }

    // Constructor to handle Firebase's ServerValue.TIMESTAMP (Long or Double)
    public Message(String messageId, String senderId, String text, Object timestampObj) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.text = text;
        this.timestamp = parseTimestamp(timestampObj);
    }

    private long parseTimestamp(Object timestampObj) {
        if (timestampObj instanceof Long) {
            return (Long) timestampObj;
        } else if (timestampObj instanceof Double) {
            return ((Double) timestampObj).longValue();
        } else if (timestampObj != null) {
            try {
                return Long.parseLong(timestampObj.toString());
            } catch (Exception e) {
                return 0L;
            }
        }
        return 0L;
    }
}