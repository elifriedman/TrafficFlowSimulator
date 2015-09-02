package edu.cooper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

/**
 * Created by friedm3 on 8/6/15.
 */
public class Agent {
    private Road[] route;
    final int ID;

    public Agent(int ID)  {
        this.route = null;
        this.ID = ID;
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
    public void chooseRoute(Road[] route) {
        this.route = route;
    }

    @Override
    public String toString() {
        return "" + this.ID;
    }

}
