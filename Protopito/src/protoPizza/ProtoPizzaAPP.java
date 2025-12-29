package protoPizza;

/**
 * Proyecto ProtoPizza.
 * Archivo: ProtoPizzaAPP.java
 * Documentación JavaDoc generada para entender el código (núcleo + UI).
 */
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Clase principal de arranque/orquestación del juego. Define el modelo (Datos),
 * construye la lista de mejoras y crea la Interfaz. También arranca el Timer
 * que actúa como motor (tick) del juego.
 */
public class ProtoPizzaAPP {
// clase para la ejecucion de la app final, controla el timer interno el "motor" y parametros de las mejoras

// TODO
	// * revertir texto coste y boton
	// * añadir clicker
	// * añadir clicker automatico con mejoras
	// * añadir boton grande imagen de pizza
	// implementar un "timer" que lleva el tiempo de partida
	// implementar contador de clicks por segundo en un lado de la pizza y si se
	// pasa de un umbral cambiar fondo de panel gris a otro color
	// probar a poner imagenes de emjojis en vez de strings y colocar el nombre de
	// la mejora junto al icono a la derecha y dentrar todo
	// elemento gatcha
	// objetos consumibles tiempo limitado

	private Datos datos = new Datos();
	public static List<Mejora> mejoras = new ArrayList<>();
	public static List<Mejora> mejorasClicker = new ArrayList<>();

	/**
	 * Constructor de ProtoPizzaAPP. Inicializa el estado interno de la clase.
	 */
	public ProtoPizzaAPP() {

		// mejoras activas clickando
		// nombre / coste / incrementoCoste / umbralDesbloqueo / accion
		mejorasClicker.add(
				new Mejora("Contratar Cocineros", 216, 1.225, 200, a -> datos.subirAutoClicker(), "/img/cook.png"));
		mejorasClicker
				.add(new Mejora("Experiencia del Chef", 13.5, 1.15, 0, a -> datos.subirClicker(0.5), "/img/brain.png"));
		mejorasClicker.add(
				new Mejora("Premium Pizza-Cutter", 78, 1.1575, 70, a -> datos.subirClicker(1.125), "/img/knife.png"));
		mejorasClicker.add(
				new Mejora("Manos de Maestro", 1_035, 1.1725, 900, a -> datos.subirClicker(5.625), "/img/hand.png"));

		// mejoras pasivas autoclicker
		// nombre / coste / incrementoCoste / umbralDesbloqueo / accion
		mejoras.add(
				new Mejora("Tabla de Pizzería", 103.5, 1.1575, 90, accion -> datos.subirNPS(0.625), "/img/wood.png"));
		mejoras.add(new Mejora("Air Fryer", 431.25, 1.1575, 380, accion -> datos.subirNPS(2.25), "/img/fire.png"));
		mejoras.add(
				new Mejora("Horno de piedra", 1_466.25, 1.165, 1_300, accion -> datos.subirNPS(7.5), "/img/fire.png"));
		mejoras.add(
				new Mejora("Horno doble", 4_743.75, 1.1725, 4_200, accion -> datos.subirNPS(22.5), "/img/fire.png"));
		mejoras.add(new Mejora("Horno industrial", 14_231.25, 1.18, 12_500, accion -> datos.subirNPS(68.75),
				"/img/fire.png"));
		mejoras.add(new Mejora("Cinta automática", 41_400, 1.18, 36_000, accion -> datos.subirNPS(187.5),
				"/img/railway.png"));
		mejoras.add(new Mejora("Amasadora automática", 116_437.5, 1.1875, 100_000, accion -> datos.subirNPS(525),
				"/img/crane.png"));
		mejoras.add(new Mejora("Fábrica de masa", 327_750, 1.1875, 285_000, accion -> datos.subirNPS(1_500),
				"/img/factory.png"));
		mejoras.add(new Mejora("Línea de producción", 905_625, 1.195, 800_000, accion -> datos.subirNPS(4_250),
				"/img/factory.png"));
		mejoras.add(new Mejora("Central pizzera", 2_501_250, 1.195, 2_200_000, accion -> datos.subirNPS(11_875),
				"/img/office.png"));
		mejoras.add(new Mejora("Megafactoría", 7_072_500, 1.2025, 6_200_000, accion -> datos.subirNPS(33_750),
				"/img/factory.png"));
		mejoras.add(new Mejora("PizzaCorp", 19_837_500, 1.2025, 17_000_000, accion -> datos.subirNPS(93_750),
				"/img/briefcase.png"));
		mejoras.add(new Mejora("Miltinacional PizzaCorp", 60_375_000, 1.2175, 52_000_000,
				accion -> datos.subirNPS(275_000), "/img/earth_africa.png"));
		mejoras.add(new Mejora("Impresora 3D de pizzas", 181_125_000, 1.225, 160_000_000,
				accion -> datos.subirNPS(875_000), "/img/printer.png"));

	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				new ProtoPizzaAPP().raiz();
			} catch (IOException e) {
				System.err.println("fallo en raiz");
				e.printStackTrace();
			}
		});
	}

	/**
	 * Construye y muestra la interfaz principal. Inicializa la lista de mejoras y
	 * arranca el "motor" (timer).
	 */
	private void raiz() throws IOException {
		Interfaz interfaz = new Interfaz(datos, mejoras, mejorasClicker);
		interfaz.render();
		timer(interfaz);
	}

	// loop que se actualiza cada 0.015 segundos
	private void timer(Interfaz interfaz) {

		new Timer(15, ejecuta -> {
			datos.reloj(0.015);
			interfaz.render();
		}).start();
	}

}
