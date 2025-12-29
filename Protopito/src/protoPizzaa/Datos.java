package protoPizzaa;

/**
 * Modelo de datos del juego (estado).
 * <p>
 * Esta clase guarda los números principales y ofrece operaciones del "núcleo":
 * <ul>
 * <li>Click manual (ganancia inmediata).</li>
 * <li>Producción pasiva (pizzas por segundo, NPS).</li>
 * <li>Auto-clicker (clicks automáticos según un periodo).</li>
 * <li>Compras: verificar si se puede comprar y gastar.</li>
 * </ul>
 *
 * Importante: esta clase <b>no</b> tiene componentes Swing; no dibuja nada.
 */
public class Datos {

	/**
	 * Flag interno: se pone a true cuando el auto-clicker ha hecho al menos un
	 * click en este tick.
	 */
	private boolean autoClickerPulsado = false;

	/** Pizzas actuales. Es el recurso principal del juego. */
	private double num = 900; // pizzas iniciales

	/** NPS = pizzas por segundo (producción pasiva total). */
	private double nps = 0;

	/**
	 * Récord máximo alcanzado (máximo histórico). Se usa para desbloquear mejoras
	 * aunque luego gastes pizzas.
	 */
	private double recordMaximo = 0;

	/** Incremento base del click manual (pizzas por click). */
	private double clickIncremento = 1;

	/** Periodo inicial del auto-clicker (segundos entre clicks) en nivel 1. */
	private final double periodoInicial = 1.0;

	/** Periodo actual del auto-clicker (puede reducirse con niveles). */
	private double periodoAutoClicker = periodoInicial;

	/** Acumulador de tiempo desde el último auto-click (en segundos). */
	private double contadorAutoClicker = 0.0;

	/**
	 * Cuánto se reduce el periodo por cada nivel adicional del auto-clicker.
	 * Ejemplo: 1.0s - (nivel-1)*0.033.
	 */
	private final double decrementoNivel = 0.033;

	/** Límite inferior del periodo del auto-clicker para que no vaya "loco". */
	private final double periodoMinimo = 0.05;

	/** Nivel del auto-clicker. 0 = desactivado. */
	private int nivelAutoClicker = 0;

	/**
	 * Sube el nivel del auto-clicker y recalcula su periodo.
	 * <p>
	 * Regla:
	 * <ul>
	 * <li>Nivel 0: desactivado</li>
	 * <li>Nivel 1: periodo = periodoInicial</li>
	 * <li>Niveles siguientes: se reduce el periodo según
	 * {@link #decrementoNivel}</li>
	 * <li>Se aplica un tope mínimo {@link #periodoMinimo}</li>
	 * </ul>
	 */
	public void subirAutoClicker() {
		nivelAutoClicker++;

		// Cálculo del periodo en función del nivel.
		double calculado = periodoInicial - (nivelAutoClicker - 1) * decrementoNivel;

		periodoAutoClicker = calculado;
		if (periodoAutoClicker < periodoMinimo) {
			periodoAutoClicker = periodoMinimo;
		}
	}

	/**
	 * Click manual del jugador.
	 * <p>
	 * La ganancia del click depende de:
	 * <ul>
	 * <li>{@link #clickIncremento} (base)</li>
	 * <li>Un pequeño bonus ligado a NPS: {@code nps / 50}</li>
	 * </ul>
	 * Esto hace que el click escale ligeramente con la producción pasiva.
	 */
	public void click() {
		num += clickIncremento + nps / 50;
		if (num > recordMaximo) {
			recordMaximo = num;
		}
	}

