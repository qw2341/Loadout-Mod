package loadout.portraits;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

    public static BufferedImage readResourceImage(String resourcePath) throws IOException {
        if (resourcePath == null || resourcePath.isEmpty()) {
            throw new IOException("Resource path missing.");
        }
        try (InputStream in = ImageUtil.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            BufferedImage src = ImageIO.read(in);
            if (src == null) {
                throw new IOException("Unsupported image format.");
            }
            return src;
        }
    }

    public static BufferedImage readResourceImageQuiet(String resourcePath) {
        try {
            return readResourceImage(resourcePath);
        } catch (IOException e) {
            return null;
        }
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
        BufferedImage mask = crop != null ? crop.getMaskForOutput(outW, outH) : null;
        if (mask != null) {
            out = applyMask(out, mask);
        }
        if (crop != null && crop.hasMask()) {
            return new MaskedImage(out, crop.smallMask, crop.largeMask);
        }
        return out;
    }

    public static BufferedImage scale(BufferedImage src, int outW, int outH) {
        BufferedImage out = new BufferedImage(outW, outH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        applyQualityHints(g);
        g.drawImage(src, 0, 0, outW, outH, null);
        g.dispose();
        BufferedImage mask = null;
        if (src instanceof MaskedImage) {
            mask = ((MaskedImage) src).getMaskForOutput(outW, outH);
        }
        if (mask != null) {
            out = applyMask(out, mask);
        }
        return out;
    }

    public static BufferedImage applyMask(BufferedImage src, BufferedImage mask) {
        if (src == null || mask == null) {
            return src;
        }
        int width = src.getWidth();
        int height = src.getHeight();
        if (width <= 0 || height <= 0) {
            return src;
        }

        BufferedImage scaledMask = mask;
        if (mask.getWidth() != width || mask.getHeight() != height) {
            scaledMask = scale(mask, width, height);
        }

        int[] srcPixels = src.getRGB(0, 0, width, height, null, 0, width);
        int[] maskPixels = scaledMask.getRGB(0, 0, width, height, null, 0, width);
        for (int i = 0; i < srcPixels.length; i++) {
            int srcPixel = srcPixels[i];
            int srcA = (srcPixel >>> 24) & 0xFF;
            if (srcA == 0) {
                continue;
            }
            int maskPixel = maskPixels[i];
            int maskA = (maskPixel >>> 24) & 0xFF;
            int r = (maskPixel >> 16) & 0xFF;
            int g = (maskPixel >> 8) & 0xFF;
            int b = maskPixel & 0xFF;
            int luma = (r + g + b) / 3;
            int coverage = (luma * maskA) / 255;
            int outA = (srcA * coverage) / 255;
            srcPixels[i] = (srcPixel & 0x00FFFFFF) | (outA << 24);
        }

        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        out.setRGB(0, 0, width, height, srcPixels, 0, width);
        return out;
    }

    private static void applyQualityHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    }

    private static final class MaskedImage extends BufferedImage {
        private final BufferedImage smallMask;
        private final BufferedImage largeMask;

        private MaskedImage(BufferedImage src, BufferedImage smallMask, BufferedImage largeMask) {
            super(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
            this.smallMask = smallMask;
            this.largeMask = largeMask;
            Graphics2D g = createGraphics();
            g.drawImage(src, 0, 0, null);
            g.dispose();
        }

        private BufferedImage getMaskForOutput(int outW, int outH) {
            BufferedImage best = null;
            int bestScore = Integer.MAX_VALUE;
            if (largeMask != null) {
                best = largeMask;
                bestScore = Math.abs(largeMask.getWidth() - outW) + Math.abs(largeMask.getHeight() - outH);
            }
            if (smallMask != null) {
                int score = Math.abs(smallMask.getWidth() - outW) + Math.abs(smallMask.getHeight() - outH);
                if (best == null || score < bestScore) {
                    best = smallMask;
                }
            }
            return best;
        }
    }

    public static final class CropRect {
        public final double x;
        public final double y;
        public final double width;
        public final double height;
        public final BufferedImage smallMask;
        public final BufferedImage largeMask;

        public CropRect(double x, double y, double width, double height) {
            this(x, y, width, height, null, null);
        }

        public CropRect(double x, double y, double width, double height, BufferedImage smallMask, BufferedImage largeMask) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.smallMask = smallMask;
            this.largeMask = largeMask;
        }

        public boolean hasMask() {
            return smallMask != null || largeMask != null;
        }

        public BufferedImage getMaskForOutput(int outW, int outH) {
            BufferedImage best = null;
            int bestScore = Integer.MAX_VALUE;
            if (largeMask != null) {
                best = largeMask;
                bestScore = Math.abs(largeMask.getWidth() - outW) + Math.abs(largeMask.getHeight() - outH);
            }
            if (smallMask != null) {
                int score = Math.abs(smallMask.getWidth() - outW) + Math.abs(smallMask.getHeight() - outH);
                if (best == null || score < bestScore) {
                    best = smallMask;
                }
            }
            return best;
        }
    }
}
