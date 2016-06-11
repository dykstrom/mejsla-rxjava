package se.dykstrom.rxjava.swing.mandel;

/**
 * Contains parameters for calculating one segment of the image, including the y-coordinate for the
 * first pixel line in the segment, the width and height of the segment in pixels, and the image
 * attributes that defines coordinates and scale.
 */
class Parameters {

    private final int firstY;
    private final int width;
    private final int height;
    private final ImageAttributes imageAttributes;

    Parameters(int firstY, int width, int height, ImageAttributes imageAttributes) {
        this.firstY = firstY;
        this.width = width;
        this.height = height;
        this.imageAttributes = imageAttributes;
    }

    int getFirstY() {
        return firstY;
    }

    int getWidth() {
        return width;
    }

    int getHeight() {
        return height;
    }

    ImageAttributes getImageAttributes() {
        return imageAttributes;
    }

    @Override
    public String toString() {
        return "[" + firstY + ", " + width + "x" + height + ", " + imageAttributes + "]";
    }
}
