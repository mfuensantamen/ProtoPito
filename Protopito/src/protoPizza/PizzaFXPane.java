package protoPizza;

/**
 * Proyecto ProtoPizza.
 * Archivo: PizzaFXPane.java
 */
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * Panel de efectos alrededor de la pizza (halos, textos flotantes, feedback de
 * autoclick). Solo efectos visuales; no modifica el modelo directamente.
 */
public class PizzaFXPane extends JLayeredPane {

	private final JLabel pizzaLabel;

	// --- ICONO PARA FLOATS (reemplaza 🍕) ---
	private ImageIcon sliceIcon;
	private int sliceIconSize = 22;

	// --- HALO AUTOCLICK ---
	private float haloAlpha = 0f;
	private float haloTarget = 0f;
	private long lastHaloPulseMs = 0;
	private int haloMinIntervalMs = 160;

	private Color haloOuterColor = new Color(255, 215, 0);
	private Color haloInnerColor = new Color(255, 245, 200);
	private float haloOuterStroke = 8f;
	private float haloInnerStroke = 4f;
	private int haloPadding = 10;
	// cache (evita new BasicStroke / new Color cada frame)
	private transient BasicStroke haloOuterStrokeObj = new BasicStroke(haloOuterStroke);
	private transient BasicStroke haloInnerStrokeObj = new BasicStroke(haloInnerStroke);
	private static final Color[] GOLD_ALPHA = buildGoldAlpha();

	private static Color[] buildGoldAlpha() {
		Color[] alpha = new Color[256];
		for (int i = 0; i < 256; i++)
			alpha[i] = new Color(255, 192, 0, i);
		return alpha;
	}

	// --- FLOATING LABELS ---
	private static class FloatingFX {
		JLabel label;
		float x, y;
		float vx, vy;
		long startMs;
		long durationMs;
		int lastAlpha = -1;
	}

	private final List<FloatingFX> floats = new ArrayList<>();
	private final Timer animTimer;

	public PizzaFXPane(JLabel pizzaLabel) {
		setOpaque(false);
		setLayout(null);
		this.pizzaLabel = pizzaLabel;
		add(pizzaLabel, JLayeredPane.DEFAULT_LAYER);

		setSliceIcon("/img/pizza_slice.png", 22);
		animTimer = new Timer(16, e -> step());
		animTimer.start();
	}

	public void setSliceIcon(String resourcePath, int sizePx) {
		this.sliceIconSize = Math.max(12, sizePx);
		this.sliceIcon = loadIcon(resourcePath, this.sliceIconSize);
		if (this.sliceIcon == null) {
			System.err.println("❌ No se pudo cargar " + resourcePath + " (floats usarán emoji 🍕)");
		}
	}

