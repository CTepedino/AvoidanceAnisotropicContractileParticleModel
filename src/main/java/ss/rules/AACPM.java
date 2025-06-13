package ss.rules;

import ss.Parameters;
import ss.model.Contact;
import ss.model.Particle;
import ss.model.Vector2D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AACPM implements Ruleset {


    @Override
    public List<Particle> updateParticles(List<Particle> particles){
        List<Particle> next = new ArrayList<>();

        for (Particle p: particles){
            Particle nextParticle = new Particle(p.id, p.position, p.velocity, p.radius, p.goesLeft);
            boolean inContact = false;

            Vector2D repulsion = new Vector2D(0, 0);
            Vector2D desired = new Vector2D(p.goesLeft ? -1 : 1, 0);

            List<Particle> frontColliders = getFrontColliders(p, particles);
            for (Particle other: frontColliders) {
                inContact = true;
                repulsion = repulsion.add(p.position.subtract(other.position).normalize());
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

            if (!inContact) {
                Vector2D avoidance = new Vector2D(0, 0);
                List<Particle> front = getTwoNearestFront(p, particles);
                for (Particle other: front){
                    avoidance = avoidance.add(computeCollisionVector(p, other));
                }

                avoidance = avoidance.add(computeWallAvoidance(p));

                System.out.println(avoidance);

                nextParticle.radius = Math.min(Parameters.R_MAX, p.radius + Parameters.R_MAX * Parameters.DT / Parameters.TAU);
                Vector2D e_a = avoidance.add(desired).normalize();

                double vMag = Parameters.V_D * Math.pow((nextParticle.radius - Parameters.R_MIN)/(Parameters.R_MAX - Parameters.R_MIN), Parameters.BETA);

                nextParticle.velocity = e_a.scale(vMag);
            } else {
                nextParticle.radius = Parameters.R_MIN;
                double vMag = Parameters.V_D;
                Vector2D avoidanceDirection = repulsion.add(desired).normalize();
                nextParticle.velocity = avoidanceDirection.scale(vMag);
            }

            nextParticle.position = nextParticle.position.add(nextParticle.velocity.scale(Parameters.DT));
            next.add(nextParticle);
        }

        return next;
    }

    private List<Particle> getFrontColliders(Particle p, List<Particle> particles){
        List<Particle> frontColliders = new ArrayList<>(particles.stream()
                .filter(other -> other != p && Contact.isFrontCollider(p, other))
                .toList());

        frontColliders.sort(Comparator.comparingDouble(o -> o.position.distanceTo(p.position)));
        return frontColliders;
    }



    private List<Particle> getTwoNearestFront(Particle p, List<Particle> particles){
        Vector2D vi = p.velocity;

        return particles.stream()
                .filter(j -> j != p)
                .filter(j -> {
                    Vector2D rij = j.position.subtract(p.position);
                    double angle = vi.signedAngleTo(rij);
                    return Math.abs(angle) <= Math.PI / 2;
                })
                .sorted(Comparator.comparingDouble(j -> j.position.distanceTo(p.position)))
                .limit(2)
                .toList();
    }


    private Vector2D computeCollisionVector(Particle i, Particle j){
        Vector2D rij = i.position.subtract(j.position);
        Vector2D vij = j.velocity.subtract(i.velocity);

        double dij = rij.magnitude();

        double beta = i.velocity.signedAngleTo(rij);
        if (Math.abs(beta) < Math.PI / 2) {
            return new Vector2D(0, 0);
        }

        Vector2D eij = rij.normalize();

        double alpha = eij.signedAngleTo(vij);

        double fAlpha = Math.abs(Math.abs(alpha) - Math.PI / 2);

        double rotationAngle = -Math.signum(alpha) * fAlpha;
        Vector2D ecij = eij.rotate(rotationAngle);

        double scale = Parameters.A_P * Math.exp(-dij / Parameters.B_P);
        return ecij.scale(scale);
    }

    private Vector2D computeWallAvoidance(Particle p){
        Vector2D avoidance = new Vector2D(0, 0);

        double distBottom = p.position.y;
        double distTop = Parameters.W - p.position.y;

        if (distBottom < distTop) {
            Vector2D w = new Vector2D(p.position.x, 0);
            Vector2D e = p.position.subtract(w).normalize();
            double d = p.position.y;
            Vector2D n = e.scale(Parameters.A_W * Math.exp(-d / Parameters.B_W));
            avoidance = avoidance.add(n);
        } else {
            Vector2D w = new Vector2D(p.position.x, Parameters.W);
            Vector2D e = p.position.subtract(w).normalize();
            double d = Parameters.W - p.position.y;
            Vector2D n = e.scale(Parameters.A_W * Math.exp(-d / Parameters.B_W));
            avoidance = avoidance.add(n);
        }

        return avoidance;
    }
}
