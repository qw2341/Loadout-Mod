package loadout.portraits;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public final class ImageUtil {
    private ImageUtil() {
    }

    public static BufferedImage readImage(File file) throws IOException {
        BufferedImage src = ImageIO.read(file);
        if (src == null) {
            throw new IOException("Unsupported image format.");
        }
        return src;
    }

    public static CropRect centerCropRect(BufferedImage src, double targetAspect) {
        double srcW = src.getWidth();
        double srcH = src.getHeight();
        double srcAspect = srcW / srcH;

        if (srcAspect >= targetAspect) {
            double cropW = srcH * targetAspect;
            double cropX = (srcW - cropW) / 2.0;
            return new CropRect(cropX, 0.0, cropW, srcH);
        }

        double cropH = srcW / targetAspect;
        double cropY = (srcH - cropH) / 2.0;
        return new CropRect(0.0, cropY, srcW, cropH);
    }

    public static BufferedImage cropAndScale(BufferedImage src, CropRect crop, int outW, int outH) {
        BufferedImage out = new BufferedImage(outW, outH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        applyQualityHints(g);
        // Map source pixels inside crop rect into the output buffer via an affine transform.
        double scaleX = outW / crop.width;
        double scaleY = outH / crop.height;
        AffineTransform tx = new AffineTransform();
        tx.scale(scaleX, scaleY);
        tx.translate(-crop.x, -crop.y);
        g.drawImage(src, tx, null);
        g.dispose();
        return out;
    }

    public static BufferedImage scale(BufferedImage src, int outW, int outH) {
        BufferedImage out = new BufferedImage(outW, outH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        applyQualityHints(g);
        g.drawImage(src, 0, 0, outW, outH, null);
        g.dispose();
        return out;
    }

    private static void applyQualityHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    }

    public static final class CropRect {
        public final double x;
        public final double y;
        public final double width;
        public final double height;

        public CropRect(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
