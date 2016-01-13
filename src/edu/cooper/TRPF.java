/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cooper;

/**
 * Keeps track of the Traffic Route Preference Function (TRPF) for each route for each
 * round.
 * TRPF = sum_over_weights(congestion_weight * num_users_reporting_that_weight)/num_reports
 *      = sum_over_agents(congestion_weight) / num_reports
 * @author EliFriedman
 */
public class TRPF {
    private int[][] numreports; // keeps track of # of reports per route per round.
    private double[][] reports; // keeps track of the actual reports per route per round.
    private int numroutes;
    private int numrounds;
    private int currentround;

    /**
     * Keeps track of the Traffic Route Preference Function for each route
     * for the last numrounds rounds.
     * @param numroutes The number of routes that should be tracked.
     * @param numrounds The TRPF will be calculated using the reports from the
     * last numrounds rounds.
     */
    public TRPF(int numroutes, int numrounds) {
        this.numroutes = numroutes;
        this.numrounds = numrounds;
        currentround = 0;
        numreports = new int[numrounds][numroutes];
        reports = new double[numrounds][numroutes];
    }
    
    /**
     * This method MUST be called before adding reports in a given round. It
     * tells TRPF that we're on a new round, so it knows to put the reports in
     * the proper place.
     */
    public void newReport() {
        currentround = (currentround+1)%numrounds; // 0 <= currentround < numrounds
        // reset the current round to 0
        for(int i=0; i < numroutes; i++) {
            reports[currentround][i] = 0;
            numreports[currentround][i] = 0;
        }
    }
    
    /**
     * Add a new congestion report for a given route.
     * @param routenumber The route that the driver just travelled and is
     * reporting congestion for.
     * @param report A real valued number that kind of indicates how much
     * traffic (=# of cars above SO) on the road.
     */
    public void addReport(int routenumber, double report) {
        reports[currentround][routenumber] += report;
        numreports[currentround][routenumber]++;
    }
    
    /**
     * Gets the TRPF of a specific route. The calculation is:
     * <br/>
     * TRPF = sum_over_weights(congestion_weight * num_users_reporting_that_weight)/num_reports
     * <br/>= sum_over_agents(congestion_weight) / num_reports
     * The sum is done over the last numrounds number of rounds, where numrounds
     * was specified in the constructor.
     * @param routenumber You'll get the TRPF for route number routenumber
     * @return The TRPF for route number routenumber
     */
    public double getRouteTRPF(int routenumber) {
        double trpf = 0;
        double num = 0;
        for(int i=0; i < numrounds; i++) {
            trpf += reports[i][routenumber];
            num += numreports[i][routenumber];
        }
        if(num==0) return 1.0;
        return 1.0-trpf/num;
    }
    
    /**
     * Gets the TRPF of a specific route. The calculation is:
     * TRPF = sum_over_weights(congestion_weight * num_users_reporting_that_weight)/num_reports
     *      = sum_over_agents(congestion_weight) / num_reports
     * The sum is done over the last numrounds number of rounds, where numrounds
     * was specified in the constructor.
     * @return An array containing the TRPF for each route.
     */
    public double[] getTRPF() {
        double[] trpf = new double[numroutes];
        for(int i=0; i < numroutes; i++) {
            trpf[i] = getRouteTRPF(i);
        }
        return trpf;
    }

}
