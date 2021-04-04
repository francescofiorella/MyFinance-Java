package com.frafio.myfinance.objects;

public class Purchase {

    // definizione campi
    public String name, type;
    public double price;
    public int year, month, day;

    // costruttore nuovo utente
    public Purchase(String name, String type, double price, int year, int month, int day){
        this.name = name;
        this.type = type;
        this.price = price;
        this.year = year;
        this.month = month;
        this.day = day;
    }

    // costruttore vuoto utile per FireBase
    private Purchase(){}

    // getter e setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }
}
