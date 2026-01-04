package protoPizza;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

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
	// * probar a poner imagenes de emjojis en vez de strings y colocar el nombre de
	// la mejora junto al icono a la derecha y dentrar todo
	// elemento gatcha
	// objetos consumibles tiempo limitado
	// * marikita marikita marikita marikon

	private Datos datos = new Datos();
	public static List<Mejora> mejorasActivas = new ArrayList<>();
	public static List<Mejora> mejorasPasivas = new ArrayList<>();

	// Constructor de ProtoPizzaAPP
	public ProtoPizzaAPP() {

		// mejoras activas clickando
		// nombre / coste / incrementoCoste / umbralDesbloqueo / accion / ruta icono
		mejorasActivas.add(new Mejora("Contratar Cocineros", 216, 1.225, 200, accion -> datos.subirAutoClicker(),
				"/img/cook.png"));
		mejorasActivas.add(new Mejora("Experiencia del Chef", 13.5, 1.15, 0, accion -> datos.subirPotenciaClick(0.5),
				"/img/brain.png"));
		mejorasActivas.add(new Mejora("Premium Pizza-Cutter", 78, 1.1575, 70, accion -> datos.subirPotenciaClick(1.125),
				"/img/knife.png"));
		mejorasActivas.add(new Mejora("Manos de Maestro", 1_035, 1.1725, 900, accion -> datos.subirPotenciaClick(5.625),
				"/img/hand.png"));

		// mejoras pasivas autoclicker
		// nombre / coste / incrementoCoste / umbralDesbloqueo / accion / ruta icono
		mejorasPasivas
				.add(new Mejora("Pala Pizzera", 103.5, 1.1575, 90, accion -> datos.subirNPS(0.625), "/img/wood.png"));
		mejorasPasivas
				.add(new Mejora("Air Fryer", 431.25, 1.1575, 380, accion -> datos.subirNPS(2.25), "/img/fire.png"));
		mejorasPasivas.add(
				new Mejora("Horno de piedra", 1_466.25, 1.165, 1_300, accion -> datos.subirNPS(7.5), "/img/fire.png"));
		mejorasPasivas.add(
				new Mejora("Horno doble", 4_743.75, 1.1725, 4_200, accion -> datos.subirNPS(22.5), "/img/fire.png"));
		mejorasPasivas.add(new Mejora("Horno industrial", 14_231.25, 1.18, 12_500, accion -> datos.subirNPS(68.75),
				"/img/fire.png"));
		mejorasPasivas.add(new Mejora("Cinta automática", 41_400, 1.18, 36_000, accion -> datos.subirNPS(187.5),
				"/img/railway.png"));
		mejorasPasivas.add(new Mejora("Amasadora automática", 116_437.5, 1.1875, 100_000, accion -> datos.subirNPS(525),
				"/img/crane.png"));
		mejorasPasivas.add(new Mejora("Fábrica de masa", 327_750, 1.1875, 285_000, accion -> datos.subirNPS(1_500),
				"/img/factory.png"));
		mejorasPasivas.add(new Mejora("Línea de producción", 905_625, 1.195, 800_000, accion -> datos.subirNPS(4_250),
				"/img/factory.png"));
		mejorasPasivas.add(new Mejora("Central Pizzera", 2_501_250, 1.195, 2_200_000, accion -> datos.subirNPS(11_875),
				"/img/office.png"));
		mejorasPasivas.add(new Mejora("Megafactoría", 7_072_500, 1.2025, 6_200_000, accion -> datos.subirNPS(33_750),
				"/img/factory.png"));
		mejorasPasivas.add(new Mejora("PizzaCorp", 19_837_500, 1.2025, 17_000_000, accion -> datos.subirNPS(93_750),
				"/img/briefcase.png"));
		mejorasPasivas.add(new Mejora("Multinacional", 60_375_000, 1.2175, 52_000_000,
				accion -> datos.subirNPS(275_000), "/img/earth_africa.png"));
		mejorasPasivas.add(new Mejora("Impresora 3D de pizzas", 181_125_000, 1.225, 160_000_000,
				accion -> datos.subirNPS(875_000), "/img/printer.png"));

	}

	// main
	// invokeLater para crear y mostrar la interfaz
	// dentro mas adelante en su propio hilo de Swing (??????)
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new ProtoPizzaAPP().raiz();
		});
	}

	// Construir y muestrar interfaz arranca mejoras yarranca el timer (motor)
	private void raiz() {
		// arranca la interfaz enviandole datos y la lista de las mejoras
		Interfaz interfaz = new Interfaz(datos, mejorasPasivas, mejorasActivas);
		// inicializa timer y le envia la interfaz para poder actualizarla cada tick
		timer(interfaz);
	}

	// loop que se actualiza cada 0.015 segundos (15ms)
	private void timer(Interfaz interfaz) {
		// crea timer de 15ms que loopea y ejecuta el reloj en datos y el refresco de
		// interfaz en interfaz
		new Timer(15, ejecuta -> {
			datos.reloj(0.015);
			interfaz.refrescarInterfaz();
		}).start();
	}

}