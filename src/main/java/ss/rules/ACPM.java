package ss.rules;

import ss.Parameters;
import ss.model.Particle;
import ss.model.Vector2D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ACPM implements Ruleset {
    @Override
    public void updateParticles(List<Particle> particles, double dt) {
        for (Particle p : particles) {
            boolean inContact = false;
            Vector2D repulsion = new Vector2D(0, 0);

            if (Double.compare(p.radius, Parameters.R_MIN)==0){
                for(Particle other: particles){
                    if (p == other) continue;
                    double distance = p.position.distanceTo(other.position);
                    if (distance < p.radius + other.radius){
                        inContact = true;
                        repulsion = repulsion.add(p.position.subtract(other.position).normalize());
                    }
                }
            } else {
                List<Particle> frontColliders = getFrontColliders(p, particles);
                for (Particle other: frontColliders){
                    Vector2D diff = p.position.subtract(other.position);
                    repulsion = repulsion.add(diff.normalize());
                    inContact = true;
                }
            }

            double yTopDist = Parameters.W - p.position.y;
            double yBotDist = p.position.y;

            if (yTopDist < Parameters.R_MIN) {
                repulsion = repulsion.add(new Vector2D(0, -1));
                inContact = true;
            }
            if (yBotDist < Parameters.R_MIN) {
                repulsion = repulsion.add(new Vector2D(0, 1));
                inContact = true;
            }

            if (!inContact){
                p.radius = Math.min(Parameters.R_MAX, p.radius + Parameters.R_MAX * dt / Parameters.TAU);

                double vMag = Parameters.V_D * Math.pow((p.radius - Parameters.R_MIN)/(Parameters.R_MAX - Parameters.R_MIN), Parameters.BETA);
                p.velocity = new Vector2D(vMag * (p.goesLeft? -1: 1), 0);

            } else {
                p.radius = Parameters.R_MIN;
                double vMag = Parameters.V_D;
                Vector2D desired = new Vector2D(p.goesLeft ? -1 : 1, 0);
                Vector2D avoidanceDirection = repulsion.add(desired).normalize();
                p.velocity = avoidanceDirection.scale(vMag);
            }


            p.position = p.position.add(p.velocity.scale(dt));
        }
    }

    private List<Particle> getFrontColliders(Particle p, List<Particle> particles){
        List<Particle> frontColliders = new ArrayList<>();
        Vector2D direction = new Vector2D(p.goesLeft? -1:1, 0);

        for (Particle other: particles){
            if (p == other) continue;

            double distance = p.position.distanceTo(other.position);
            if(distance < p.radius + other.radius) {
                double angle = direction.angleWith(p.position.subtract(other.position));
                if (angle < Math.PI / 2) {
                    frontColliders.add(other);
                }
            }


        }

        frontColliders.sort(Comparator.comparingDouble(o -> o.position.distanceTo(p.position)));
        return frontColliders;
    }



}
