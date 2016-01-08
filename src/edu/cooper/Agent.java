package edu.cooper;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by friedm3 on 8/6/15.
 */
public class Agent {

    private int routechoice;
    final int ID;
    int[] thresholds;            // Number of drivers above the SO to report weight[i]
    double[] weights;            // Weights to be used in the TRPF
    double G;                    // Fraction of agents who will change routes from one round to the next.
    boolean useTRPF;
    int numroutes;
    Random rng;

    public Agent(int ID, int num_routes,
            int[] thresholds,
            double[] weights,
            double fracChange,
            double fracTRPF) {
        this.ID = ID;
        this.thresholds = thresholds;
        this.weights = weights;
        this.G = fracChange;
        this.numroutes = num_routes;
        this.rng = new Random();
        this.routechoice = rng.nextInt(numroutes); // Initialize with a random choice
        this.useTRPF = false;
        if (rng.nextDouble() < fracTRPF) {
            useTRPF = true;
        }
    }

    /**
     * Choose randomly from one of the routes in routelist
     *
     * @return An int from 0 to number_routes representing the chosen route.
     */
    public int chooseRoute() {
        routechoice = rng.nextInt(numroutes);
        return routechoice;
    }

    public int chooseRoute(double[] trpf) {
        // Check to see if we're changing routes.
        if (rng.nextDouble() <= G) {
            //Check to see if we make use of the TRPF
            if (useTRPF) {
                // We do use the TRPF. Let's find the maximum route
                double max = trpf[0];
                for (int i = 0; i < trpf.length; i++) {
                    if (trpf[i] > max) {
                        max = trpf[i];
                    }
                }
                ArrayList<Integer> max_index = new ArrayList<>();
                for (int i = 0; i < trpf.length; i++) {
                    if (trpf[i] == max) {
                        max_index.add(i);
                    }
                }
                // If more than one route are equivalent, choose randomly
                int r = rng.nextInt(max_index.size());
                routechoice = max_index.get(r);
                return routechoice;
            } else {
                routechoice = rng.nextInt(numroutes);
                return routechoice;
            }
        } else { // if we're not changing routes, return our last route.
            return routechoice;
        }
    }

    /**
     * Returns the previously made routechoice
     *
     * @return The previously made routechoice.
     */
    public int getPrevRouteChoice() {
        return routechoice;
    }

    public boolean usesTRPF() {
        return useTRPF;
    }

    public double congestionReport(int numCarsOverSO) {
        if (!useTRPF) {
            Logger.getLogger(Agent.class.getName()).log(Level.WARNING,
                    "This agent does not use the TRPF! Agent ID: {0}", ID);
            return 0;
        }

        // Find the maximum threshold that's less than numCarsOverSO
        double weight = 0;
        double max = 0;
        for (int i = 0; i < thresholds.length; i++) {
            // TODO if multiple thresholds are the same, choose randomly 
            // between them.
            if (thresholds[i] <= numCarsOverSO && thresholds[i] > max) {
                max = thresholds[i];
                weight = weights[i];
            }
        }
        return weight;
    }

    @Override
    public String toString() {
        return "" + this.ID;
    }

}
