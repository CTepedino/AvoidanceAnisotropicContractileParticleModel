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

    public double angleWith(Vector2D v) {
        double dot = this.dot(v);
        double mags = this.magnitude() * v.magnitude();
        if (mags == 0) return 0;
        double cos = dot / mags;
        return Math.acos(Math.max(-1.0, Math.min(1.0, cos))); // evita NaN por redondeo
    }

    public double signedAngleTo(Vector2D other) {
        double dot = this.dot(other);
        double det = this.x * other.y - this.y * other.x; // determinante 2D
        return Math.atan2(det, dot); // valor en [-π, π]
    }

    public Vector2D rotate(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vector2D(cos * this.x - sin * this.y, sin * this.x + cos * this.y);
    }

    public Vector2D projectOnto(Vector2D v) {
        double scale = this.dot(v) / v.dot(v);
        return v.scale(scale);
    }

    @SuppressWarnings("all")
    public Vector2D perpendicular(){
        return new Vector2D(-y, x);
    }

    @Override
    public String toString() {
        return String.format("(%.3f, %.3f)", x, y);
    }
}
