/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cooper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author EliFriedman
 */
public class PrettyOutput {

    HashMap<String, String> series;

    public PrettyOutput() {
        series = new HashMap<>();
    }

    public void createNewSeries(String name) {
        series.put(name, "[");
    }

    public void addToSeries(String name, String x, String y) {
        String s = series.get(name);
        String add = "{x:" + x + ",y:" + y + "},";
        series.put(name, s + add);
    }

    public void endSeries(String name) {
        String s = series.get(name);
        series.put(name, s + "]");
    }

    public void combineSeries(String newname, String[] names) {
        String s = "[";
        for (String name : names) {
            s += series.get(name) + ",";
        }
        s += "]";
        series.put(newname, s);
    }
    
    public void writeVariable(String filename, String varname, String seriesname) {
        String s = "var " + varname + " = ";
        s += series.get(seriesname);
        s += ";";
        try {
            FileWriter f = new FileWriter(filename);
            f.write(s+"\n");
            f.close();
        } catch (IOException ex) {
            Logger.getLogger(PrettyOutput.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
