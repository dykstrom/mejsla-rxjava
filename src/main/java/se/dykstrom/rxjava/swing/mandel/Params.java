package se.dykstrom.rxjava.swing.mandel;

class Params {

    static final int NUM_ITERATIONS = 100;

    private final int firstLine;
    private final int width;
    private final int height;
    private final double minY;
    private final double maxY;

    Params(int firstLine, int width, int height, double minY, double maxY) {
        this.firstLine = firstLine;
        this.width = width;
        this.height = height;
        this.minY = minY;
        this.maxY = maxY;
    }

    int getFirstLine() {
        return firstLine;
    }

    int getWidth() {
        return width;
    }

    int getHeight() {
        return height;
    }

    double getMinY() {
        return minY;
    }

    double getMaxY() {
        return maxY;
    }

    @Override
    public String toString() {
        return "[" + width + "x" + height + ", " + firstLine + ", " + minY + " -- " + maxY + "]";
    }
}
