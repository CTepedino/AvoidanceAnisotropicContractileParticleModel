package ss;

public class Main {

    public static void main(String[] args){

        Simulation simulation = new Simulation(1, (double)1/33, 40, "out.txt");
        simulation.run();

    }

}
