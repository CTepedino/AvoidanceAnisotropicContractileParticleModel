package ss;

import java.util.Locale;

public class Main {

    public static void main(String[] args){
        for (int qIn = 1; qIn <=10; qIn+=1) {
            Simulation simulation = new Simulation(qIn, (double) 1 / 33, 200, String.format(Locale.US,"output_Qin_%.2f.txt", (double) qIn));
            simulation.run();
            System.out.println(qIn);
        }
    }

}
