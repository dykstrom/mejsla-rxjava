package se.dykstrom.rxjava.swing.mandel;

class Line {

    private final int y;

    private final int[] escapeTimes;

    Line(int y, int[] escapeTimes) {
        this.y = y;
        this.escapeTimes = escapeTimes;
    }

    int getY() {
        return y;
    }

    int[] getEscapeTimes() {
        return escapeTimes;
    }
}