	/**
	 * Tick principal del juego.
	 * <p>
	 * Se llama periódicamente desde el motor (Timer). Recibe el tiempo transcurrido
	 * (en segundos) y:
	 * <ol>
	 * <li>Aplica producción pasiva: {@code num += nps * diferenciaTiempo}</li>
	 * <li>Actualiza el récord máximo si procede</li>
	 * <li>Ejecuta auto-clicker si está activado (nivel > 0)</li>
	 * </ol>
	 *
	 * @param diferenciaTiempo segundos transcurridos desde el último tick (delta
	 *                         time)
	 */
	public void reloj(double diferenciaTiempo) {

		// 1) Producción pasiva proporcional al tiempo real transcurrido.
		num += nps * diferenciaTiempo;

		// 2) Récord histórico (para desbloqueos).
		if (num > recordMaximo) {
			recordMaximo = num;
		}

		// 3) Si el auto-clicker está apagado, no hacemos más.
		if (nivelAutoClicker == 0) {
			return;
		}

		// 4) Acumulamos tiempo para decidir si toca auto-click.
		contadorAutoClicker += diferenciaTiempo;

		// 5) Mientras haya acumulado suficiente tiempo, hacemos clicks automáticos.
		while (contadorAutoClicker >= periodoAutoClicker) {
			this.click();
			autoClickerPulsado = true; // lo usa la UI para efectos (halo/flash)
			contadorAutoClicker -= periodoAutoClicker;
		}
	}

	/**
	 * Indica si el auto-clicker ha clicado desde la última vez que se consultó.
	 * <p>
	 * Es un "flag de un solo uso": al leerlo se resetea a false. Esto permite a la
	 * UI disparar efectos visuales una vez por tick sin duplicarlos.
	 *
	 * @return true si hubo al menos un auto-click desde la última consulta; si no,
	 *         false
	 */
	public boolean autoClickerPulsado() {
		boolean pulsacion = autoClickerPulsado;
		autoClickerPulsado = false;
		return pulsacion;
	}

	/**
	 * Comprueba si hay suficientes pizzas para pagar un coste.
	 *
	 * @param coste coste a comprobar
	 * @return true si {@link #num} es mayor o igual que el coste; si no, false
	 */
	public boolean verificarCompra(double coste) {
		return num >= coste;
	}

	/**
	 * Aumenta el incremento base del click manual.
	 *
	 * @param incremento cantidad a sumar a {@link #clickIncremento}
	 */
	public void subirClicker(double incremento) {
		clickIncremento += incremento;
	}

	/**
	 * Aumenta la producción pasiva (NPS).
	 *
	 * @param incremento cantidad a sumar a {@link #nps}
	 */
	public void subirNPS(double incremento) {
		nps += incremento;
	}

	/**
	 * Resta pizzas actuales (gasto).
	 * <p>
	 * Nota: este método no valida si te quedas en negativo; esa validación debe
	 * hacerse antes (por ejemplo con {@link #verificarCompra(double)}).
	 *
	 * @param cantidad cantidad a restar
	 */
	public void gastar(double cantidad) {
		num -= cantidad;
	}

	// --------------------
	// Getters / setters
	// --------------------

	/** @return récord máximo histórico alcanzado */
	public double getMaximo() {
		return recordMaximo;
	}

	/** @param click nuevo incremento base del click manual */
	public void setClickIncremento(double click) {
		this.clickIncremento = click;
	}

	/** @return incremento base del click manual */
	public double getClickIncremento() {
		return clickIncremento;
	}

	/** @return pizzas actuales */
	public double getNum() {
		return num;
	}

	/** @param num nuevo valor absoluto de pizzas (útil para debug/cheats/tests) */
	public void setNum(double num) {
		this.num = num;
	}

	/** @return pizzas por segundo (NPS) */
	public double getNps() {
		return nps;
	}

	/** @param nps nuevo valor de pizzas por segundo */
	public void setNps(double nps) {
		this.nps = nps;
	}

	/** @return periodo actual del auto-clicker (segundos entre clicks) */
	public double getPeriodoAutoClicker() {
		return periodoAutoClicker;
	}

	/** @param periodoAutoClicker establece el periodo actual del auto-clicker */
	public void setPeriodoAutoClicker(double periodoAutoClicker) {
		this.periodoAutoClicker = periodoAutoClicker;
	}

	/** @return nivel del auto-clicker (0 = off) */
	public int getNivelAutoClicker() {
		return nivelAutoClicker;
	}

	/** @param nivelAutoClicker establece el nivel del auto-clicker */
	public void setNivelAutoClicker(int nivelAutoClicker) {
		this.nivelAutoClicker = nivelAutoClicker;
	}
}
