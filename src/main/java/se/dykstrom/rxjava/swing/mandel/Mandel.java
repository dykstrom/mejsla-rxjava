package se.dykstrom.rxjava.swing.mandel;

import javax.swing.*;

class Mandel {
    public static void main(String args[]) {
        int segments = (args.length > 0) ? Integer.parseInt(args[0]) : 1;
        SwingUtilities.invokeLater(() -> {
            MandelView view = new MandelView();
            new MandelController(view, segments);
            view.setVisible(true);
            view.setLocationRelativeTo(null);
        });
    }
}
