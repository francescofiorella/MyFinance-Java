package com.frafio.myfinance.objects;

public class Purchase {

    // definizione campi
    public String name, type, email;
    public double price;
    public int year, month, day, num;

    // costruttore nuovo utente
    public Purchase(String email, String name, String type, double price, int year, int month, int day, int num){
        this.email = email;
        this.name = name;
        this.type = type;
        this.price = price;
        this.year = year;
        this.month = month;
        this.day = day;
        this.num = num;
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

    public String getType() {
        return type;
    }

    public double getPrice() {
        return price;
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

    public int getNum() {
        return num;
    }
}
