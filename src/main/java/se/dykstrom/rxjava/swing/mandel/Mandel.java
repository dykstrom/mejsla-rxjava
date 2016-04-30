package se.dykstrom.rxjava.swing.mandel;

import javax.swing.*;

public class Mandel {
    public static void main(String args[]) {
        int workers = (args.length > 0) ? Integer.parseInt(args[0]) : 1;
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame(workers);
            mainFrame.setVisible(true);
            mainFrame.setLocationRelativeTo(null);
        });
    }
}
