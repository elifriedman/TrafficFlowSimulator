package edu.cooper;

/**
 * Created by friedm3 on 8/6/15.
 */
public class Road {
    String name;
    float capacity;
    double freeflow_tt; // road length
    int num_agents;

    public Road(String name,float capacity, float freeflow_tt) {
        this.name = name;
        this.capacity = capacity;
        this.freeflow_tt = freeflow_tt;
        this.num_agents = 0;
    }

    public double cost() {
        if("start".equals(name) || "end".equals(name)) return 0;
        return Road.cost(this.freeflow_tt, this.capacity, this.num_agents);
    }

    public void addCars(int num) {
        this.num_agents += num;
    }
    public void resetNumCars() {
        this.num_agents = 0;
    }
    public int numCars() {
        return this.num_agents;
    }
    public double potentialFunction() {
        return this.potentialFunction(this.num_agents);
    }
    public double potentialFunction(int num_cars) {
        double sum = 0;
        for(int i=0;i<=num_cars; i++) {
            sum += Road.cost(this.freeflow_tt,this.capacity,i);
        }
        return sum;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Road)) return false;
        Road oRoad = (Road) other;
        if (this.name.equals(oRoad.name) && this.capacity==oRoad.capacity && this.freeflow_tt==oRoad.freeflow_tt) return true;
        return false;
    }

    public static double alpha = .15;
    public static double beta = 4;
    public static double cost(double freeflowtraveltime, double capacity, int num_cars) {
        return freeflowtraveltime + capacity*num_cars;
//        return freeflowtraveltime + 0.02*(num_cars/capacity);
//        return freeflowtraveltime * ( 1 + Road.alpha*Math.pow(num_cars/capacity,Road.beta) );
    }
}
