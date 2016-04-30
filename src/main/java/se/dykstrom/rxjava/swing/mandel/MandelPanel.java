package se.dykstrom.rxjava.swing.mandel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Displays the Mandelbrot fractal image in a {@code JPanel}. The image is
 * drawn pixel by pixel, using colors specified by color indices stored in a
 * {@code Line}. This class allocates an array of colors to use when
 * drawing pixels, and the {@code Line} color indices are modified to fit
 * the number of available colors.<p>
 *
 * The image is drawn in an off-screen buffer to improve performance. The
 * {@code paintComponent} method draws the entire image at once, which is a lot
 * faster when the panel is resized etc. Since Swing already provides double
 * buffering, this panel is in fact triple buffered.
 *
 * @author Johan Dykstrom
 */
class MandelPanel extends JPanel {

    /** The RGB colors to use when drawing the image. */
    private final int[] colors = new int[256 * 2];

    private final double factor = (double) (colors.length - 1) / Params.NUM_ITERATIONS;

    /** The off-screen image that is used to "triple buffer" this panel. */
    private BufferedImage image;

    /**
     * Creates a new {@code MandelPanel} and allocates a number of colors to use when displaying the image.
     */
    MandelPanel() {
        int index = 0;
        for (int red = 0; red < 256; red++) {
            colors[index++] = (new Color(red, 0, 0)).getRGB();
        }
        for (int green = 0; green < 256; green++) {
            colors[index++] = (new Color(255, green, 0)).getRGB();
        }
    }

    @Override
    public void paintComponent(Graphics graphics) {
        graphics.drawImage(image, 0, 0, null);
    }

    void clear() {
        image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        repaint(new Rectangle(0, 0, getWidth(), getHeight()));
    }

    void draw(Line line) {
        // Get the color index, that is the "escape time", for each pixel in
        // the image. Use the color index to select the RGB color for this
        // pixel from the array of available RGB colors.
        int[] escapeTimes = line.getEscapeTimes();
        for (int x = 0; x < escapeTimes.length; x++) {
            image.setRGB(x, line.getY(), colors[(int) (escapeTimes[x] * factor)]);
        }
    }

    void finish() {
        repaint(new Rectangle(0, 0, getWidth(), getHeight()));
    }
}
