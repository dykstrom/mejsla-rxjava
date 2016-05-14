package se.dykstrom.rxjava.swing.mandel;

/**
 * Contains RGB color data for a single line in an image.
 */
class Line {

    private final int y;

    private final int[] rgb;

    Line(int y, int[] rgb) {
        this.y = y;
        this.rgb = rgb;
    }

    /**
     * Returns the line number.
     */
    int getY() {
        return y;
    }

    /**
     * Returns an array of RGB color data for this line.
     */
    int[] getRGB() {
        return rgb;
    }
}
