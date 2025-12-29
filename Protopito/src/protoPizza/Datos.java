package protoPizza;

/**
 * Proyecto ProtoPizza.
 * Archivo: Datos.java
 * Documentación JavaDoc generada para entender el código (núcleo + UI).
 */
/**
 * Modelo del juego: guarda el estado numérico y la lógica esencial (recurso,
 * NPS, autoclicker). No conoce la UI; se actualiza desde el motor (Timer) y
 * desde interacciones del usuario.
 */
public class Datos {
// clase donde se guardas los datos base y funcionalidades esenciales del juego principal

	private boolean autoClickerPulsado = false;
	/**
	 * Cantidad actual de pizzas (recurso principal).
	 */
	private double num = 1000000; // n de pizzas inicial
	/**
	 * Producción pasiva: pizzas por segundo.
	 */
	private double nps = 0; // n de pizzas/s iniciales
	/**
	 * Máximo histórico alcanzado; se usa para desbloqueos.
	 */
	private double recordMaximo = 0;
	/**
	 * Multiplicador/valor del click manual.
	 */
	private double clickIncremento = 1;
	// el clicker clickara automaticamente cada 1 segundo al inicio
	private double periodoInicial = 1.0;
	/**
	 * Periodo actual del autoclick (segundos entre autoclicks).
	 */
	private double periodoAutoClicker = periodoInicial;
	// cuenta el tiempo que pasa desde ultimo clik
	private double contadorAutoClicker = 0.0;
	/**
	 * Variable de estado usada por esta clase.
	 */
	private double decrementoNivel = 0.033;
	// autoclick no clickara mas rapido que esto
	private double periodoMinimo = 0.05;
	// nivel inicial de autoclick es decir desactivado
	private int nivelAutoClicker = 0;

	/**
	 * Activa el autoclicker o sube su nivel si ya está activo. Ajusta el periodo de
	 * autoclick para que sea más rápido con los niveles.
	 */
	public void subirAutoClicker() {
		nivelAutoClicker++;

		double calculado = periodoInicial - (nivelAutoClicker - 1) * decrementoNivel;

		periodoAutoClicker = calculado;
		if (periodoAutoClicker < periodoMinimo) {
			periodoAutoClicker = periodoMinimo;
		}
	}

	/**
	 * Aplica la ganancia por click manual. Incrementa el recurso principal en
	 * función del multiplicador de click.
	 */
	public void click() {
		num += clickIncremento + nps / 50;
		if (num > recordMaximo) {
			recordMaximo = num;
		}
	}

	// numeros actuales + numeros + numeros/s * 0.015 frecuencia de render
	// actualizado
	public void reloj(double diferenciaTiempo) {

		num += nps * diferenciaTiempo;
		if (num > recordMaximo) {
			recordMaximo = num;
		}
		if (nivelAutoClicker == 0) {
			return;
		}

		contadorAutoClicker += diferenciaTiempo;

		while (contadorAutoClicker >= periodoAutoClicker) {
			this.click();
			autoClickerPulsado = true;
			contadorAutoClicker -= periodoAutoClicker;
		}

	}

	public boolean autoClickerPulsado() {

		boolean pulsacion = autoClickerPulsado;
		autoClickerPulsado = false;
		return pulsacion;
	}

	public boolean verificarCompra(double coste) {
		return num >= coste;
	}

	public void subirClicker(double incremento) {
		clickIncremento += incremento;
	}

	public void subirNPS(double incremento) {
		nps += incremento;
	}

	public void gastar(double cantidad) {
		num -= cantidad;
	}

	// getters setters
	public double getMaximo() {
		return recordMaximo;
	}

	public double getClickIncremento() {
		return clickIncremento;
	}

	public double getNum() {
		return num;
	}

	public double getNps() {
		return nps;
	}

	public double getPeriodoAutoClicker() {
		return periodoAutoClicker;
	}

	public int getNivelAutoClicker() {
		return nivelAutoClicker;
	}

}
