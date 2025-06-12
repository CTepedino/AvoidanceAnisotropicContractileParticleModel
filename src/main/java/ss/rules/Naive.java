package ss.rules;

import ss.model.Particle;

import java.util.List;

public class Naive implements Ruleset {


    @Override
    public void updateParticles(List<Particle> particles, double dt) {
        for(Particle particle: particles){
            particle.position = particle.position.add(particle.velocity.scale(dt));
        }
    }
}
