package edu.cooper;

import java.util.ArrayList;
import java.util.HashMap;

public class FlowManager {
    HashMap<String, Road> roads;
    ArrayList<Road[]> routes;
    ArrayList<Agent[]> flows;
    public FlowManager() {
        roads = ConfigReader.initializeRoadNetwork("config/roadnet.csv");
        routes = ConfigReader.initializeRouteList("config/routelist.csv", roads);
        flows = ConfigReader.initializeAgents("config/agentlist.csv", 30);
    }
    public double[] doFlow(int flownum) {
        Agent[] flow = flows.get(flownum);
        int[] routeusers = new int[routes.size()];

        // Agents select which route
        for (int a = 0; a < flow.length; a++) {
            int route = flow[a].chooseRoute(routes);
            routeusers[route]++;
        }

        // "Simulate" the agents travelling along each route
        for (int r = 0; r < routes.size(); r++) {
            for (Road road : routes.get(r)) {
                road.addCars(routeusers[r]);
            }
        }

        // Recover the costs
        double totalcost = 0;
        double[] routecosts = new double[routeusers.length];
        for (int r = 0; r < routes.size(); r++) {
            double routecost = 0;
            String rt = "";
            for (Road road : routes.get(r)) {
                routecost += road.cost();
                rt += road.name + ",";
            }
            routecosts[r] = routecost;
            totalcost += routecost * routeusers[r];
        }
        return routecosts;
    }

    public static void main(String[] args) {
        FlowManager fm = new FlowManager();
        fm.doFlow(0);
    }
}
