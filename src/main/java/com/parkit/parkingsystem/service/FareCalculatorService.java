package com.parkit.parkingsystem.service;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.time.Duration;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){

        double hours = calculateHours(ticket);
        if(hours<=0.5) ticket.setPrice(0);
        else {
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    ticket.setPrice(hours * Fare.CAR_RATE_PER_HOUR);
                    break;
                }
                case BIKE: {
                    ticket.setPrice(hours * Fare.BIKE_RATE_PER_HOUR);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unkown Parking Type");
            }
        }
    }
    public void calculateFareWithRegularCustomerDiscount(Ticket ticket){
        double price = ticket.getPrice()*0.95;
        ticket.setPrice(price);
    }

    public double calculateHours (Ticket ticket) {
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().isBefore(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        //Using Duration Between two LocalDateTime objects converts to minutes divided by 60 to have the
        //correct hours.
        Duration duration = Duration.between(ticket.getInTime(), ticket.getOutTime());
        long minutes = duration.toMinutes();
        return ((double) minutes/60);
    }
}