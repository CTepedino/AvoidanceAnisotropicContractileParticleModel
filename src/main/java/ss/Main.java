package ss;

public class Main {

    public static void main(String[] args){

        Simulation simulation = new Simulation(5, (double)1/33, 40, "out.txt");
        simulation.run();

    }

}
