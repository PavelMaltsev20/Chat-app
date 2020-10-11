package com.example.pavel.chatapp.AdaptersAndModulus.Items;

public class Message {

    private String sender;
    private String receiver;
    private String message;
    private boolean isSeen;
    private boolean isNotified;

    public Message() {
    }

    public Message(String sender, String receiver, String message, boolean isSeen, boolean isNotified) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isSeen = isSeen;
        this.isNotified = isNotified;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        this.isSeen = seen;
    }

    public boolean isNotified() {
        return isNotified;
    }

    public void setNotified(boolean notified) {
        isNotified = notified;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", message='" + message + '\'' +
                ", isSeen=" + isSeen +
                ", isNotified=" + isNotified +
                '}';
    }
}