	private ImageIcon loadIcon(String resourcePath, int sizePx) {
		if (resourcePath == null || resourcePath.isBlank())
			return null;

		String path = resourcePath.startsWith("/") ? resourcePath : ("/" + resourcePath);

		try {
			var url = getClass().getResource(path);
			if (url == null) {
				System.err.println("❌ Recurso no encontrado: " + path);
				return null;
			}
			BufferedImage img = ImageIO.read(url);
			Image scaled = img.getScaledInstance(sizePx, sizePx, Image.SCALE_SMOOTH);
			return new ImageIcon(scaled);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setHaloColors(Color outer, Color inner) {
		if (outer != null)
			this.haloOuterColor = outer;
		if (inner != null)
			this.haloInnerColor = inner;
	}

	public void setHaloStrokes(float outerStroke, float innerStroke) {
		if (outerStroke > 0)
			this.haloOuterStroke = outerStroke;
		if (innerStroke > 0)
			this.haloInnerStroke = innerStroke;

		// refrescamos cache
		haloOuterStrokeObj = new BasicStroke(this.haloOuterStroke);
		haloInnerStrokeObj = new BasicStroke(this.haloInnerStroke);
	}

	public void setHaloPadding(int paddingPx) {
		this.haloPadding = Math.max(0, paddingPx);
	}

	public void efectoHaloTick() {
		long now = System.currentTimeMillis();
		if (now - lastHaloPulseMs >= haloMinIntervalMs) {
			lastHaloPulseMs = now;
			haloTarget = 0.55f;
		}
	}

	public void spawnClickFloat(double pizzasPorClick, String texto) {

		FloatingFX fx = new FloatingFX();

		JLabel lbl = null;
		if (sliceIcon != null) {
			lbl = new JLabel(texto, sliceIcon, SwingConstants.CENTER);
			lbl.setHorizontalTextPosition(SwingConstants.RIGHT);
			lbl.setVerticalTextPosition(SwingConstants.CENTER);
			lbl.setIconTextGap(6);
		}

		lbl.setOpaque(false);
		lbl.setForeground(new Color(255, 215, 0));
		lbl.setFont(Interfaz.fuente);

		lbl.setHorizontalAlignment(SwingConstants.LEFT); // mejor para icon+texto
		lbl.setIconTextGap(6);

		// ancho mínimo para que no recorte
		int minW = 90;
		int minH = 26;

		Dimension pref = lbl.getPreferredSize();
		int w = Math.max(pref.width, minW);
		int h = Math.max(pref.height, minH);

		lbl.setSize(w, h);

		int pizzaW = pizzaLabel.getWidth();
		int pizzaH = pizzaLabel.getHeight();
		int px = pizzaLabel.getX();
		int py = pizzaLabel.getY();

		if (pizzaW <= 0 || pizzaH <= 0) {
			Dimension p = pizzaLabel.getPreferredSize();
			pizzaW = p.width;
			pizzaH = p.height;
		}

		int centerX = px + pizzaW / 2;
		int centerY = py + pizzaH / 2;

		boolean right = ThreadLocalRandom.current().nextBoolean();
		int sideOffset = ThreadLocalRandom.current().nextInt(30, 60) * (right ? 1 : -1);
		int upOffset = ThreadLocalRandom.current().nextInt(-10, 20);

		fx.x = centerX + sideOffset - pref.width / 2f;
		fx.y = centerY + upOffset - pref.height / 2f;

		fx.vy = -1.2f - ThreadLocalRandom.current().nextFloat() * 0.8f;
		fx.vx = (right ? 0.5f : -0.5f) + (ThreadLocalRandom.current().nextFloat() - 0.5f) * 0.6f;

		fx.startMs = System.currentTimeMillis();
		fx.durationMs = 1000;
		fx.label = lbl;

		add(lbl, JLayeredPane.PALETTE_LAYER);
		floats.add(fx);

		lbl.setLocation(Math.round(fx.x), Math.round(fx.y));
		lbl.repaint();
		repaint();
	}

	private void step() {
		long now = System.currentTimeMillis();

		// halo más rápido y más "snap"
		haloAlpha += (haloTarget - haloAlpha) * 0.35f; // antes 0.18
		haloTarget *= 0.65f; // antes 0.94 (decay más rápido)
		if (haloAlpha < 0.02f)
			haloAlpha = 0f;

		// move floats
		Iterator<FloatingFX> it = floats.iterator();
		while (it.hasNext()) {
			FloatingFX fx = it.next();
			float t = (now - fx.startMs) / (float) fx.durationMs;
			if (t >= 1f) {
				remove(fx.label);
				it.remove();
				continue;
			}

			fx.x += fx.vx;
			fx.y += fx.vy;

			// fade out
			float alpha = 1f - t;
			int a = Math.max(90, Math.min(255, Math.round(alpha * 255)));
			if (a != fx.lastAlpha) {
				fx.lastAlpha = a;
				fx.label.setForeground(GOLD_ALPHA[a]);
			}

			fx.label.setLocation(Math.round(fx.x), Math.round(fx.y));
		}

		repaint();
	}

	@Override
	public void doLayout() {
		super.doLayout();
		if (pizzaLabel == null)
			return;

		// si aún no tiene tamaño (0), usa el preferred del pane
		int paneW = getWidth();
		int paneH = getHeight();
		if (paneW <= 0 || paneH <= 0) {
			Dimension p = getPreferredSize();
			paneW = (p != null) ? p.width : 260;
			paneH = (p != null) ? p.height : 260;
		}

		Dimension pref = pizzaLabel.getPreferredSize();
		int w = (pref != null) ? pref.width : 200;
		int h = (pref != null) ? pref.height : 200;

		int x = (paneW - w) / 2;
		int y = (paneH - h) / 2;

		pizzaLabel.setBounds(x, y, w, h);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (haloAlpha <= 0f)
			return;

		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, haloAlpha)));

		int pizzaW = pizzaLabel.getWidth();
		int pizzaH = pizzaLabel.getHeight();
		int px = pizzaLabel.getX();
		int py = pizzaLabel.getY();

		if (pizzaW <= 0 || pizzaH <= 0) {
			Dimension p = pizzaLabel.getPreferredSize();
			pizzaW = p.width;
			pizzaH = p.height;
		}

		int size = Math.max(pizzaW, pizzaH) + haloPadding * 2;
		int x = px + (pizzaW - size) / 2;
		int y = py + (pizzaH - size) / 2;

		g2.setColor(haloOuterColor);
		g2.setStroke(haloOuterStrokeObj);
		g2.drawOval(x, y, size, size);

		g2.setColor(haloInnerColor);
		g2.setStroke(haloInnerStrokeObj);
		int inset = Math.round(haloOuterStroke);
		g2.drawOval(x + inset, y + inset, size - inset * 2, size - inset * 2);

		g2.dispose();
	}
}