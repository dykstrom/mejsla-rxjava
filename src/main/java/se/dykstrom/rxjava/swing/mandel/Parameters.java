package se.dykstrom.rxjava.swing.mandel;

/**
 * Contains parameters for calculating one segment of the image.
 */
class Parameters {

    private final int startY;
    private final int width;
    private final int height;
    private final Coordinates coordinates;

    Parameters(int startY, int width, int height, Coordinates coordinates) {
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.coordinates = coordinates;
    }

    int getStartY() {
        return startY;
    }

    int getWidth() {
        return width;
    }

    int getHeight() {
        return height;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        return "[" + width + "x" + height + ", " + startY + ", " + coordinates + "]";
    }
}
