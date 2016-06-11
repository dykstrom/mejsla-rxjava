package se.dykstrom.rxjava.swing.mandel;

/**
 * Represents the coordinates in the Mandelbrot coordinate space used to calculate the image,
 * or one part of the image.
 *
 * @author Johan Dykstrom
 */
class Coordinates {

    static final Coordinates INITIAL_COORDINATES = new Coordinates(-2.0, -1.5);

    static final double INITIAL_SIZE = 3.0;

    private final double minX;
    private final double minY;

    Coordinates(double minX, double minY) {
        this.minX = minX;
        this.minY = minY;
    }

    double getMinX() {
        return minX;
    }

    double getMinY() {
        return minY;
    }

    Coordinates withMinY(double minY) {
        return new Coordinates(minX, minY);
    }

    @Override
    public String toString() {
        return "[" + minX + ", " + minY + "]";
    }
}
