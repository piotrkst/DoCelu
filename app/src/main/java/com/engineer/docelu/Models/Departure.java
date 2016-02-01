package com.engineer.docelu.Models;

import java.io.Serializable;

public class Departure implements Serializable {
    private Boolean realTime;
    private String line;
    private Integer minutes;
    private Boolean onStopPoint;
    private String departure;

    public Departure(Boolean realTime, String line, Integer minutes, String departure, Boolean onStopPoint) {
        this.realTime = realTime;
        this.line = line;
        this.minutes = minutes;
        this.departure = departure;
        this.onStopPoint = onStopPoint;
    }

    public Boolean getRealTime() { return realTime; }
    public String getLine() { return line; }
    public Integer getMinutes() { return minutes; }
    public Boolean getOnStopPoint() { return onStopPoint; }
    public String getDeparture() { return departure; }
}
