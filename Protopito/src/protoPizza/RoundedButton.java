package protoPizza;

/**
 * Proyecto ProtoPizza.
 * Archivo: RoundedButton.java
 * Documentación JavaDoc generada para entender el código (núcleo + UI).
 */
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * Botón con estilo redondeado (custom painting).
 * Reutilizable para mejoras y acciones secundarias.
 */
public class RoundedButton extends JButton {
	// clase para generar boton con bordes redondeados

	private final int radius;

	public RoundedButton(String text, int radius) {
		super(text);
		this.radius = radius;

		setContentAreaFilled(false);
		setFocusPainted(false);
		setBorderPainted(false);
		setOpaque(false);

		// márgenes
		setMargin(new Insets(0, 12, 0, 12));
		setHorizontalAlignment(SwingConstants.CENTER);
		setVerticalAlignment(SwingConstants.CENTER);
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// fondo
		g2.setColor(getBackground());
		g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

		// borde suave
		g2.setColor(new Color(0, 0, 0, 50));
		g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

		// texto
		String text = getText();
		if (text != null && !text.isEmpty()) {
			g2.setFont(getFont());
			FontMetrics fm = g2.getFontMetrics();

			// padding para el texto
			Insets in = getInsets();
			int availableW = getWidth() - in.left - in.right;
			int availableH = getHeight() - in.top - in.bottom;

			int textWidth = fm.stringWidth(text);

			// centrado horizontal dentro del área útil
			int x = in.left + (availableW - textWidth) / 2;

			// centrado vertical por baseline dentro del área útil
			int y = in.top + (availableH - fm.getHeight()) / 2 + fm.getAscent();
			y += 3;

			g2.setColor(getForeground());
			g2.drawString(text, x, y);
		}

		g2.dispose();
	}
}
