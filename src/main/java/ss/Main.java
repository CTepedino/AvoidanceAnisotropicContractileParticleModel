package ss;

import java.util.Locale;

public class Main {

    public static void main(String[] args){

        Simulation simulation = new Simulation(1, (double)1/33, 200, "out.txt");
        simulation.run();

    }

}
