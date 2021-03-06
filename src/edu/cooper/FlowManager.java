package edu.cooper;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the traffic simulation.
 *
 * @author EliFriedman
 */
public class FlowManager {

    /**
     * Parses the configuration .properties file to get the parameters.
     */
    ConfigReader conf;
    /**
     * Maps road names to Road objects
     */
    HashMap<String, Road> roads;
    /**
     * List of usable routes
     */
    ArrayList<Road[]> routes;
    /**
     * List of optimal # of drivers for each route
     */
    int[] SOlist;
    /**
     * List of agents.
     */
    ArrayList<Agent> agents;
    /**
     * Number of previous rounds to use for calculating the TRPF
     */
    int T;
    /**
     * The Traffic Route Preference Function object
     */
    TRPF trpf;

    PrettyOutput po;
    String[] seriesnames;
    int simNumber;

    String basepath;

    /**
     * Initialize FlowManager with the parameters set in the .properties file
     * specified by paramfile
     *
     * @param basepath A path to a folder which should contain a
     * traffic.properties file from which to load the paraemters, a roadnet.csv
     * file, from which to load the roadnet, and an output/ folder.
     */
    public FlowManager(String basepath) {
        this.basepath = basepath;
        conf = new ConfigReader(basepath + "/traffic.properties");
        po = new PrettyOutput();
        seriesnames = new String[]{"cars", "costs", "trpfs"};
        initParams();
        simNumber = 0;
    }

    /**
     * If you want programmatic access to the parameters, you can use this
     * constructor. You can pass in a ConfigReader object and use the setX()
     * method of the ConfigReader object to update the parameters. After you've
     * updated the parameters, call updateParams() and you're ready to simulate.
     *
     * @param conf
     */
    public FlowManager(ConfigReader conf) {
        this.conf = conf;
        initParams();
    }

    /**
     * Initializes all the model and algorithm parameters.
     *
     * @param paramfile A .parameter file with format key=value
     */
    private void initParams() {
        roads = conf.getRoadNetwork(this.basepath + "/roadnet.csv");
        routes = conf.getRouteList(roads);
        SOlist = conf.getSOList();
        agents = conf.getAgents();
        T = conf.getNumPrevRounds();
        trpf = new TRPF(routes.size(), T); // TRPF(# routes, # prev rounds)

        // initialize pretty output
        for (int i = 0; i < routes.size(); i++) {
            for (String seriesname : seriesnames) {
                po.createNewSeries(seriesname + i);
            }
        }
        printParameters();
    }

    /**
     * A useful function if you want to programmatically update the parameters.
     * Call it before starting your simulations, so that the simulation uses the
     * new parameters.
     */
    public void updateParams() {
        initParams();
    }

    /**
     * Resets the roads in between rounds so they have no drivers on them.
     */
    public void clearRoads() {
        for (Road r : roads.values()) {
            r.resetNumCars();
        }
    }

