package loadout.portraits;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PortraitCropDialog extends JDialog {
    private static final int PREVIEW_WIDTH = 860;
    private static final int PREVIEW_HEIGHT = 620;
    private static final int ZOOM_MIN = 100;
    private static final int ZOOM_MAX = 500;

    private final PreviewPanel preview;
    private final JSlider zoomSlider;
    private CropResult result;

    public static CropResult showDialog(BufferedImage source, PortraitFrameType defaultFrameType) {
        if (source == null) {
            return null;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            PortraitCropDialog dialog = new PortraitCropDialog(null, source, defaultFrameType);
            return dialog.showModal();
        }

        final CropResult[] out = new CropResult[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                PortraitCropDialog dialog = new PortraitCropDialog(null, source, defaultFrameType);
                out[0] = dialog.showModal();
            });
        } catch (InterruptedException | InvocationTargetException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        return out[0];
    }

    private PortraitCropDialog(Window owner, BufferedImage source, PortraitFrameType defaultFrameType) {
        super(owner, "Crop Portrait", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        PortraitFrameType initialFrame = defaultFrameType != null ? defaultFrameType : PortraitFrameType.ATTACK;
        zoomSlider = new JSlider(ZOOM_MIN, ZOOM_MAX, ZOOM_MIN);
        preview = new PreviewPanel(source, initialFrame, zoom -> {
            int value = (int) Math.round(zoom * 100.0);
            if (value != zoomSlider.getValue()) {
                zoomSlider.setValue(value);
            }
        });
        preview.setPreferredSize(new Dimension(PREVIEW_WIDTH, PREVIEW_HEIGHT));

        JPanel controls = new JPanel();
        controls.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JLabel frameLabel = new JLabel("Frame");
        JComboBox<PortraitFrameType> frameSelector = new JComboBox<>(PortraitFrameType.values());
        frameSelector.setSelectedItem(initialFrame);
        frameSelector.addActionListener(e -> preview.setFrameType((PortraitFrameType) frameSelector.getSelectedItem()));

        JLabel zoomLabel = new JLabel("Zoom");
        zoomSlider.setPreferredSize(new Dimension(180, 24));
        zoomSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                double zoom = zoomSlider.getValue() / 100.0;
                preview.setZoomRelative(zoom);
            }
        });

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> {
            preview.resetView();
            zoomSlider.setValue(ZOOM_MIN);
        });

        controls.add(frameLabel);
        controls.add(frameSelector);
        controls.add(zoomLabel);
        controls.add(zoomSlider);
        controls.add(resetButton);

        JPanel buttons = new JPanel();
        JButton confirm = new JButton("Confirm");
        JButton cancel = new JButton("Cancel");
        confirm.addActionListener(e -> {
            result = preview.getCropResult();
            dispose();
        });
        cancel.addActionListener(e -> {
            result = null;
            dispose();
        });
        buttons.add(confirm);
        buttons.add(cancel);

        setLayout(new BorderLayout());
        add(controls, BorderLayout.NORTH);
        add(preview, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private CropResult showModal() {
        // Ensure the dialog is brought to the front and focused on open.
        setAlwaysOnTop(true);
        setVisible(true);
        setAlwaysOnTop(false);
        return result;
    }

    public static final class CropResult {
        public final ImageUtil.CropRect cropRect;
        public final PortraitFrameType frameType;

        private CropResult(ImageUtil.CropRect cropRect, PortraitFrameType frameType) {
            this.cropRect = cropRect;
            this.frameType = frameType;
        }
    }

    private static final class PreviewPanel extends JPanel {
        private static final int FRAME_PADDING = 24;
        private static final double MAX_ZOOM = 5.0;

        private final BufferedImage source;
        private PortraitFrameType frameType;
        private final ZoomListener zoomListener;

        private Rectangle2D.Double frameRect = new Rectangle2D.Double();
        private double minScale = 1.0;
        private double scale = 1.0;
        private double imageX = 0.0;
        private double imageY = 0.0;
        private boolean initialized = false;
        private Point lastDrag;

        private PreviewPanel(BufferedImage source, PortraitFrameType frameType, ZoomListener zoomListener) {
            this.source = source;
            this.frameType = frameType;
            this.zoomListener = zoomListener;

            setBackground(new Color(32, 32, 32));
            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    lastDrag = e.getPoint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (lastDrag == null) {
                        return;
                    }
                    Point current = e.getPoint();
                    int dx = current.x - lastDrag.x;
                    int dy = current.y - lastDrag.y;
                    lastDrag = current;
                    imageX += dx;
                    imageY += dy;
                    clampImage();
                    repaint();
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    double zoom = getZoom();
                    double delta = -e.getPreciseWheelRotation() * 0.1;
                    zoom = clamp(zoom * (1.0 + delta), 1.0, MAX_ZOOM);
                    setZoom(zoom, e.getX(), e.getY());
                }
            };
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
            addMouseWheelListener(mouseAdapter);
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    updateFrameRect(false);
                }
            });
        }

        public void setFrameType(PortraitFrameType frameType) {
            this.frameType = frameType;
            updateFrameRect(true);
        }

        public void resetView() {
            scale = minScale;
            centerImage();
            notifyZoom();
            repaint();
        }

        public void setZoomRelative(double zoom) {
            setZoom(zoom, frameRect.getCenterX(), frameRect.getCenterY());
        }

        private void setZoom(double zoom, double pivotX, double pivotY) {
            zoom = clamp(zoom, 1.0, MAX_ZOOM);
            double newScale = minScale * zoom;
            if (Math.abs(newScale - scale) < 0.0001) {
                return;
            }

            // Keep the source pixel under the cursor stable while zooming.
            double srcX = (pivotX - imageX) / scale;
            double srcY = (pivotY - imageY) / scale;
            scale = newScale;
            imageX = pivotX - srcX * scale;
            imageY = pivotY - srcY * scale;
            clampImage();
            notifyZoom();
            repaint();
        }

        private double getZoom() {
            return scale / minScale;
        }

        public CropResult getCropResult() {
            if (!initialized) {
                updateFrameRect(true);
            }
            // Convert the frame rectangle (panel space) back into source pixel space.
            double cropX = (frameRect.getX() - imageX) / scale;
            double cropY = (frameRect.getY() - imageY) / scale;
            double cropW = frameRect.getWidth() / scale;
            double cropH = frameRect.getHeight() / scale;
            cropX = clamp(cropX, 0.0, source.getWidth() - cropW);
            cropY = clamp(cropY, 0.0, source.getHeight() - cropH);
            return new CropResult(new ImageUtil.CropRect(cropX, cropY, cropW, cropH), frameType);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!initialized) {
                updateFrameRect(true);
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            double drawW = source.getWidth() * scale;
            double drawH = source.getHeight() * scale;
            g2.drawImage(source, (int) Math.round(imageX), (int) Math.round(imageY),
                (int) Math.round(drawW), (int) Math.round(drawH), null);

            drawOverlay(g2);
            g2.dispose();
        }

        private void drawOverlay(Graphics2D g2) {
            Rectangle bounds = getBounds();
            g2.setColor(new Color(0, 0, 0, 120));
            int frameX = (int) Math.round(frameRect.getX());
            int frameY = (int) Math.round(frameRect.getY());
            int frameW = (int) Math.round(frameRect.getWidth());
            int frameH = (int) Math.round(frameRect.getHeight());
            g2.fillRect(0, 0, bounds.width, frameY);
            g2.fillRect(0, frameY, frameX, frameH);
            g2.fillRect(frameX + frameW, frameY, bounds.width - frameX - frameW, frameH);
            g2.fillRect(0, frameY + frameH, bounds.width, bounds.height - frameY - frameH);

            g2.setColor(new Color(255, 255, 255, 200));
            g2.setStroke(new BasicStroke(2.0f));
            g2.draw(frameRect);

            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
            g2.drawString(frameType.getDisplayName(), (int) frameRect.getX() + 6, (int) frameRect.getY() + 16);
        }

        private void updateFrameRect(boolean recenter) {
            int width = getWidth();
            int height = getHeight();
            if (width <= 0 || height <= 0) {
                return;
            }

            double aspect = frameType.getAspect();
            double availW = Math.max(1.0, width - FRAME_PADDING * 2.0);
            double availH = Math.max(1.0, height - FRAME_PADDING * 2.0);
            double frameW = availW;
            double frameH = frameW / aspect;
            if (frameH > availH) {
                frameH = availH;
                frameW = frameH * aspect;
            }
            double x = (width - frameW) / 2.0;
            double y = (height - frameH) / 2.0;
            frameRect.setRect(x, y, frameW, frameH);

            // Minimum zoom ensures the crop frame is always fully covered (no empty areas).
            minScale = Math.max(frameW / source.getWidth(), frameH / source.getHeight());
            if (!initialized || recenter) {
                scale = Math.max(scale, minScale);
                centerImage();
                initialized = true;
            } else {
                if (scale < minScale) {
                    scale = minScale;
                }
                clampImage();
            }
            notifyZoom();
            repaint();
        }

        private void centerImage() {
            double drawW = source.getWidth() * scale;
            double drawH = source.getHeight() * scale;
            imageX = frameRect.getCenterX() - drawW / 2.0;
            imageY = frameRect.getCenterY() - drawH / 2.0;
            clampImage();
        }

        private void clampImage() {
            double drawW = source.getWidth() * scale;
            double drawH = source.getHeight() * scale;
            // Clamp pan so the image continues to cover the frame rectangle in both axes.
            double minX = frameRect.getX() + frameRect.getWidth() - drawW;
            double maxX = frameRect.getX();
            double minY = frameRect.getY() + frameRect.getHeight() - drawH;
            double maxY = frameRect.getY();
            imageX = clamp(imageX, minX, maxX);
            imageY = clamp(imageY, minY, maxY);
        }

        private void notifyZoom() {
            if (zoomListener != null) {
                zoomListener.onZoomChanged(getZoom());
            }
        }

        private static double clamp(double value, double min, double max) {
            return Math.max(min, Math.min(max, value));
        }
    }

    private interface ZoomListener {
        void onZoomChanged(double zoom);
    }
}
