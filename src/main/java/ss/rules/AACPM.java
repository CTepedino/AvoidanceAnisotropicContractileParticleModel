package ss.rules;

import ss.Parameters;
import ss.model.Particle;
import ss.model.Vector2D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static ss.Parameters.*;

public class AACPM implements Ruleset {
    @Override
    public List<Particle> updateParticles(List<Particle> particles) {
        List<Particle> result = new ArrayList<>();

        for (Particle p: particles) {
            boolean inContact = false;
            for(Particle j: particles){
                if (p == j) continue;
                if (areColliding(p, j)){
                    p.radius = R_MIN;
                    inContact = true;
                    break;
                }
            }
            if (!inContact){
                p.radius = Math.min(R_MAX, p.radius+R_MAX*DT/TAU);
            }

            Vector2D dir;
            double speed;

            if (inContact){
                Vector2D repulsion = new Vector2D(0, 0);
                for (Particle j: particles){
                    if (p == j) continue;

                    if (areColliding(p, j)){
                        repulsion = repulsion.add(p.position.subtract(j.position)).normalize();
                    }
                }
                dir = repulsion.normalize();
                speed = Parameters.V_D;
            } else {
                dir = avoidance(p, particles);
                speed = V_D * Math.pow((p.radius - R_MIN) / (R_MAX - R_MIN), BETA);
            }

            Vector2D vel = dir.scale(speed);
            Vector2D pos = p.position.add(vel.scale(DT));
            double x = pos.x;
            double y = Math.max(p.radius, Math.min(W - p.radius, pos.y));

            Particle pNext = new Particle(
                p.id,
                new Vector2D(x, y),
                vel,
                p.radius,
                p.goesLeft
            );

            result.add(pNext);
        }

        return result;
    }


    private boolean areColliding(Particle p_i, Particle p_j) {

        double r_min = R_MIN;
        Vector2D r_ij=p_j.position.subtract(p_i.position);
        boolean radiiOverlap=r_ij.magnitude() < p_i.radius+p_j.radius;
        boolean firstCondition=p_i.radius==r_min && radiiOverlap;
        if(firstCondition){
            return true;
        }

        Vector2D vel=p_i.velocity;
        double cosBeta=r_ij.dot(vel)/(vel.magnitude()*r_ij.magnitude());
        if(p_i.radius!=r_min && cosBeta>=0){
            firstCondition=true;
        }

        return firstCondition && radiiOverlap && intersectsTangentialStrip(p_i,p_j);
    }

    private boolean intersectsTangentialStrip(Particle p_i, Particle p_j) {
        Vector2D v_i = p_i.velocity;
        if (v_i.magnitude() == 0.0) {
            return false;
        }

        Vector2D dir = v_i.normalize();
        Vector2D perp = dir.perpendicular();

        Vector2D diff = p_j.position.subtract(p_i.position);

        double forward = dir.dot(diff);
        if (forward < 0) return false;

        double dist = Math.abs(perp.dot(diff));

        return dist < R_MIN + p_j.radius;
    }


    public Vector2D avoidance(Particle p_i, List<Particle> particles) {

        Vector2D target = new Vector2D(p_i.goesLeft ? 0 : L, p_i.position.y);
        Vector2D e_t = target.subtract(p_i.position).normalize();
        Vector2D sum_n_jc = new Vector2D(0, 0);

        Vector2D v_i = p_i.velocity;
        List<Particle> nearestFront =particles.stream().filter(
                p-> isInFront(p_i,p)).sorted(Comparator.comparingDouble(p->p.position.distanceTo(p_i.position))).toList();

        for (Particle p_j : nearestFront.subList(0,Math.min(2,nearestFront.size()))) {
            Vector2D r_ij = p_j.position.subtract(p_i.position);
            double d = r_ij.magnitude();

            Vector2D v_ij = p_j.velocity.subtract(v_i);
            Vector2D e_ij = p_i.position.subtract(p_j.position).normalize();
            double beta = v_ij.angleWith(e_t);

            Vector2D e_ij_c;
            if (v_ij.magnitude()==0||Math.abs(beta) <Math.PI/2) {
                e_ij_c = new Vector2D(0,0);
            }

            else {
                double dot = e_ij.dot(v_ij) / v_ij.magnitude();
                double det = e_ij.cross(v_ij) / v_ij.magnitude();
                double alpha = Math.atan2(det, dot);
                double fa = Math.abs(Math.abs(alpha) - Math.PI / 2);
                e_ij_c = e_ij.rotate(-Math.signum(alpha) * fa);
            }
            double w_j = A_P * Math.exp(-d / B_P);
            sum_n_jc=sum_n_jc.add(e_ij_c.scale(w_j));
        }


        double dBottom = p_i.position.y;
        boolean closerToBottom = dBottom < W - dBottom;
        Vector2D w = new Vector2D(p_i.position.x, closerToBottom ? 0.0 : W);
        Vector2D ei_w = w.subtract(p_i.position).normalize();
        double d_iw = closerToBottom ? dBottom : W - dBottom;
        Vector2D n_wc = ei_w.scale(A_W * Math.exp(-d_iw / B_W));

        return e_t.add(sum_n_jc).add(n_wc).normalize();
    }

    public boolean isInFront(Particle pi, Particle pj) {
        if (pi == pj) return false;
        double x = pi.position.x;
        if (!pi.goesLeft) {
            return pj.position.x >= x;
        }
        return pj.position.x <= x;
    }
}
