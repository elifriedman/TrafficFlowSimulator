package edu.cooper;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by friedm3 on 8/6/15.
 */
public class Agent {
    private Road[] route;
    final int ID;
    int[] thresholds;            // Number of drivers above the SO to report weight[i]
    double[] weights;            // Weights to be used in the TRPF
    
    public Agent(int ID, int[] thresholds, double[] weights)  {
        this.route = null;
        this.ID = ID;
        this.thresholds = thresholds;
        this.weights = weights;
    }

    /**
     * Choose from one of the routes in routelist
     * @param routelist a list of possible routes from which to choose
     */
    public int chooseRoute(ArrayList<Road[]> routelist) {
        // TODO create a choice function
        Random rnd = new Random();
        return rnd.nextInt(routelist.size());
    }
    public void travelRoute(Road[] route) {
        this.route = route;
    }

    @Override
    public String toString() {
        return "" + this.ID;
    }

}
