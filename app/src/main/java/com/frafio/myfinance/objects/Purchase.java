package com.frafio.myfinance.objects;

public class Purchase {

    // definizione campi
    public String name, email;
    public double price;
    public int year, month, day, type;

    // costruttore
    public Purchase(String email, String name, double price, int year, int month, int day, int type){
        this.email = email;
        this.name = name;
        this.price = price;
        this.year = year;
        this.month = month;
        this.day = day;
        this.type = type;
    }

    // costruttore vuoto utile per FireBase
    private Purchase(){}

    // getter e setter
    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getType() {
        return type;
    }
}
