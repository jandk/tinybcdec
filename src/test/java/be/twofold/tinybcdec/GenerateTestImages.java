package be.twofold.tinybcdec;

import javax.imageio.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;

public final class GenerateTestImages {
    public static final int WIDTH = 256;
    public static final int HEIGHT = 256;

    public static void main(String[] args) throws IOException {
        Path target = Path.of(args[0]);

        Color black = new Color(0, 0, 0, 0);
        Color grey = new Color(0, 0, 0, 255);
        Color blue = new Color(128, 128, 255, 255);
        Color white = new Color(255, 255, 255, 255);
        Color red = new Color(255, 64, 0, 255);
        Color green = new Color(64, 255, 0, 255);

        renderImage("BC1 RGB", "4bbp", "no alpha", blue, white, target.resolve("bc1.png"));
        renderImage("BC1 RGBA", "4bbp", "1-bit alpha", blue, black, target.resolve("bc1a.png"));
        renderImage("BC2 RGBA", "8bbp", "4-bit alpha", blue, black, target.resolve("bc2.png"));
        renderImage("BC3 RGBA", "8bbp", "8-bit alpha", blue, black, target.resolve("bc3.png"));
        renderImage("BC4U R", "4bpp", "no alpha", white, grey, target.resolve("bc4u.png"));
        renderImage("BC5U R", "8bpp", "no alpha", red, green, target.resolve("bc5u.png"));
        renderImage("BC6H_SF16 RGB", "8bbp", "no alpha", blue, white, target.resolve("bc6h_sf16.png"));
        renderImage("BC6H_UF16 RGB", "8bbp", "no alpha", blue, white, target.resolve("bc6h_uf16.png"));
        renderImage("BC7 RGBA", "8bbp", "8-bit alpha", blue, white, target.resolve("bc7.png"));
    }

    private static void renderImage(String s0, String s1, String s2, Color c0, Color c1, Path path) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);

        boolean half = s0.contains("BC7");
        int x1 = half ? WIDTH / 3 : 0;
        int y1 = half ? HEIGHT / 3 : 0;
        int x2 = half ? WIDTH * 2 / 3 : WIDTH;
        int y2 = half ? HEIGHT * 2 / 3 : HEIGHT;
        GradientPaint gradientPaint = new GradientPaint(x1, y1, c0, x2, y2, c1);

        Graphics2D g2d = image.createGraphics();
        primoGraphics(g2d);
        g2d.setPaint(gradientPaint);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw some text on it in courier new
        Font font = new Font("Courier New", Font.BOLD, 32);
        g2d.setFont(font);
        g2d.setColor(Color.BLACK);
        renderText(g2d, font, s0, s1, s2);
        g2d.dispose();

        ImageIO.write(image, "png", path.toFile());
    }

    private static void renderText(Graphics2D g2d, Font font, String s0, String s1, String s2) {
        FontMetrics metrics = g2d.getFontMetrics(font);
        int center = ((HEIGHT - metrics.getHeight()) / 2) + metrics.getAscent();

        g2d.setFont(font);
        int x0 = (WIDTH - metrics.stringWidth(s0)) / 2;
        int y0 = center - (metrics.getHeight() * 5 / 4);
        g2d.drawString(s0, x0, y0);

        int x1 = (WIDTH - metrics.stringWidth(s1)) / 2;
        g2d.drawString(s1, x1, center);

        int x2 = (WIDTH - metrics.stringWidth(s2)) / 2;
        int y2 = center + (metrics.getHeight() * 5 / 4);
        g2d.drawString(s2, x2, y2);
    }

    private static void primoGraphics(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
    }
}
