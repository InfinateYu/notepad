package com.example.notepad;

import java.util.List;

public class Note {
    private String title;
    private String time;
    private String content;
    private String imagePaths;
    private String tags;

    public Note(String title, String content, String time,  String imagePaths) {
        this.title = title;
        this.time = time;
        this.content = content;
        this.imagePaths = imagePaths;
    }
    public Note(String title, String content, String time, String tags, String imagePaths) {
        this.title = title;
        this.time = time;
        this.content = content;
        this.tags = tags;
        this.imagePaths = imagePaths;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public String getContent() {
        return content;
    }

    public String getImagePaths() {
        return imagePaths;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setImagePaths(String imagePaths) {this.imagePaths = imagePaths;}
    public String getTags() {return tags;}
    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getBody() { return content;    }
}