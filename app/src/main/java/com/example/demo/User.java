package com.example.demo;

public class User {
    String email, fullName, type;

    public User(String email, String fullName, String type) {
        this.email = email;
        this.fullName = fullName;
        this.type = type;
    }

    public User() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
