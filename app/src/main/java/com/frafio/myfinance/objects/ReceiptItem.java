package com.frafio.myfinance.objects;

public class ReceiptItem {

    // definizione campi
    public String name;
    public double price;

    // costruttore
    public ReceiptItem(String name, double price){
        this.name = name;
        this.price = price;
    }

    // costruttore vuoto utile per FireBase
    private ReceiptItem(){}

    // getter e setter
    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
}
