package protoPizzaa;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Punto de entrada y "orquestador" de alto nivel del juego ProtoPizza.
 * <p>
 * Responsabilidades:
 * <ul>
 * <li>Crear el {@link Datos} (estado del juego).</li>
 * <li>Definir el catálogo de {@link Mejora} (mejoras activas y pasivas).</li>
 * <li>Crear la {@link Interfaz} y arrancar el "motor" (un {@link Timer}).</li>
 * </ul>
 *
 * Nota: Aquí <b>NO</b> se dibuja nada; la UI vive en {@link Interfaz}. Aquí
 * solo se configuran datos iniciales y se lanza el bucle.
 */
public class ProtoPizzaAPP {

	/**
	 * Estado del juego (pizzas, pizzas/segundo, auto-clicker, etc.). Se comparte
	 * con la interfaz para renderizar y aplicar compras.
	 */
	private final Datos datos = new Datos();

	/**
	 * Mejoras pasivas (aumentan NPS = pizzas por segundo). Son estáticas para poder
	 * consultarse fácilmente desde otros puntos si hiciera falta.
	 */
	public static final List<Mejora> mejoras = new ArrayList<>();

	/**
	 * Mejoras activas (afectan al click: más por click o desbloquean/level del
	 * auto-clicker).
	 */
	public static final List<Mejora> mejorasClicker = new ArrayList<>();

	/**
	 * Constructor: define el catálogo de mejoras.
	 * <p>
	 * Cada {@link Mejora} recibe:
	 * <ol>
	 * <li>Nombre</li>
	 * <li>Coste inicial</li>
	 * <li>Multiplicador de coste tras comprar (incrCoste)</li>
	 * <li>Requisito de desbloqueo (umbral por record máximo)</li>
	 * <li>Acción (lambda) que aplica el efecto sobre {@link Datos}</li>
	 * </ol>
	 */
	public ProtoPizzaAPP() {

		// --- Mejoras "activas" (click) ---
		mejorasClicker.add(new Mejora("Experiencia del Chef", 18, 1.20, 0, d -> d.subirClicker(0.4)));
		mejorasClicker.add(new Mejora("Cuchillo bien afilado", 104, 1.21, 70, d -> d.subirClicker(0.9)));
		mejorasClicker.add(new Mejora("Manos de Maestro", 1_380, 1.23, 900, d -> d.subirClicker(4.5)));

		// Esta mejora sube el nivel del auto-clicker (y por tanto reduce el periodo
		// entre auto-clicks).
		mejorasClicker.add(new Mejora("Contratar Cocineros", 288, 1.30, 200, d -> d.subirAutoClicker()));

		// --- Mejoras "pasivas" (producción automática / NPS) ---
		mejoras.add(new Mejora("Tabla de Pizzería", 138, 1.21, 90, d -> d.subirNPS(0.5)));
		mejoras.add(new Mejora("Air Fryer", 575, 1.21, 380, d -> d.subirNPS(1.8)));
		mejoras.add(new Mejora("Horno de piedra", 1_955, 1.22, 1_300, d -> d.subirNPS(6)));
		mejoras.add(new Mejora("Horno doble", 6_325, 1.23, 4_200, d -> d.subirNPS(18)));
		mejoras.add(new Mejora("Horno industrial", 18_975, 1.24, 12_500, d -> d.subirNPS(55)));
		mejoras.add(new Mejora("Cinta automática", 55_200, 1.24, 36_000, d -> d.subirNPS(150)));
		mejoras.add(new Mejora("Amasadora automática", 155_250, 1.25, 100_000, d -> d.subirNPS(420)));
		mejoras.add(new Mejora("Fábrica de masa", 437_000, 1.25, 285_000, d -> d.subirNPS(1_200)));
		mejoras.add(new Mejora("Línea de producción", 1_207_500, 1.26, 800_000, d -> d.subirNPS(3_400)));
		mejoras.add(new Mejora("Central pizzera", 3_335_000, 1.26, 2_200_000, d -> d.subirNPS(9_500)));
		mejoras.add(new Mejora("Megafactoría", 9_430_000, 1.27, 6_200_000, d -> d.subirNPS(27_000)));
		mejoras.add(new Mejora("PizzaCorp", 26_450_000, 1.27, 17_000_000, d -> d.subirNPS(75_000)));
		mejoras.add(new Mejora("Red mundial de franquicias", 80_500_000, 1.29, 52_000_000, d -> d.subirNPS(220_000)));
		mejoras.add(new Mejora("Impresora 3D de pizzas", 241_500_000, 1.30, 160_000_000, d -> d.subirNPS(700_000)));
	}

	/**
	 * Entrada principal de Java.
	 * <p>
	 * Swing exige que la UI se cree en el Event Dispatch Thread (EDT). Por eso
	 * usamos {@link SwingUtilities#invokeLater(Runnable)}.
	 *
	 * @param args argumentos de línea de comandos (no usados)
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				new ProtoPizzaAPP().raiz();
			} catch (IOException e) {
				System.err.println("Fallo al inicializar la raíz de la app.");
				e.printStackTrace();
			}
		});
	}

	/**
	 * Crea la ventana principal ({@link Interfaz}), renderiza el primer frame y
	 * arranca el temporizador (motor del juego).
	 *
	 * @throws IOException si falla la carga de recursos gráficos (por ejemplo,
	 *                     pizza.png)
	 */
	private void raiz() throws IOException {
		Interfaz interfaz = new Interfaz(datos, mejoras, mejorasClicker);
		interfaz.render(); // primer render
		timer(interfaz); // arranca bucle
	}

	/**
	 * "Motor" del juego: un {@link Timer} que ejecuta ticks cada 15ms (~66 FPS).
	 * <p>
	 * En cada tick:
	 * <ol>
	 * <li>Se actualiza el estado ({@link Datos#reloj(double)})</li>
	 * <li>Se vuelve a pintar la UI ({@link Interfaz#render()})</li>
	 * </ol>
	 *
	 * @param interfaz instancia de la ventana principal a refrescar
	 */
	private void timer(Interfaz interfaz) {
		new Timer(15, e -> {
			// 0.015 = 15ms. Se usa como "delta time" para la producción pasiva.
			datos.reloj(0.015);
			interfaz.render();
		}).start();
	}
}
