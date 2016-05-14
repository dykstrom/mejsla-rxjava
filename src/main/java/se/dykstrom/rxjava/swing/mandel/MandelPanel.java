package se.dykstrom.rxjava.swing.mandel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Displays the Mandelbrot fractal image in a panel. The image is drawn pixel by pixel,
 * using the RGB colors stored in the given {@link Line} objects.
 *
 * @author Johan Dykstrom
 */
class MandelPanel extends JComponent {

    /** The off-screen image buffer. */
    private BufferedImage image;

    @Override
    public void paintComponent(Graphics graphics) {
        graphics.drawImage(image, 0, 0, null);
    }

    /**
     * Clears the image and panel.
     */
    void clear() {
        image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        repaint(new Rectangle(0, 0, getWidth(), getHeight()));
    }

    /**
     * Draws one line in the image.
     */
    void draw(Line line) {
        int y = line.getY();
        int[] rgb = line.getRGB();
        for (int x = 0; x < rgb.length; x++) {
            image.setRGB(x, y, rgb[x]);
        }
    }

    /**
     * Finishes by repainting the panel when the image is complete.
     */
    void finish() {
        repaint(new Rectangle(0, 0, getWidth(), getHeight()));
    }
}
