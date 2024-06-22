package com.example.notepad;

public class ListItem {
    private int iconResId;
    private String text;
    private String signature;
    private boolean isSelectable;
    private boolean isSelected;
    private boolean isVisible;
    private boolean isClickable;

    public ListItem(int iconResId, String text, String signature, boolean isSelectable, boolean isSelected) {
        this.iconResId = iconResId;
        this.text = text;
        this.signature = signature;
        this.isSelectable = isSelectable;
        this.isSelected = isSelected;
    }

    public ListItem(String text, String signature, boolean isSelectable, boolean isSelected) {
        this.text = text;
        this.signature = signature;
        this.isSelectable = isSelectable;
        this.isSelected = isSelected;
    }

    public ListItem(String text, boolean isVisible, boolean isSelected) {
        this.text = text;
        this.isVisible = isVisible;
        this.isSelectable = true;
        this.isSelected = isSelected;
    }
    public int getIconResId() {
        return iconResId;
    }

    public String getText() {
        return text;
    }

    public String getSignature() {
        return signature;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public boolean isClickable() {
        return isClickable;
    }

    public void setClickable(boolean clickable) {
        isClickable = clickable;
    }

    public boolean isSelectable() {
        return isSelectable;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setText(String text) {
        this.text = text;
    }
    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }
}