package se.dykstrom.rxjava.swing.mandel;

import java.util.Objects;

/**
 * Represents the coordinate space used to calculate the Mandelbrot set.
 *
 * @author Johan Dykstrom
 */
class Coordinates {

    public static final Coordinates START = new Coordinates(-2.0, 1.0, -1.5, 1.5);

    private final double minX;
    private final double maxX;
    private final double minY;
    private final double maxY;

    Coordinates(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    double getMinX() {
        return minX;
    }

    double getMaxX() {
        return maxX;
    }

    double getMinY() {
        return minY;
    }

    double getMaxY() {
        return maxY;
    }

    @Override
    public String toString() {
        return "[" + minX + " -- " + maxX + ", " + minY + " -- " + maxY + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return Objects.equals(minX, that.minX) &&
               Objects.equals(maxX, that.maxX) &&
               Objects.equals(minY, that.minY) &&
               Objects.equals(maxY, that.maxY);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minX, maxX, minY, maxY);
    }
}
