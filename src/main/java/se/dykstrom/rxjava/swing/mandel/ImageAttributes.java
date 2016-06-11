package se.dykstrom.rxjava.swing.mandel;

import static se.dykstrom.rxjava.swing.mandel.Coordinates.INITIAL_COORDINATES;
import static se.dykstrom.rxjava.swing.mandel.MandelPanel.INITIAL_IMAGE_SIZE;

/**
 * Defines attributes needed to draw an image, that is coordinates for the upper left corner in the Mandelbrot
 * coordinate space, and a scale to convert between Mandelbrot coordinates and pixels.
 */
public class ImageAttributes {

    static final ImageAttributes INITIAL_ATTRIBUTES = new ImageAttributes(INITIAL_COORDINATES, Coordinates.INITIAL_SIZE / INITIAL_IMAGE_SIZE);

    private final Coordinates coordinates;
    private final double scale;

    public ImageAttributes(Coordinates coordinates, double scale) {
        this.coordinates = coordinates;
        this.scale = scale;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
     * Returns the scale, that is, the amount of Mandelbrot coordinate space per pixel.
     */
    public double getScale() {
        return scale;
    }

    public ImageAttributes withCoordinates(Coordinates coordinates) {
        return new ImageAttributes(coordinates, scale);
    }

    @Override
    public String toString() {
        return "[" + coordinates + ", " + scale + "]";
    }
}
