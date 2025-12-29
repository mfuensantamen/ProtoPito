package protoPizza;

/**
 * Proyecto ProtoPizza.
 * Archivo: FlashPanel.java
 * Documentación JavaDoc generada para entender el código (núcleo + UI).
 */
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Panel que puede hacer un flash (destello) temporal cambiando su color/alpha.
 * Se usa como feedback visual sin bloquear el hilo de UI.
 */
public class FlashPanel extends JPanel {
// clasepara el feedback de la compra de las mejoras un flash verde

	private final Color porDefecto;
	/**
	 * Temporizador Swing usado para animaciones o ticks.
	 */
	private Timer flashTimer;

	/**
	 * Constructor de FlashPanel.
	 * Inicializa el estado interno de la clase.
	 * 
	 * Parámetros:
	 * @param porDefecto TODO
	 */
	public FlashPanel(Color porDefecto) {
		this.porDefecto = porDefecto;
		setBackground(porDefecto);
		setOpaque(true);
	}

	/**
	 * Método flash de FlashPanel.
	 * Ver descripción en el código/uso.
	 * 
	 * Parámetros:
	 * @param flashColor TODO
	 * @param ms TODO
	 */
	public void flash(Color flashColor, int ms) {

		setBackground(flashColor);

		flashTimer = new Timer(ms, ejecutar -> {
			setBackground(porDefecto);
			((Timer) ejecutar.getSource()).stop();
		});
		flashTimer.setRepeats(false);
		flashTimer.start();
	}
}
