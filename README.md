# TrafficFlowSimulator
A flow based simulation of traffic over a road network for the purpose of testing distributed concensus algorithms that try to minimize average travel time.

## Overview
The program simulates traffic flowing through a user-specified road network with linear latency functions. It outputs the results of the simulation to an HTML file (output.html) for easy viewing.

The simulation reads in a road network and the costs of each road. It also reads a list of the possible routes that the agents can use, and the optimal number of drivers that should be on each route. 

The simulation proceeds in the following steps:

1. Each agent chooses a route.
  * Only a randomly chosen fraction of the agents (`G` in the properties file) will choose a new route. The rest will use the same route they used last time.
  * A fraction of the agents (`p` in the propeties file) will use the Traffic Route Preference Function (TRPF) to choose their route. The rest will choose a route uniformly at random.
2. The cost of each route is calculated. It's just the sum of the costs of each road on that route.
3. The agents who use the TRPF report the levels of congestion on their routes and the TRPF is updated.
4. The output is printed to a file.
5. The simulation goes to step 1 and repeats until `num_iterations` have been reached.

### Traffic Route Preference Function
A randomly chosen fraction of the drivers (determined by `p` in the properties file) will use the Traffic Route Preference function to choose their route. Drivers who use the TRPF will report the level of congestion on the route that they just used. The purpose of the function is to encourage drivers to avoid the routes that have too many people on them and to use the routes that have too few. 

After drivers have travelled along a route, they report a level of congestion. Here, "congestion" is defined by the number of drivers above the system optimum (Note that this simplifying assumption is probably not reflective of reality). If the number of drivers on a route is n<sub>route</sub> and the system optimum of that route is **n<sup>*</sup><sub>route</sub>**, then a user will report a congestion level `w` if n<sub>route</sub> - **n<sup>*</sup><sub>route</sub>** > `threshold_w`, where `w` and `threshold_w` are properties in the properties file.

The TRPF sums up all the congestion reports for a given route over the last `T` iterations.
The TRPF, is a function of iteration number t and the route, is given by:

![TRPF Equation](http://www.ee.cooper.edu/~friedm3/imgs/research_img1.png)

where

![ntauroute Equation](http://www.ee.cooper.edu/~friedm3/imgs/research_img3.png)

and

![N Equation](http://www.ee.cooper.edu/~friedm3/imgs/research_img2.png)

## How to use
### Compiling and running
To compile the code, use a terminal to navigate to the TrafficFlowNetwork folder and type:
```
make compile
```
If you don't have make installed, you can instead type:
```
javac  -d class/ -sourcepath src: src/edu/cooper/*.java
```

To run the code, you should have a directory already set up that contains a "traffic.properties" file and a "roadnet.csv" file. For example, the "BraessNet" folder is already set up appropriately.
To run the simulation, type:
```
make run dir=directory_name
```
For example, to run the BraessNet simulation, type:
```
make run dir=BraessNet
```
If you don't have make installed, you can instead type:
```
java -cp class/ edu.cooper.FlowManager directory_name
cp src/index.html directory_name/output.html
```

After you run it, if you didn't get any errors, you can open "output.html" in any browser to view the output graphs.

### File Structure
To run the simulation, you need to give it a directory name that contains a few specific files.
The directory needs to contain files called "roadnet.csv" and "traffic.properties".


#####roadnet.csv
This file contains the road network. Each line represents one road in the network. A road is defined by the names of the nodes on either side, and the cost function, cost(n) = a + n b
Each line (except for the first) should have the following format:
node1,node2,a,b
where node1 and node2 are the names of the nodes, and a and b could be any real number.


#####traffic.properties
This file contains the various parameters you can play with. The parameters are as follows:

######Algorithm Parameters
These parameters control the Traffic Route Preference Function Algorithm.

| Parameter        | Format           | Description  |
|:-------------:|:-------------:|:----- |
| w      | threshold1:weight1,threshold2:weight2,... | threshold1 is a threshold above the System Optimum and weight1 is the actual weight. In other words, "30:2" means that if there are more than 30 drivers above the System Optimum for a route, than the drivers on that route could report a congestion level of 2 |
| p | 0 <= p <= 1 | `p` is the fraction of agents using the TRPF. When an agent is initialized, it will "decide" whether to use the TRPF by choosing a random number from 0 to 1 and seeing if it's less than p. If it is, it will use the TRPF. If not, it won't. |
| T | integer >= 1 | T is the number of previous rounds to use for calculating the TRPF. |

######Model Parameters
These parameters control the traffic model that we're using.

| Parameter        | Format           | Description  |
|:-------------:|:-------------:|:----- |
| num_agents      | integer > 0 | The total number of agents travelling |
| G | 0 <=G<= 1 | G is the fraction of agents who will change routes from one iteration to the next. Whenever an agent needs to choose a route, it will pick a random number from 0 to 1. If the number is less than G, then the agent chooses a new route, otherwise, it uses the same route as last time. |
|routeN| "N" should actually be a number | List the possible routes that an agent can use. e.g. route1=S,M,D. route2=S,N,D |
|routeN.opt| "N" should actually be a number | routeN.opt is the number of drivers who should travel on route "N" at the System Optimum. e.g. route1.opt=100. route2.opt=100. **This needs to be calculated based on `num_agents`!** |

######Simulation Parameters
These parameters control the simulation.

| Parameter        | Format           | Description  |
|:-------------:|:-------------:|:----- |
| num_iterations      | integer > 0 | Number of rounds to run the simulation. |
