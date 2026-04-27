package com.hotelnova.model.entity;

public class Guest {
    private int id;
    private String documentId;
    private String fullName;
    private String email;
    private String phone;
    private boolean active;

    public Guest() {}
    public Guest(int id, String documentId, String fullName, String email, String phone, boolean active) {
        this.id = id; this.documentId = documentId; this.fullName = fullName;
        this.email = email; this.phone = phone; this.active = active;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return String.format("Guest{id=%d, doc='%s', name='%s', active=%b}",
                id, documentId, fullName, active);
    }
}
