package edu.cooper;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class for reading the configuration files. The default file is traffic.properties.
 * 
 * @author EliFriedman
 */
public class ConfigReader {

    Properties prop;
    int num_routes;

    double[] weights;
    int[] thresholds;
    ArrayList<Road[]> routeList;
    
    public ConfigReader(String propfile) {
        prop = new Properties();
        try {
            prop.load(new FileInputStream(propfile));
            this.initializeWeights();
            this.initializeNumRoutes();
        } catch (FileNotFoundException ex) {
            System.err.println("File not found!");
            Logger.getLogger(ConfigReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConfigReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setNumAgents(int num_agents) {
        prop.setProperty("num_agents", "" + num_agents);
    }

    public void setChangeFrac(double G) {
        prop.setProperty("G", "" + G);
    }

    public void setTRPFFrac(double p) {
        prop.setProperty("p", "" + p);
    }

    public void setNumPrevRounds(int T) {
        prop.setProperty("T", "" + T);
    }

    public void setWeights(int[] thresholds, double[] weights) {
        if (thresholds.length != weights.length) {
            Logger.getLogger(ConfigReader.class.getName())
                    .log(Level.SEVERE, "thresholds and weights need to be the same length as the number of routes.");
            return;
        }
        String s = "";
        int L = thresholds.length;
        for (int i = 0; i < L - 1; i++) {
            s += "" + thresholds[i] + ":" + weights[i] + ",";
        }
        s += "" + thresholds[L - 1] + ":" + weights[L - 1];
        prop.setProperty("w", "" + s);
    }

    /**
     * Returns a HashMap mapping road names to Road objects. A road name is
     * defined by the names of the nodes on either side of an edge. e.g. if node
     * A goes to node B, there would be a road called "AB". The road network is
     * defined in a file specified by the "road_network_filename" parameter in
     * traffic.properties.
     *
     * @return A HashMap which maps road names to a Road object.
     */
    public HashMap<String, Road> getRoadNetwork() {
        HashMap<String, Road> ht = new HashMap<>(); // each road contains the list of agents currently using that road
        String roadfile = prop.getProperty("road_network_filename");
        try (Scanner s = new Scanner(new File(roadfile))) {
            s.useDelimiter("[,\\n]");
            s.nextLine();
            while (s.hasNext()) {
                String c1 = s.next();
                String c2 = s.next();
                String roadname = c1 + c2;
                float freeflowtraveltime = s.nextFloat();
                float capacity = s.nextFloat();
                Road road = new Road(roadname, capacity, freeflowtraveltime);
                ht.put(roadname, road);
                ht.put(c2 + c1, road); // make sure it's symmetric
            }
        } catch (FileNotFoundException fe) {
            Logger.getLogger(ConfigReader.class.getName()).log(
                    Level.SEVERE,
                    "Could not find roadnet file: '{0}'", roadfile);
            System.exit(-1);
        } catch (InputMismatchException im) {
            Logger.getLogger(ConfigReader.class.getName()).log(
                    Level.SEVERE,
                    "Please check that file '{0}' is in the correct place and has the correct format.", roadfile);
            System.exit(-1);
        }
        return ht;
    }

    private void initializeNumRoutes() {
        int n = 1;
        String line;
        while ((line = prop.getProperty("route" + n)) != null) {
            n++;
        }
        num_routes = n;
    }
    /**
     *
     * @param agentHolder
     * @return
     */
    public ArrayList<Road[]> getRouteList(HashMap<String, Road> agentHolder) {
        final String DELIMITER = ",";
        ArrayList<Road[]> routelist = new ArrayList<>();
        int n = 1;
        String line;
        while ((line = prop.getProperty("route" + n)) != null) {
            String[] tokens = line.split(DELIMITER);
            Road[] route = new Road[tokens.length - 1];
            for (int i = 0; i < tokens.length - 1; i++) {
                String roadName = tokens[i] + tokens[i + 1];
                if (agentHolder.containsKey(roadName)) {
                    route[i] = agentHolder.get(roadName);
                } else {
                    Logger.getLogger(ConfigReader.class.getName()).log(
                            Level.SEVERE,
                            "The road network does not contain a road from {0} to {1}.",
                            new Object[]{tokens[i], tokens[i + 1]});
                    System.exit(-1);
                }
            }
            routelist.add(route);
            n++;
        }
        num_routes = routelist.size();
        Logger.getLogger(ConfigReader.class.getName()).log(
                Level.INFO, "Loaded: {0} routes",
                routelist.size());
        return routelist;
    }

    public int[] getSOList() {
        ArrayList<Integer> SOlist = new ArrayList<>();
        int n = 1;
        String line;
        while ((line = prop.getProperty("route" + n + ".opt")) != null) {
            SOlist.add(Integer.parseInt(line));
            n++;
        }
        int[] SOs = new int[SOlist.size()];
        for (int i = 0; i < SOlist.size(); i++) {
            SOs[i] = SOlist.get(i);
        }
        return SOs;
    }

    public double getChangeFrac() {
        double changers = Double.parseDouble(prop.getProperty("G"));
        if (changers < 0 || changers > 1) {
            Logger.getLogger(ConfigReader.class.getName()).log(
                    Level.SEVERE, "G should be between 0 and 1. G = {0}",
                    changers);
            System.exit(-1);
        }
        Logger.getLogger(ConfigReader.class.getName()).log(
                Level.INFO, "Loaded: G = {0}",
                changers);
        return changers;
    }

    public double getTRPFFrac() {
        double users = Double.parseDouble(prop.getProperty("p"));
        if (users < 0 || users > 1) {
            Logger.getLogger(ConfigReader.class.getName()).log(
                    Level.SEVERE, "p should be between 0 and 1. G = {0}",
                    users);
            System.exit(-1);
        }
        Logger.getLogger(ConfigReader.class.getName()).log(Level.INFO, "Loaded: p = {0}",
                users);
        return users;
    }

    public int getNumPrevRounds() {
        int rounds = Integer.parseInt(prop.getProperty("T"));
        Logger.getLogger(ConfigReader.class.getName()).log(Level.INFO, "Loaded: T = {0}",
                rounds);
        return rounds;
    }

    private void initializeWeights() {
        String wstr = prop.getProperty("w").replaceAll(" ", "").replaceAll("\t", "");
        String[] twlist = wstr.split(",");
        weights = new double[twlist.length];
        thresholds = new int[twlist.length];

        for (int i = 0; i < twlist.length; i++) {
            String[] tw = twlist[i].split(":");
            thresholds[i] = Integer.parseInt(tw[0]);
            weights[i] = Double.parseDouble(tw[1]);
        }
    }

    public int[] getThresholds() {
        return thresholds;
    }

    public double[] getWeights() {
        return weights;
    }

    /**
     * Initializes the agents. The number of agents is specified in the
     * "num_agents" property in the "traffic.properties" file.
     *
     * @return An ArrayList of agents.
     */
    public ArrayList<Agent> getAgents() {
        int num_agents = Integer.parseInt(prop.getProperty("num_agents", "200"));
        Logger.getLogger(ConfigReader.class.getName()).log(
                Level.INFO, "Loaded: {0} agents.", num_agents);
        ArrayList<Agent> agents = new ArrayList<>(num_agents);
        double fracChange = getChangeFrac();
        double fracTRPF = getTRPFFrac();
        for (int id = 0; id < num_agents; id++) {
            Agent a = new Agent(id, num_routes,thresholds, weights, fracChange, fracTRPF);
            agents.add(a);
        }
        return agents;
    }

    public int getNumIterations() {
        return Integer.parseInt(prop.getProperty("num_iterations", "1"));
    }
    private static void makeRouteShareMatrix(String matrixfilename, String vectorfilename, ArrayList<Road[]> routelist) {
        final String DELIMITER = ",";
        double[][] matrix = new double[routelist.size()][routelist.size()];
        double[] vector = new double[routelist.size()];
        try (BufferedWriter mfileWriter = new BufferedWriter(new FileWriter(matrixfilename));
                BufferedWriter vfileWriter = new BufferedWriter(new FileWriter(vectorfilename))) {
            String mat = "";
            String vec = "";
            for (int i = 0; i < routelist.size(); i++) {
                for (int j = 0; j < routelist.size(); j++) {
                    for (Road r1 : routelist.get(i)) {
                        if (i == j) {
                            matrix[i][i] += 1.0 / r1.driverdependence;
                            vector[i] += r1.freeflow_tt;
                        } else {
                            for (Road r2 : routelist.get(j)) {
                                if (r1.equals(r2)) {
                                    matrix[i][j] += 1.0 / r1.driverdependence;
                                }
                            }
                        }
                    }
                    mat += 0.02 * matrix[i][j];
                    if (j == routelist.size() - 1) {
                        mat += "\n";
                        vec += vector[i] + "\n";
                    } else {
                        mat += ",";
                    }
                }
            }
            System.out.println(mat);
            vec = vec.substring(0, vec.length() - 1);
            System.out.println(vec);
            mfileWriter.write(mat);
            vfileWriter.write(vec);
        } catch (Exception e) {
            System.err.println("ConfigReader.makeRouteShareMatrix: A problem occured while trying to create or write to file " + matrixfilename);
        }
    }
}
