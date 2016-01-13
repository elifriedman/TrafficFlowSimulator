package edu.cooper;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An agent can choose which route to travel along, either randomly or by making
 * use of the Traffic Route Preference Function.
 * @author EliFriedman
 */
public class Agent {

    int routechoice;
    double[] routeCosts;
    int[] routesTravelled;
    final int ID;
    int[] thresholds;            // Number of drivers above the SO to report weight[i]
    double[] weights;            // Weights to be used in the TRPF
    double G;                    // Fraction of agents who will change routes from one round to the next.
    boolean useTRPF;
    int numroutes;
    Random rng;

    /**
     * 
     * @param ID A unique ID for this agent. Currently unused.
     * @param num_routes The total number of available routes in this simulation.
     * @param thresholds A list containing a threshold for each route. When the
     * number of drivers on the current route is greater than the System Optimum
     * by this threshold, then the agent will (probably) report congestion.
     * I say probably because some agents do not report congestion based on the
     * fracTRPF parameter ('p' in the .properties file).
     * @param weights A list containing a weight for each route. When the
     * number of drivers on the current route is greater than the System Optimum
     * by the associated threshold, then the agent will (probably) report congestion.
     * I say probably because some agents do not report congestion based on the
     * parameter fracTRPF parameter ('p' in the .properties file).
     * @param fracChange Each time the agent needs to choose a route, it first
     * chooses a random number. If the number is less than fracChange, then the 
     * agent will choose a new route. Othrwise, it will stay on the same route.
     * @param fracTRPF During initialization, the agent chooses a random number.
     * If the number is less than fracTRPF, then the agent will report congestion
     * and use the TRPF when making route choices. Otherwise it will not report
     * congestion and will choose a route randomly.
     */
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
        this.routeCosts = new double[num_routes];
        this.routesTravelled = new int[num_routes];
        this.rng = new Random();
        this.routechoice = rng.nextInt(numroutes); // Initialize with a random choice
        this.useTRPF = false;
        if (rng.nextDouble() < fracTRPF) {
            useTRPF = true;
        }
    }

    /**
     * Chooses one of the lowest cost routes, or, with probability 0.05, chooses a random route.
     * Choose randomly from among the routes that this agent knows to have the minimum average cost.
     *
     * @return An int from 0 to number_routes representing the chosen route.
     */
    public int chooseRoute() {
        double explore = rng.nextDouble();
        if(explore<0.05) {
            routechoice = rng.nextInt(numroutes);
            this.routesTravelled[routechoice]++;
            return routechoice;
        }
        double min = routeCosts[0];
        for (int i = 0; i < routeCosts.length; i++) {
            if (routeCosts[i] < min) {
                min = routeCosts[i];
            }
        }
        ArrayList<Integer> min_index = new ArrayList<>();
        for (int i = 0; i < routeCosts.length; i++) {
            if (Math.abs(routeCosts[i] - min) <= 0.1) {
                min_index.add(i);
            }
        }

        int r = rng.nextInt(min_index.size());
        routechoice = min_index.get(r);
        this.routesTravelled[routechoice]++;

        return routechoice;
    }

    /**
     * The agent chooses a new route based on the following process.
     * <ul>
     * <li> First, the agent decides whether it will choose a new route or just
     * stick with the old one by choosing a random number between 0 and 1 and 
     * seeing whether it is less than fracChange ('G' in the .properties file).
     * </li>
     * <li> If it does not change routes, it just returns the previously selected
     * route.</li>
     * <li> If it does change routes, and it's an agent who uses the TRPF, then
     * it will select the route with highest scoring TRPF (breaking ties randomly).
     * </li>
     * <li> If it does change routes, but does not use the TRPF, then it will
     * choose a new route randomly.</li>
     * </ul>
     * @param trpf A list containing the Traffic Route Preference Function for 
     * each of the possible routes.
     * @return The chosen route (expressed as an integer between 0 and numroutes).
     */
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
                this.routesTravelled[routechoice]++;
                return routechoice;
            } else {
                return chooseRoute();
            }
        } else { // if we're not changing routes, return our last route.
            this.routesTravelled[routechoice]++;
            return routechoice;
        }
    }

    /**
     * After travelling a route, the agent should receive the cost of travelling on that route.
     * The agent updates its knowledge about that route's cost, which is the average cost of all the times it's
     * travelled on that route.
     * @param cost The cost of the route the agent just travelled on.
     */
    public void receiveRouteCost(double cost) {
        int N = this.routesTravelled[routechoice];
        double Anm1 = this.routeCosts[routechoice];
        this.routeCosts[routechoice] = 1.0/N * ((N-1)*Anm1 + cost); // calculate new average from old average
    }

    /**
     * Returns the previously made routechoice
     *
     * @return The previously made routechoice.
     */
    public int getPrevRouteChoice() {
        return routechoice;
    }

    /**
     * Returns whether this agent is among those who use the TRPF. When iniitialized, 
     * the agent decides whether to use the TRPF by choosing a random number and
     * seeing whether it is less than fracTRPF ('p' in the .properties file).
     * @return 
     */
    public boolean usesTRPF() {
        return useTRPF;
    }

    /**
     * Returns the congestion level of the current route. When the
     * number of drivers on the current route is greater than the System Optimum
     * by the largest threshold, then the agent will report congestion.
     * For example, if the thresholds specified in the .properties file are 
     * <br/>w   =   10:3, 20:5
     * <br/>then the agent will report "3" if there are 10 more drivers on this
     * route than the System Optimum. If there are at least 20 more drivers on
     * this route than the System Optimum, then the agent will report 5
     * <br/>* Note that the agent will only report congestion if it uses the 
     * TRPF.
     * @param numCarsOverSO The difference between the number of drivers on this
     * route and the number of drivers there should be on this route at the
     * System Optimum.
     * @return Returns a weight expressing the level of congestion on the road.
     */
    public double congestionReport(int numCarsOverSO) {
        if (!useTRPF) {
            Logger.getLogger(Agent.class.getName())
                    .log(Level.WARNING, "{0} does not use the TRPF!", this.toString());
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
        return "Agent " + this.ID;
    }

}
