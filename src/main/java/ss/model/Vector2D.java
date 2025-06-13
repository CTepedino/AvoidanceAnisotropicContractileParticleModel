package ss.model;

public class Vector2D {
    public double x, y;

    public Vector2D(double x, double y){
        this.x = x;
        this.y = y;
    }

    public Vector2D add(Vector2D v) {
        return new Vector2D(this.x + v.x, this.y + v.y);
    }

    public Vector2D subtract(Vector2D v) {
        return new Vector2D(this.x - v.x, this.y - v.y);
    }

    public Vector2D scale(double s) {
        return new Vector2D(this.x * s, this.y * s);
    }

    public double dot(Vector2D v) {
        return this.x * v.x + this.y * v.y;
    }

    public double cross(Vector2D v){
        return this.x * v.y - this.y * v.x;
    }

    public double magnitude() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public Vector2D normalize() {
        double mag = magnitude();
        if (mag == 0) return new Vector2D(0, 0);
        return new Vector2D(this.x / mag, this.y / mag);
    }

    public double distanceTo(Vector2D v) {
        return this.subtract(v).magnitude();
    }



    public Vector2D rotate(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vector2D(cos * x - sin * y, sin * x + cos * y);
    }

    public Vector2D perpendicular(){
        return new Vector2D(-y, x);
    }

    public double angleWith(Vector2D other){
        double dot = this.dot(other);
        double norm1 = other.magnitude();
        double norm2 = this.magnitude();
        double cos = Math.max(-1, Math.min(dot / (norm1 * norm2), 1));
        return Math.acos(cos);
    }

    @Override
    public String toString() {
        return String.format("(%.3f, %.3f)", x, y);
    }


}
