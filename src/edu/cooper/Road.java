package edu.cooper;

import java.util.Objects;

/**
 * A representation of a road. A road keeps track of the number of drivers using
 * it. The travel time along a road is a function of the number of drivers.
 * @author EliFriedman
 */
public class Road {
    String name;
    double driverdependence;
    double freeflow_tt; // road length
    int num_agents;

    /**
     * 
     * @param name The name of the road. It should be specified as part of 
     * a road network. See the README for more details.
     * @param freeflow_tt The cost of the road when no drivers are on it
     * @param driverdependence A weighting factor for calculating the cost as 
     * a function of the number of drivers.
     */
    public Road(String name, double freeflow_tt, double driverdependence) {
        this.name = name;
        this.driverdependence = driverdependence;
        this.freeflow_tt = freeflow_tt;
        this.num_agents = 0;
    }

    /**
     * Returns the travel time on this road as a function of number of drivers.
     * Currently the travel time is a linear function of number of drivrs.
     * traveltime(n) = freeflowtraveltime + driverdependence*n;
     * @return this.freeflowtraveltime + this.driverdependence*this.num_cars
     */
    public double cost() {
        if("start".equals(name) || "end".equals(name)) return 0;
        return Road.cost(this.freeflow_tt, this.driverdependence, this.num_agents);
    }

    public void addCars(int num) {
        this.num_agents += num;
    }
    public void resetNumCars() {
        this.num_agents = 0;
    }
    /**
     * Returns the number of cars currently on the road.
     * @return Returns the number of cars currently on the road.
     */
    public int numCars() {
        return this.num_agents;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Road)) return false;
        Road oRoad = (Road) other;
        return this.name.equals(oRoad.name) && this.driverdependence==oRoad.driverdependence && this.freeflow_tt==oRoad.freeflow_tt;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.name);
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.driverdependence) ^ (Double.doubleToLongBits(this.driverdependence) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.freeflow_tt) ^ (Double.doubleToLongBits(this.freeflow_tt) >>> 32));
        return hash;
    }

    public static double alpha = .15;
    public static double beta = 4;
    /**
     * Returns the travel time on this road as a function of number of drivers.
     * Currently the travel time is a linear function of number of drivrs.
     * traveltime(n) = freeflowtraveltime + driverdependence*n;
     * @param freeflowtraveltime
     * @param capacity
     * @param num_cars
     * @return freeflowtraveltime + driverdependence*num_cars
     */
    public static double cost(double freeflowtraveltime, double driverdependence, int num_cars) {
        return freeflowtraveltime + driverdependence*num_cars;
//        return freeflowtraveltime + 0.02*(num_cars/capacity);
//        return freeflowtraveltime * ( 1 + Road.alpha*Math.pow(num_cars/capacity,Road.beta) );
    }
}
