package ss.rules;

import ss.Parameters;
import ss.model.Particle;
import ss.model.Vector2D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AACPM implements Ruleset {


    @Override
    public void updateParticles(List<Particle> particles, double dt){
        for (Particle p: particles){
            boolean inContact = false;

            List<Particle> neighbors = getFrontColliders(p, particles);
            Vector2D avoidance = new Vector2D(0, 0);

            for (Particle other: neighbors) {
                inContact = true;
                avoidance = avoidance.add(computeCollisionVector(p, other));
            }

            Vector2D wallAvoidance = computeWallAvoidance(p);
            if (wallAvoidance.magnitude() > 0) {
                inContact = true;
                avoidance = avoidance.add(wallAvoidance);
            }

            Vector2D desired = new Vector2D(p.goesLeft ? -1 : 1, 0);

            if (!inContact) {
                p.radius = Math.min(Parameters.R_MAX, p.radius + Parameters.R_MAX * dt / Parameters.TAU);
                double vMag = Parameters.V_D * Math.pow((p.radius - Parameters.R_MIN) / (Parameters.R_MAX - Parameters.R_MIN), Parameters.BETA);
                p.velocity = desired.scale(vMag);
            } else {
                p.radius = Parameters.R_MIN;
                Vector2D e_a = avoidance.add(desired).normalize();
                p.velocity = e_a.scale(Parameters.V_D);
            }

            p.position = p.position.add(p.velocity.scale(dt));

        }
    }

    private List<Particle> getFrontColliders(Particle p, List<Particle> particles) {
        List<Particle> front = new ArrayList<>();
        Vector2D desired = new Vector2D(p.goesLeft ? -1 : 1, 0);

        for (Particle other : particles) {
            if (p == other) continue;

            double dist = p.position.distanceTo(other.position);
            if (dist < p.radius + other.radius) {
                Vector2D rij = other.position.subtract(p.position);
                double angle = desired.angleWith(rij);
                if (angle < Math.PI / 2) {
                    front.add(other);
                }
            }
        }

        front.sort(Comparator.comparingDouble(o -> o.position.distanceTo(p.position)));
        return front.subList(0, Math.min(2, front.size()));
    }

    private Vector2D computeCollisionVector(Particle i, Particle j){
        Vector2D rij = i.position.subtract(j.position);
        Vector2D vij = j.velocity.subtract(i.velocity);

        Vector2D eij = rij.normalize();

        double alpha = eij.signedAngleTo(vij);
        double f = Math.abs(Math.abs(alpha) - Math.PI / 2);

        double rotAngle = -Math.signum(alpha) * f;
        Vector2D eij_c = eij.rotate(rotAngle);

        double dij = rij.magnitude();
        return eij_c.scale(Parameters.A_P * Math.exp(-dij / Parameters.B_P));
    }

    private Vector2D computeWallAvoidance(Particle p){
        Vector2D result = new Vector2D(0, 0);

        double dTop = Parameters.W - p.position.y;
        double dBottom = p.position.y;

        if (dTop < 2 * Parameters.R_MIN) {
            result = result.add(new Vector2D(0, -1).scale(Parameters.A_W * Math.exp(-dTop / Parameters.B_W)));
        }
        if (dBottom < 2 * Parameters.R_MIN) {
            result = result.add(new Vector2D(0, 1).scale(Parameters.A_W * Math.exp(-dBottom / Parameters.B_W)));
        }

        return result;
    }
}
