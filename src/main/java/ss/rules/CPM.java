package ss.rules;

import ss.Parameters;
import ss.model.Particle;
import ss.model.Vector2D;

import java.util.List;

public class CPM implements Ruleset{
    @Override
    public void updateParticles(List<Particle> particles, double dt) {
        for (Particle p: particles){
            boolean inContact = false;
            Vector2D velocityUnitVector = null;

            for(Particle other: particles){
                if (p == other) continue;
                if (interacting(p, other)){
                    inContact = true;
                    velocityUnitVector = p.position.subtract(other.position).normalize();
                    break;
                }
            }

            double wallDistance = Math.min(Parameters.W - p.position.y, p.position.y);
            if (wallDistance < p.radius) {
                inContact = true;
                velocityUnitVector = new Vector2D(0, p.position.y < p.radius? 1: -1);
            }

            if (!inContact){
                p.radius = Math.min(Parameters.R_MAX, p.radius + Parameters.R_MAX * dt / Parameters.TAU);

                double vMag = Parameters.V_D * Math.pow((p.radius - Parameters.R_MIN)/(Parameters.R_MAX - Parameters.R_MIN), Parameters.BETA);
                p.velocity = new Vector2D(vMag * (p.goesLeft? -1: 1), 0);

            } else {
                p.radius = Parameters.R_MIN;

                double vMag = Parameters.V_D;
                p.velocity = velocityUnitVector.scale(vMag);

            }

            p.position = p.position.add(p.velocity.scale(dt));

        }
    }

    private boolean interacting(Particle a, Particle b){
        return a.position.distanceTo(b.position) < a.radius + b.radius;
    }
}
