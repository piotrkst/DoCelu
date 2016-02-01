package com.engineer.docelu.Models;

public class StopPoint {
    private String symbol;
    private String name;

    public StopPoint(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public String getSymbol() { return symbol; }
    public String getName() { return name; }
}
