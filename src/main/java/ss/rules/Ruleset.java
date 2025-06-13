package ss.rules;

import ss.model.Particle;

import java.util.List;

public interface Ruleset {

    List<Particle> updateParticles(List<Particle> particles);
}
