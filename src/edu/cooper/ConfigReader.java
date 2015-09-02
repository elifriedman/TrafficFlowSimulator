package edu.cooper;

import java.io.*;
import java.util.*;

/**
 * Created by friedm3 on 8/6/15.
 */
public class ConfigReader {
    public static HashMap<String,Road> initializeRoadNetwork(String filename) {
        HashMap<String,Road> ht = new HashMap<>(); // each road contains the list of agents currently using that road
        ht.put("start",new Road("start",0,0)); // a place to put agents before they start on their journey
        ht.put("end",new Road("end",0,0)); // a place to put agents after they finish their journey
        try {
            Scanner s = new Scanner(new File(filename));
            s.useDelimiter("[,\\n]");
            s.nextLine();
            while(s.hasNext()) {
                String c1 = s.next(); String c2 = s.next();
                String roadname = c1 + c2;
                float freeflowtraveltime = s.nextFloat();
                int capacity = s.nextInt();
                Road road = new Road(roadname,capacity,freeflowtraveltime);
                ht.put(roadname, road);
                ht.put(c2 + c1, road); // make sure it's symmetric
            }
            s.close();
        } catch (FileNotFoundException fe) {

        } catch (InputMismatchException im) {
            System.err.println("ConfigReader.initializeRoadNetwork: Please check that file '" + filename+ "' is in the correct place and has the correct format.");
            System.exit(-1);
        }
        return ht;
    }

    public static ArrayList<Road[]> initializeRouteList(String filename, HashMap<String,Road> agentHolder) {
        final String DELIMITER = ",";
        ArrayList<Road[]> routelist = new ArrayList<>();
        try (BufferedReader fileReader = new BufferedReader(new FileReader(filename))){
            fileReader.readLine(); // skip first line

            String line = "";
            while ((line = fileReader.readLine()) != null) {
                if(line.charAt(0)=='#') continue; // python comments!
                //Get all tokens available in line
                String[] tokens = line.split(DELIMITER);
                Road[] route = new Road[tokens.length+1];
                route[0] = agentHolder.get("start");
                route[route.length - 1] = agentHolder.get("end");
                for (int i = 0; i < tokens.length - 1; i++) {
                    String routeName = tokens[i] + tokens[i+1];
                    if(agentHolder.containsKey(routeName)) {
                        route[i+1] = agentHolder.get(routeName);
                    } else {
                        System.err.println("The road network does not contain "
                                + "a road from " + tokens[i] +
                                " to " + tokens[i+1] + ".");
                        System.exit(-1);
                    }
                }
                routelist.add(route);
            }
        } catch (Exception e) {
            System.err.println("ConfigReader.initializeRoute: Check that file '" + filename + "' is in the correct place and has the correct format");
            System.exit(-1);
        }
        return routelist;
    }

    public static ArrayList<Agent[]> initializeAgents(String filename, double timewindow) {
        Random rng = new Random();
        ArrayList<Agent[]> waveList = new ArrayList<>();

        //Delimiter used in CSV file
        final String DELIMITER = ",";
        try (BufferedReader fileReader = new BufferedReader(new FileReader(filename))){
            fileReader.readLine(); // skip first line

            String line = "";
            int id = 0;
            while ((line = fileReader.readLine()) != null) {
                if(line.charAt(0)=='#') continue; // python comments!
                //Get all tokens available in line
                String[] tokens = line.split(DELIMITER);
                int num_agents = Integer.parseInt(tokens[0]);
                String randcheck = tokens[1];
                double start_time;
                if(randcheck.equals("r")) {
                    start_time = rng.nextDouble()*timewindow;
                } else {
                    start_time = Double.parseDouble(randcheck);
                }

                Agent[] wave = new Agent[num_agents];
                for(int i=0;i<num_agents;i++) {
                    Agent a = new Agent(id);
                    wave[i] = a;
                    id++;
                }
                waveList.add(wave);
            }
        } catch (Exception e) {
            System.err.println("ConfigReader.initializeRoute: Check that file '" + filename + "' is in the correct place and has the correct format");
            System.exit(-1);
        }
        return waveList;
    }
}
