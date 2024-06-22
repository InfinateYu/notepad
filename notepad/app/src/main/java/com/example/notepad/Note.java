package com.example.notepad;


import java.util.List;

public class Note {
    private String title;
    private String body;
    private String time;
    private List<String> tags;

    // Constructors, getters, and setters

    public Note(String title, String body, String time) {
        this.title = title;
        this.body = body;
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getTime() {
        return time;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
