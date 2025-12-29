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
	protected String icono;
	protected double coste;

	/**
	 * Factor de incremento del coste al subir de nivel.
	 */
	protected double incrCoste;
	/**
	 * Umbral de desbloqueo (normalmente récord mínimo).
	 */
	protected double requisitoDesbloqueo;

	// constructor para pasivas y activas
	/**
	 * Constructor de Mejora. Inicializa el estado interno de la clase.
	 * 
	 * Parámetros:
	 * 
	 * @param nombre              TODO
	 * @param coste               TODO
	 * @param incrCoste           TODO
	 * @param requisitoDesbloqueo TODO
	 * @param accion              TODO
	 */
	public Mejora(String nombre, String icono, double coste, double incrCoste, double requisitoDesbloqueo,
			Consumer<Datos> accion) {
		this.nombre = nombre;
		this.icono = icono;
		this.nivel = 0;
		this.coste = coste;
		this.incrCoste = incrCoste;
		this.requisitoDesbloqueo = requisitoDesbloqueo;
		this.accion = accion;
	}

	/**
	 * Compra/upgradea esta mejora aplicando su acción sobre el modelo. Incrementa
	 * nivel, aumenta coste (incrCoste) y ejecuta la acción asociada.
	 * 
	 * Parámetros:
	 * 
	 * @param datos TODO
	 * @return TODO
	 */
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

	/**
	 * Indica si la mejora debe mostrarse (desbloqueo) según el máximo alcanzado.
	 * Normalmente usa el requisitoDesbloqueo contra el récord del jugador.
	 * 
	 * Parámetros:
	 * 
	 * @param numActual TODO
	 * @return TODO
	 */
	public boolean desbloquado(double numActual) {
		return numActual >= requisitoDesbloqueo;
	}

	/**
	 * Método getIncrCoste de Mejora. Ver descripción en el código/uso.
	 * 
	 * @return TODO
	 */
	public double getIncrCoste() {
		return incrCoste;
	}

	/**
	 * Método setIncrCoste de Mejora. Ver descripción en el código/uso.
	 * 
	 * Parámetros:
	 * 
	 * @param incrCoste TODO
	 */
	public void setIncrCoste(double incrCoste) {
		this.incrCoste = incrCoste;
	}

	/**
	 * Método getNombre de Mejora. Ver descripción en el código/uso.
	 * 
	 * @return TODO
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * Método getNivel de Mejora. Ver descripción en el código/uso.
	 * 
	 * @return TODO
	 */
	public int getNivel() {
		return nivel;
	}

	/**
	 * Método getCoste de Mejora. Ver descripción en el código/uso.
	 * 
	 * @return TODO
	 */
	public double getCoste() {
		return coste;
	}

	/**
	 * Método setCoste de Mejora. Ver descripción en el código/uso.
	 * 
	 * Parámetros:
	 * 
	 * @param coste TODO
	 */
	public void setCoste(double coste) {
		this.coste = coste;
	}

	public String getIcono() {
		return icono;
	}

}
