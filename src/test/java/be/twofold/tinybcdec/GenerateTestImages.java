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

        renderImage("BC1 RGB", "4bbp", "no alpha", Transparency.NONE, target.resolve("bc1.png"));
        renderImage("BC1 RGBA", "4bbp", "1-bit alpha", Transparency.FULL, target.resolve("bc1a.png"));
        renderImage("BC2 RGBA", "8bbp", "4-bit alpha", Transparency.FULL, target.resolve("bc2.png"));
        renderImage("BC3 RGBA", "8bbp", "8-bit alpha", Transparency.FULL, target.resolve("bc3.png"));
        renderImage("BC6H_SF16 RGB", "8bbp", "no alpha", Transparency.NONE, target.resolve("bc6h_sf16.png"));
        renderImage("BC6H_UF16 RGB", "8bbp", "no alpha", Transparency.NONE, target.resolve("bc6h_uf16.png"));
        renderImage("BC7 RGBA", "8bbp", "8-bit alpha", Transparency.HALF, target.resolve("bc7.png"));
    }

    private static void renderImage(String s0, String s1, String s2, Transparency transparency, Path path) throws IOException {
        var image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);

        var value = transparency != Transparency.NONE ? 0 : 255;
        var c0 = new Color(128, 128, 255);
        var c1 = new Color(value, value, value, value);

        var x1 = transparency == Transparency.HALF ? WIDTH / 3 : 0;
        var y1 = transparency == Transparency.HALF ? HEIGHT / 3 : 0;
        var x2 = transparency == Transparency.HALF ? WIDTH * 2 / 3 : WIDTH;
        var y2 = transparency == Transparency.HALF ? HEIGHT * 2 / 3 : HEIGHT;
        var gradientPaint = new GradientPaint(x1, y1, c0, x2, y2, c1);

        var g2d = image.createGraphics();
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

    private enum Transparency {
        NONE, HALF, FULL
    }
}
