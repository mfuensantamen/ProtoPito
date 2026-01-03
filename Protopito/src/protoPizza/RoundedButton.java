package protoPizza;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * Botón con estilo redondeado (custom painting). Reutilizable para mejoras y
 * acciones secundarias.
 */
public class RoundedButton extends JButton {
	// clase para generar boton con bordes redondeados

	private final int radio;

	public RoundedButton(String texto, int radio) {
		super(texto);
		this.radio = radio;

		setContentAreaFilled(false);
		setFocusPainted(false);
		setBorderPainted(false);
		setOpaque(false);

		// margenes
		setMargin(new Insets(0, 12, 0, 12));
		setHorizontalAlignment(SwingConstants.CENTER);
		setVerticalAlignment(SwingConstants.CENTER);
	}

	@Override
	protected void paintComponent(Graphics graf) {
		Graphics2D grafismo = (Graphics2D) graf.create();
		grafismo.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// fondo
		grafismo.setColor(getBackground());
		grafismo.fillRoundRect(0, 0, getWidth(), getHeight(), radio, radio);

		// borde suave
		grafismo.setColor(new Color(0, 0, 0, 50));
		grafismo.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radio, radio);

		// texto
		String texto = getText();
		if (texto != null && !texto.isEmpty()) {
			grafismo.setFont(getFont());
			FontMetrics fm = grafismo.getFontMetrics();// ?????

			// padding para el texto
			Insets insercion = getInsets();
			int anchoDsisponible = getWidth() - insercion.left - insercion.right;
			int altoDisponible = getHeight() - insercion.top - insercion.bottom;

			int anchuraTexto = fm.stringWidth(texto);

			// para centrar el texto mejor en el boton
			int x = insercion.left + (anchoDsisponible - anchuraTexto) / 2;
			int y = insercion.top + (altoDisponible - fm.getHeight()) / 2 + fm.getAscent();
			y += 3;

			grafismo.setColor(getForeground());
			grafismo.drawString(texto, x, y);
		}

		grafismo.dispose();
	}
}
