package ss.model;

public class Particle {

    public int id;
    public Vector2D position;
    public Vector2D velocity;
    public double radius;

    public boolean goesLeft;

    public Particle(int id, Vector2D position, Vector2D velocity, double radius, boolean goesLeft) {
        this.id = id;
        this.position = position;
        this.velocity = velocity;
        this.radius = radius;
        this.goesLeft = goesLeft;
    }

}
