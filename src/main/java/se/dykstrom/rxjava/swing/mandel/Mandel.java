package se.dykstrom.rxjava.swing.mandel;

import javax.swing.*;

public class Mandel {
    public static void main(String args[]) {
        int sections = (args.length > 0) ? Integer.parseInt(args[0]) : 1;
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame(sections);
            mainFrame.setVisible(true);
            mainFrame.setLocationRelativeTo(null);
        });
    }
}
