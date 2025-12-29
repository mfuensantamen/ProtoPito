package protoPizzaa;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * Botón simple con bordes redondeados.
 * <p>
 * Se usa para botones de compra de mejoras (UI secundaria), donde queremos un
 * estilo consistente. Pinta manualmente fondo, borde y texto centrado.
 */
public class RoundedButton extends JButton {

	/** Radio de redondeo (px). */
	private final int radius;

	/**
	 * Crea un botón redondeado.
	 *
	 * @param text   texto del botón
	 * @param radius radio de redondeo en píxeles
	 */
	public RoundedButton(String text, int radius) {
		super(text);
		this.radius = radius;

		setContentAreaFilled(false);
		setFocusPainted(false);
		setBorderPainted(false);
		setOpaque(false);

		setMargin(new Insets(0, 12, 0, 12));
		setHorizontalAlignment(SwingConstants.CENTER);
		setVerticalAlignment(SwingConstants.CENTER);
	}

	/**
	 * Pinta el botón con antialiasing.
	 * <p>
	 * 1) Fondo redondeado con el background actual<br/>
	 * 2) Borde semitransparente<br/>
	 * 3) Texto centrado (calculando baseline con métricas de fuente)
	 *
	 * @param g contexto gráfico
	 */
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setColor(getBackground());
		g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

		g2.setColor(new Color(0, 0, 0, 50));
		g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

		String text = getText();
		if (text != null && !text.isEmpty()) {
			g2.setFont(getFont());
			FontMetrics fm = g2.getFontMetrics();

			Insets in = getInsets();
			int availableW = getWidth() - in.left - in.right;
			int availableH = getHeight() - in.top - in.bottom;

			int textWidth = fm.stringWidth(text);

			int x = in.left + (availableW - textWidth) / 2;
			int y = in.top + (availableH - fm.getHeight()) / 2 + fm.getAscent();
			y += 3; // ajuste visual

			g2.setColor(getForeground());
			g2.drawString(text, x, y);
		}

		g2.dispose();
	}
}
