package com.example.esarthi;

public class ChatMessage {
    public static final int TYPE_USER = 1;
    public static final int TYPE_AI = 2;

    private String message;
    private String time;
    private int type;

    public ChatMessage(String message, String time, int type) {
        this.message = message;
        this.time = time;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public int getType() {
        return type;
    }
}
