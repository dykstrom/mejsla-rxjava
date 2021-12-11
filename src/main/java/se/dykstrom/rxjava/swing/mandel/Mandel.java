package se.dykstrom.rxjava.swing.mandel;

import javax.swing.*;

/**
 * This is the main class of the Mandelbrot application. It takes one optional command line argument,
 * and that is the number of segments to divide the image into when drawing. All image segments are
 * calculated in parallel and put together when actually drawing them. The default number of segments
 * is 1.
 *
 * @author Johan Dykstrom
 */
class Mandel {
    public static void main(String[] args) {
        int segments = (args.length > 0) ? Integer.parseInt(args[0]) : 1;
        SwingUtilities.invokeLater(() -> {
            MandelView view = new MandelView();
            new MandelController(view, segments);
            view.setVisible(true);
            view.setLocationRelativeTo(null);
        });
    }
}
