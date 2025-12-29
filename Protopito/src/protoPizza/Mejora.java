package protoPizza;

/**
 * Proyecto ProtoPizza.
 * Archivo: Mejora.java
 * Documentación JavaDoc generada para entender el código (núcleo + UI).
 */
import java.util.function.Consumer;

/**
 * Representa una mejora del juego (click o pasiva). Guarda nombre, coste,
 * escalado de coste, requisito de desbloqueo y una acción a aplicar sobre
 * Datos.
 */
public class Mejora {
// clase para la personalizacion y comportamiento de las mejoras pasivas y activas

	private Consumer<Datos> accion;

	/**
	 * Nombre visible de la mejora.
	 */
	protected String nombre;
	/**
	 * Nivel actual de la mejora (cuántas veces se ha comprado).
	 */
	protected int nivel;
	/**
	 * Coste actual para comprar el siguiente nivel.
	 */
	protected double coste;
	private String iconPath;

	/**
	 * Factor de incremento del coste al subir de nivel.
	 */
	protected double incrCoste;
	/**
	 * Umbral de desbloqueo (normalmente récord mínimo).
	 */
	protected double requisitoDesbloqueo;

	// constructor para pasivas y activas
	public Mejora(String nombre, double coste, double incrCoste, double requisitoDesbloqueo, Consumer<Datos> accion,
			String iconPath) {
		this.nombre = nombre;
		this.nivel = 0;
		this.coste = coste;
		this.incrCoste = incrCoste;
		this.requisitoDesbloqueo = requisitoDesbloqueo;
		this.iconPath = iconPath;
		this.accion = accion;
	}

	public boolean comprar(Datos datos) {
		if (!datos.verificarCompra(coste)) {
			return false;
		}

		datos.gastar(coste);
		nivel++;

		if (accion != null)
			accion.accept(datos);

		coste *= incrCoste;
		return true;
	}

	public boolean desbloquado(double numActual) {
		return numActual >= requisitoDesbloqueo;
	}

	public String getNombre() {
		return nombre;
	}

	public int getNivel() {
		return nivel;
	}

	public double getCoste() {
		return coste;
	}

	public String getIconPath() {
		return iconPath;
	}

	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}

}
