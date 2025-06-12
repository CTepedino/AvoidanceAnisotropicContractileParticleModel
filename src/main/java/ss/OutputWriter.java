package ss;

import ss.model.Particle;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class OutputWriter {

    private final BufferedWriter writer;

    public OutputWriter(String outPath) {
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outPath), StandardCharsets.UTF_8));
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void printState(double time, List<Particle> particles){
        try {
            for (Particle particle : particles) {
                writer.write(String.format(Locale.US, "%f %d %f %f %f %f %f\n",
                    time, particle.id,
                    particle.position.x, particle.position.y,
                    particle.velocity.x, particle.velocity.y,
                    particle.radius
                ));
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e){
            throw new RuntimeException();
        }
    }


}
