package protoPizzaa;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * Capa de efectos visuales alrededor de la pizza.
 * <p>
 * Dibuja un halo cuando el auto-clicker pulsa y muestra textos flotantes del
 * tipo "+X 🍕" al clickar. Este componente <b>no</b> modifica el modelo
 * ({@link Datos}); solo recibe notificaciones desde {@link Interfaz}.
 */

public class PizzaFXPane extends JLayeredPane {
	private static final long serialVersionUID = 1L;

	// --- HALO AUTOCLICK ---
	/** Alpha actual del halo (0..1). */
	private float haloAlpha = 0f;
	/** Alpha objetivo temporal cuando llega un pulso de auto-click. */
	private float haloTarget = 0f;
	/** Momento del último pulso de halo (ms) para aplicar un intervalo mínimo. */
	private long lastHaloPulseMs = 0L;
	/** Intervalo mínimo entre halos (ms). */
	private int haloMinIntervalMs = 90;
	/** Tiempo de subida del halo (ms). */
	private int haloRiseMs = 80;
	/** Tiempo de caída del halo (ms). */
	private int haloDecayMs = 180;

	// --- estilo del halo (editable desde Interfaz) ---
	/** Color del aro exterior del halo. */
	private Color haloOuterColor = new Color(255, 215, 0); // amarillo
	/** Color del aro interior del halo. */
	private Color haloInnerColor = new Color(255, 245, 200); // amarillo claro
	/** Grosor del aro exterior del halo (px). */
	private float haloOuterStroke = 8f; // ancho
	/** Grosor del aro interior del halo (px). */
	private float haloInnerStroke = 4f;
	/** Padding extra alrededor de la pizza para calcular el tamaño del halo. */
	private int haloPadding = 10; // más cerca = menor

	// --- FLOATING LABELS ---
	private static class FloatingFX {
		JLabel label;
		float x;
		float y;
		float vx;
		float vy;
		long startMs;
		int durationMs;
	}

	/** Textos flotantes activos (se actualizan en cada frame). */
	private final List<FloatingFX> floats = new ArrayList<>();
	/** Timer de animación (~60 FPS) que actualiza halo y floats. */
	private final Timer animTimer;

	/**
	 * Label base (pizza). Se centra en doLayout() y se usa como referencia para el
	 * halo.
	 */
	private final JLabel pizzaLabel;
	/** Fuente para los textos flotantes. */
	private final Font floatFont = new Font("Segoe UI Emoji", Font.BOLD, 14);

	/**
	 * Crea la capa FX y registra el {@code pizzaLabel} como elemento base. También
	 * arranca un {@link Timer} interno de ~60 FPS para animar halo y floats.
	 *
	 * @param pizzaLabel etiqueta que contiene la imagen de la pizza (se posiciona
	 *                   centrada)
	 */
	public PizzaFXPane(JLabel pizzaLabel) {
		setOpaque(false);
		setLayout(null);

		this.pizzaLabel = pizzaLabel;
		add(pizzaLabel, JLayeredPane.DEFAULT_LAYER);

		animTimer = new Timer(16, e -> step());
		animTimer.start();
	}

	/**
	 * Ajusta el intervalo mínimo entre pulsos de halo. Sirve para "capar"
	 * visualmente el halo si el auto-clicker va muy rápido.
	 *
	 * @param ms milisegundos mínimos entre halos (se fuerza a mínimo 16ms)
	 */
	public void setHaloMinIntervalMs(int ms) {
		haloMinIntervalMs = Math.max(16, ms);
	}

	/**
	 * Cambia los colores del halo (borde exterior e interior).
	 *
	 * @param outer color exterior (si es null, se mantiene)
	 * @param inner color interior (si es null, se mantiene)
	 */
	public void setHaloColors(Color outer, Color inner) {
		if (outer != null)
			this.haloOuterColor = outer;
		if (inner != null)
			this.haloInnerColor = inner;
	}

	/**
	 * Cambia el grosor del halo.
	 *
	 * @param outerStroke grosor del aro exterior (px)
	 * @param innerStroke grosor del aro interior (px)
	 */
	public void setHaloStrokes(float outerStroke, float innerStroke) {
		if (outerStroke > 0)
			this.haloOuterStroke = outerStroke;
		if (innerStroke > 0)
			this.haloInnerStroke = innerStroke;
	}

	/**
	 * Ajusta el padding del halo respecto a la pizza. Menor padding = halo más
	 * pegado a la pizza.
	 *
	 * @param paddingPx padding en píxeles (no puede ser negativo)
	 */
	public void setHaloPadding(int paddingPx) {
		this.haloPadding = Math.max(0, paddingPx);
	}

