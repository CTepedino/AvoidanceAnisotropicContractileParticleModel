package ss.rules;

import ss.model.Particle;

import java.util.List;

public interface Ruleset {

    void updateParticles(List<Particle> particles, double dt);
}
