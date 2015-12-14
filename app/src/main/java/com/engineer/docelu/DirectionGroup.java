package com.engineer.docelu;

public class DirectionGroup {
    private String directionGrouped;
    private String symbol;

    public DirectionGroup(String directionGrouped, String symbol){
        this.directionGrouped = directionGrouped;
        this.symbol = symbol;
    }

    public String getDirectionGrouped(){ return directionGrouped; }
    public String getSymbol(){ return symbol; }
}
