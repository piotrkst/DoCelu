package com.engineer.docelu.Models;

public class Direction {
    private String returnVariant;
    private String direction;
    private String lineName;

    public Direction(String returnVariant, String direction, String lineName) {
        this.returnVariant = returnVariant;
        this.direction = direction;
        this.lineName = lineName;
    }

    public String getReturnVariant() { return returnVariant; }
    public String getDirection() { return direction; }
    public String getLineName() { return lineName; }
}
