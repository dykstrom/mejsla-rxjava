package se.dykstrom.rxjava.swing.mandel;

/**
 * Contains parameters used to calculate an image, for example the minimum and maximum value
 * of the X and Y coordinates for which to calculate the Mandelbrot set.
 *
 * @author Johan Dykstrom
 */
class Coordinates {

    private final double minX;
    private final double maxX;
    private final double minY;
    private final double maxY;

    Coordinates() {
        this(-2.0, 1.0, -1.5, 1.5);
    }

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
}