    /**
     * Runs one round of simulation. The simulation proceeds in 4 steps.
     * <ol>
     * <li> Each agent chooses a route. Some of the agents (specified by G in
     * the .properties file), choose a new route. The other 1-G agents use the
     * same route as before. Some of the agents (specified by p in the
     * .properties file), make use of the TRPF to decide which route. The other
     * (1-p)G choose randomly. The number of agents on each route is
     * collected.</li>
     * <li> The agents are added to their chosen routes and travel to their
     * destinations.</li>
     * <li> The agents that use the TRPF report the congestion level on their
     * route using the difference between the number of cars currently on the
     * route and the number of cars that there should be at the System
     * Optimum.</li>
     * <li> Metrics are printed out.</li>
     * </ol>
     *
     * @return Returns the cost of each route.
     */
    public double[] simulate() {
        // We're starting a new round, so we'll clear the roads.
        this.clearRoads();

        // contains the number of agents using each route
        int[] routenums = new int[routes.size()];

        double[] currTRPF = trpf.getTRPF();
        // Agents select which route
        for (Agent a : agents) {
            int route = a.chooseRoute(currTRPF);
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

        // get route costs
        double[] costs = getCosts();

        // Get congestion reports and update the TRPF.
        trpf.newReport();
        for (Agent a : agents) {
            int route = a.getPrevRouteChoice();
            if (a.usesTRPF()) {
                int congestion = routenums[route] - SOlist[route];
                // Agent compares congestion to thresholds
                double report = a.congestionReport(congestion);
                trpf.addReport(route, report);
            }
            double c = costs[route];
            a.receiveRouteCost(c);
        }
        printData(simNumber, routenums, costs, trpf.getTRPF());
        simNumber++;
        return costs;
    }

    /**
     * Returns the costs of each route. It finds the cost of a route by looping
     * through the roads on that route and adding up the cost of each road.
     *
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
     * Returns the average travel time of all agents.
     *
     * @param costs The costs for each route.
     * @param routenums The number of drivers on each route.
     * @return Returns the average travel time of an agent.
     */
    public double getAvgCost(double[] costs, int[] routenums) {
        double sum = 0;
        int total = agents.size();
        for (int i = 0; i < costs.length; i++) {
            sum += costs[i] * routenums[i];
        }
        return sum / total;
    }

    /**
     * Used by simulation() to print out the useful information.
     *
     * @param simNum
     * @param routenums
     * @param costs
     * @param trpf
     */
    public void printData(int simNum, int[] routenums, double[] costs, double[] trpf) {
        for (int i = 0; i < costs.length; i++) {
            po.addToSeries(seriesnames[0] + i, "" + simNum, "" + routenums[i]);
            po.addToSeries(seriesnames[1] + i, "" + simNum, String.format("%.3f", costs[i]));
            po.addToSeries(seriesnames[2] + i, "" + simNum, String.format("%.3f", trpf[i]));
        }
    }

    public void printParameters() {
        String paramfile = this.basepath + "/js/params.js";
        po.createNewSeries("params");
        po.addToSeries("params", "'p'", String.valueOf(conf.getTRPFFrac()));
        po.addToSeries("params", "'G'", String.valueOf(conf.getChangeFrac()));
        po.addToSeries("params", "'T'", String.valueOf(conf.getNumPrevRounds()));
        double[] weights = conf.getWeights();
        String weightstr = "[";
        for (double weight : weights) {
            weightstr += String.format("%f,", weight);
        }
        weightstr += "]";
        int[] thrshs = conf.getThresholds();
        String thrshstr = "[";
        for (int thrsh : thrshs) {
            thrshstr += String.format("%d,", thrsh);
        }
        thrshstr += "]";
        po.addToSeries("params", "'thresholds'", thrshstr);
        po.addToSeries("params", "'weights'", weightstr);
        po.endSeries("params");
        po.writeVariable(paramfile, "params", "params");
    }

    public void endSimulation() {
        for (int i = 0; i < routes.size(); i++) {
            for (String seriesname : seriesnames) {
                po.endSeries(seriesname + i);
            }
        }
        String[][] combine = new String[seriesnames.length][routes.size()];
        for (int i = 0; i < combine.length; i++) {
            for (int j = 0; j < routes.size(); j++) {
                combine[i][j] = seriesnames[i] + j;
            }
        }
        for (int i = 0; i < seriesnames.length; i++) {
            po.combineSeries(seriesnames[i], combine[i]);
            String fname = this.basepath+"/js/vis_" + seriesnames[i] + ".js";
            po.writeVariable(fname, seriesnames[i], seriesnames[i]);
        }
    }

    /**
     * The number of simulation iterations.
     *
     * @return The number of simulation rounds.
     */
    public int getNumIterations() {
        return conf.getNumIterations();
    }

    public static boolean checkDirectoryStructure(String base) {
        boolean ret =  (new File(base + "/traffic.properties")).exists()
                && (new File(base + "/roadnet.csv")).exists();
        File jsdir = new File(base + "/js");
        if (!jsdir.exists()) {
            if(!jsdir.mkdir()) {
                Logger.getLogger(FlowManager.class.getName())
                    .log(Level.SEVERE, "could not create directory %s/js!",base);
                System.exit(-1);
            }
        }
        return ret;
    }

    public static void main(String[] args) {
        FlowManager fm;
        if (args.length < 1
                || !(new File(args[0])).exists()) {
            Logger.getLogger(FlowManager.class.getName())
                    .log(Level.SEVERE, "usage: make run dir=simulation_directory");
            System.exit(-1);
        } else if(!checkDirectoryStructure(args[0])) {
            Logger.getLogger(FlowManager.class.getName())
                    .log(Level.SEVERE, "your directory does not have the correct format.");
            System.exit(-1);
        }
        fm = new FlowManager(args[0]);
        int num_iterations = fm.getNumIterations();
        for (int i = 0; i < num_iterations; i++) {
            try {
                fm.simulate();
            } catch(Exception ex) {
                Logger.getLogger(FlowManager.class.getName()).log(Level.SEVERE,"Problem with simulation #{0}",i+1);
                System.exit(-1);
            }
        }
        fm.endSimulation();
    }
}
