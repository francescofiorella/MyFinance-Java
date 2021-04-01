package com.frafio.myfinance.objects;

public class User {

    // definizione campi
    public String fullName, email, image;

    // costruttore nuovo utente
    public User(String fullName, String email, String image){
        this.fullName = fullName;
        this.email = email;
        this.image = image;
    }

    // costruttore vuoto utile per FireBase
    private User(){}

    // getter e setter
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
