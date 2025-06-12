package ss;

import ss.model.Particle;
import ss.model.Vector2D;
import ss.rules.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Simulation {

    private int generatedLeft = 0;
    private int generatedRight = 0;

    private int exitedLeft = 0;
    private int exitedRight = 0;

    private final Ruleset ruleset = new AACPM();
    private final List<Particle> particles;

    private double timeSinceLastInjection = 0;

    private boolean saturated = false;

    private final double injectionInterval;
    private final double dt_2;
    private final double maxTime;
    private final OutputWriter writer;

    private final Random random = new Random();

    private int nextId = 0;

    public Simulation(double q_in, double dt_2, double maxTime, String outPath){
        this.injectionInterval = 1.00 / q_in;
        this.dt_2 = dt_2;
        this.maxTime = maxTime;
        this.writer = new OutputWriter(outPath);
        particles = new ArrayList<>();
    }

    public void run(){
        double time = 0;
        double timeSinceLastPrint = 0;
        while (/*(generatedLeft < Parameters.PARTICLES_PER_SIDE && generatedRight < Parameters.PARTICLES_PER_SIDE) && */!saturated && Double.compare(time, maxTime) <= 0){


            if (timeSinceLastPrint >= dt_2 - 1e-9) {
                writer.printState(time, particles);
                timeSinceLastPrint = 0;
            }

            boolean generate = true;
            if ((generatedLeft == Parameters.PARTICLES_PER_SIDE && generatedRight == Parameters.PARTICLES_PER_SIDE)){
                generate = false;
            }
            step(generate);

            time += Parameters.DT;
            timeSinceLastPrint += Parameters.DT;

        }

        writer.close();

    }

    private void step(boolean generateParticles){
        if (generateParticles){
            generateParticles();
        }

        ruleset.updateParticles(particles, Parameters.DT);

        removedArrived();
    }

    private void generateParticles(){
        timeSinceLastInjection += Parameters.DT;

        while (timeSinceLastInjection >= injectionInterval){
            tryInjectParticle(true);
            tryInjectParticle(false);
            timeSinceLastInjection -= injectionInterval;
        }
    }

    private void tryInjectParticle(boolean goesLeft) {
        final int maxTries = 100;
        for (int i = 0; i < maxTries; i++) {
            double x = goesLeft? Parameters.L: 0;
            double y = Parameters.R_MIN + random.nextDouble() * (Parameters.W - 2 * Parameters.R_MIN);
            Vector2D position = new Vector2D(x, y);

            if (!isOverlapping(position)){
                particles.add(new Particle(
                        nextId++,
                        position,
                        new Vector2D(Parameters.V_D * (goesLeft? -1: 1), 0),
                        Parameters.R_MIN,
                        goesLeft
                ));
                if (goesLeft){
                    generatedRight++;
                } else {
                    generatedLeft++;
                }

                return;
            }
        }
        saturated = true;
    }

    private boolean isOverlapping(Vector2D position) {
        for (Particle p : particles) {
            double distance = p.position.distanceTo(position);
            if (distance < 2 * Parameters.R_MIN) {
                return true;
            }
        }
        return false;
    }

    private void removedArrived(){
        particles.removeIf(p -> {
            if (p.goesLeft && p.position.x <= 0){
                exitedLeft++;
                return true;
            } else if (!p.goesLeft && p.position.x >= Parameters.L){
                exitedRight++;
                return true;
            }
            return false;
        });
    }
}
