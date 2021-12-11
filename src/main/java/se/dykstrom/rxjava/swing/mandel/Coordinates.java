package se.dykstrom.rxjava.swing.mandel;

/**
 * Represents the coordinates in the Mandelbrot coordinate space used to calculate the image,
 * or one part of the image.
 *
 * @author Johan Dykstrom
 */
record Coordinates(double minX, double minY) {

    static final Coordinates INITIAL_COORDINATES = new Coordinates(-2.0, -1.5);

    static final double INITIAL_SIZE = 3.0;

    Coordinates withMinY(double minY) {
        return new Coordinates(minX, minY);
    }

    @Override
    public String toString() {
        return "[" + minX + ", " + minY + "]";
    }
}