	/**
	 * Notifica que ha ocurrido un auto-click. Esto dispara un pulso de halo (sube
	 * alpha hacia un target) respetando el intervalo mínimo.
	 */
	public void notifyAutoClickTick() {
		long now = System.currentTimeMillis();
		if (now - lastHaloPulseMs >= haloMinIntervalMs) {
			lastHaloPulseMs = now;
			haloTarget = 0.55f;
		}
	}

	/**
	 * Crea un texto flotante del estilo "+X 🍕" cerca de la pizza.
	 *
	 * @param pizzasPorClick cantidad ganada en ese click (se formatea con 2
	 *                       decimales o entero si aplica)
	 */
	public void spawnClickFloat(double pizzasPorClick) {
		String txt = String.format("+%.2f 🍕", pizzasPorClick);
		if (Math.abs(pizzasPorClick - Math.rint(pizzasPorClick)) < 0.001) {
			txt = String.format("+%d 🍕", (long) Math.rint(pizzasPorClick));
		}

		FloatingFX fx = new FloatingFX();
		JLabel lbl = new JLabel(txt);
		lbl.setOpaque(false);
		lbl.setForeground(new Color(255, 215, 0));
		lbl.setFont(floatFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);

		Dimension pref = lbl.getPreferredSize();
		lbl.setSize(pref);

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
		fx.durationMs = 650;
		fx.label = lbl;

		add(lbl, JLayeredPane.PALETTE_LAYER);
		floats.add(fx);

		lbl.setLocation(Math.round(fx.x), Math.round(fx.y));
		lbl.repaint();
		repaint();
	}

	@Override
	public void doLayout() {
		super.doLayout();
		Dimension pref = pizzaLabel.getPreferredSize();
		int w = getWidth();
		int h = getHeight();

		int pw = pref != null ? pref.width : w;
		int ph = pref != null ? pref.height : h;

		int x = (w - pw) / 2;
		int y = (h - ph) / 2;
		pizzaLabel.setBounds(x, y, pw, ph);
	}

	private void step() {
		long now = System.currentTimeMillis();

		if (haloTarget > 0f) {
			float stepUp = 16f / Math.max(1, haloRiseMs);
			haloAlpha = Math.min(haloTarget, haloAlpha + stepUp);
			if (haloAlpha >= haloTarget - 0.001f) {
				haloTarget = 0f;
			}
		} else {
			float stepDown = 16f / Math.max(1, haloDecayMs);
			haloAlpha = Math.max(0f, haloAlpha - stepDown);
		}

		if (!floats.isEmpty()) {
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

				float alpha = 1f;
				if (t > 0.2f)
					alpha = 1f - (t - 0.2f) / 0.8f;
				alpha = Math.max(0f, Math.min(1f, alpha));

				fx.label.setLocation(Math.round(fx.x), Math.round(fx.y));
				fx.label.putClientProperty("alpha", alpha);
			}
		}

		repaint();
	}

	@Override
	protected void paintChildren(Graphics g) {
		super.paintChildren(g);

		if (floats.isEmpty())
			return;

		Graphics2D g2 = (Graphics2D) g.create();
		for (FloatingFX fx : floats) {
			Object a = fx.label.getClientProperty("alpha");
			float alpha = (a instanceof Float) ? (Float) a : 1f;

			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
			g2.translate(fx.label.getX(), fx.label.getY());
			fx.label.paint(g2);
			g2.translate(-fx.label.getX(), -fx.label.getY());
		}
		g2.dispose();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (haloAlpha <= 0f)
			return;

		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, haloAlpha));

		int pw = pizzaLabel.getWidth();
		int ph = pizzaLabel.getHeight();
		int px = pizzaLabel.getX();
		int py = pizzaLabel.getY();

		int size = Math.min(pw, ph) + (haloPadding * 2);
		int x = px + (pw - size) / 2;
		int y = py + (ph - size) / 2;

		// halo ancho, amarillo y más cerca
		g2.setColor(haloOuterColor);
		g2.setStroke(new java.awt.BasicStroke(haloOuterStroke));
		g2.drawOval(x, y, size, size);

		g2.setColor(haloInnerColor);
		g2.setStroke(new java.awt.BasicStroke(haloInnerStroke));
		int inset = Math.round(haloOuterStroke);
		g2.drawOval(x + inset, y + inset, size - inset * 2, size - inset * 2);

		g2.dispose();
	}
}
