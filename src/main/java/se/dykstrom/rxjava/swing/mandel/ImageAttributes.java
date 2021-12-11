package se.dykstrom.rxjava.swing.mandel;

import static se.dykstrom.rxjava.swing.mandel.Coordinates.INITIAL_COORDINATES;
import static se.dykstrom.rxjava.swing.mandel.Coordinates.INITIAL_SIZE;
import static se.dykstrom.rxjava.swing.mandel.MandelPanel.INITIAL_IMAGE_SIZE;

/**
 * Defines attributes needed to draw an image, that is coordinates for the upper left corner in the Mandelbrot
 * coordinate space, and a scale to convert between Mandelbrot coordinates and pixels.
 */
public record ImageAttributes(Coordinates coordinates, double scale) {

    static final ImageAttributes INITIAL_ATTRIBUTES = new ImageAttributes(INITIAL_COORDINATES, INITIAL_SIZE / INITIAL_IMAGE_SIZE);

    public ImageAttributes withCoordinates(Coordinates coordinates) {
        return new ImageAttributes(coordinates, scale);
    }

    @Override
    public String toString() {
        return "[" + coordinates + ", " + scale + "]";
    }
}
