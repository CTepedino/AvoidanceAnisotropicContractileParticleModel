package ss.model;

import ss.Parameters;

public class Contact {
    public static boolean isFrontCollider(Particle i, Particle j) {
        Vector2D rij = j.position.subtract(i.position);
        double distance = rij.magnitude();

        boolean overlap = distance < (i.radius + j.radius);
        if (!overlap) return false;

        if (i.radius == Parameters.R_MIN) { //Colision fisica -> siempre
            return true;
        }

        //Colision de espacio personal -> Tiene que estar adelante para contar

        double angle = angleBetween(rij, i.velocity);
        if (angle < -Math.PI / 2 || angle > Math.PI / 2) {
            return false;
        }

        return lineIntersection(i, j);
    }

    private static double angleBetween(Vector2D v1, Vector2D v2) {
        return Math.atan2(v2.cross(v1), v2.dot(v1));
    }

    private static boolean lineIntersection(Particle i, Particle j) {
        Vector2D viUnit = i.velocity.normalize();
        Vector2D perp = new Vector2D(-viUnit.y, viUnit.x);

        Vector2D left = i.position.add(perp.scale(Parameters.R_MIN));
        Vector2D right = i.position.subtract(perp.scale(Parameters.R_MIN));

        return lineIntersectsCircle(left, viUnit, j.position, j.radius)
                || lineIntersectsCircle(right, viUnit, j.position, j.radius);
    }

    private static boolean lineIntersectsCircle(Vector2D p, Vector2D dir, Vector2D center, double radius) {
        Vector2D f = p.subtract(center);

        double a = dir.dot(dir);
        double b = 2 * f.dot(dir);
        double c = f.dot(f) - radius * radius;

        double discriminant = b * b - 4 * a * c;
        return discriminant >= 0;
    }

}
