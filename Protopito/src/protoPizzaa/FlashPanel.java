package protoPizzaa;

import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Panel con efecto de "flash" de color temporal.
 * <p>
 * Se usa como feedback de compra (por ejemplo, flash verde al comprar una
 * mejora).
 */
public class FlashPanel extends JPanel {

	/** Color base del panel (al que vuelve tras el flash). */
	private final Color porDefecto;

	/** Timer de un solo disparo para revertir el color tras el flash. */
	private Timer flashTimer;

	/**
	 * Crea el panel con un color por defecto.
	 *
	 * @param porDefecto color base
	 */
	public FlashPanel(Color porDefecto) {
		this.porDefecto = porDefecto;
		setBackground(porDefecto);
		setOpaque(true);
	}

	/**
	 * Cambia el background del panel durante un tiempo y luego lo restaura.
	 *
	 * @param flashColor color temporal
	 * @param ms         duración en milisegundos
	 */
	public void flash(Color flashColor, int ms) {
		setBackground(flashColor);

		flashTimer = new Timer(ms, e -> {
			setBackground(porDefecto);
			((Timer) e.getSource()).stop();
		});
		flashTimer.setRepeats(false);
		flashTimer.start();
	}
}
