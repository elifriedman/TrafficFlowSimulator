package edu.cooper;

import java.util.ArrayList;
import java.util.HashMap;

public class FlowManager {
    HashMap<String, Road> roads; // Maps road names to Road objects
    ArrayList<Road[]> routes;    // List of usable routes
    int[] SOlist;                // List of optimal # of drivers for each route
    ArrayList<Agent> agents;     // List of agents.
    double G;                    // Fraction of agents who will change routes from one round to the next.
    double p;                    // Fraction of agents using the TRPF
    int T;                       // Number of previous rounds to use for calculating the TRPF
    
    public FlowManager(String paramfile) {
        this.initParams(paramfile);
    }
    private void initParams(String paramfile) {
        ConfigReader conf = new ConfigReader(paramfile);
        roads = conf.getRoadNetwork();
        routes = conf.getRouteList(roads);
        SOlist = conf.getSOList();
        agents = conf.getAgents();
        G = conf.getChangeFrac();
        p = conf.getTRPFFrac();
        T = conf.getNumRounds();
    }
    public void clearRoads() {
        for (Road r : roads.values()) {
            r.resetNumCars();
        }
    }
    
    /**
     * Returns the number of users
     * @param routecosts An array of length routes.size() containing the 
     * current cost of each route.
     * @return int[] an array of length 
     */
    public int[] simulate(double[] routecosts) {
        // contains the number of agents using each route
        int[] routenums = new int[routes.size()];

        // Agents select which route
        for (Agent a : agents) {
            int route = a.chooseRoute(routes);
            routenums[route]++;
        }

        // Simulate the agents travelling along each route
        // We need to loop through each route, adding the right number of 
        //  agents to each road on that route
        for (int r = 0; r < routes.size(); r++) {
            for (Road road : routes.get(r)) {
                road.addCars(routenums[r]);
            }
        }
        
        return routenums;
    }
    
    /**
     * Returns the costs of each route. It finds the cost of a route by looping
     * through the roads on that route and adding up the cost of each road.
     * @return The costs of each route. The kth element contains the cost of
     * route k.
     */
    public double[] getCosts() {
        // Contains the cost for each route
        double[] routecosts = new double[routes.size()];
        
        // Recover the costs by looping through each route and adding the 
        //  cost of each road on the route
        for (int r = 0; r < routes.size(); r++) {
            double routecost = 0;
            for (Road road : routes.get(r)) {
                routecost += road.cost();
            }
            routecosts[r] = routecost;
        }
        return routecosts;
    }
    
    
    /**
     * Initializes each agent to a randomly chosen route and returns the cost
     * of each route.
     * @return The costs of each route. The kth element contains the cost of
     * route k.
     */
    public double[] initFlow() {
        this.clearRoads();
        
        // contains the number of agents using each route
        int[] routenums = new int[routes.size()];

        // Agents select which route
        for (Agent a : agents) {
            int route = a.chooseRoute(routes);
            routenums[route]++;
        }

        // Simulate the agents travelling along each route
        // We need to loop through each route, adding the right number of 
        //  agents to each road on that route
        for (int r = 0; r < routes.size(); r++) {
            for (Road road : routes.get(r)) {
                road.addCars(routenums[r]);
            }
        }

        return getCosts();
    }

    public static void main(String[] args) {
        FlowManager fm = new FlowManager("config/traffic.properties");
    }
}
