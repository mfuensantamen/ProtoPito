package protoPizzaa;

import java.util.function.Consumer;

/**
 * Representa una mejora comprable.
 * <p>
 * Una mejora puede ser:
 * <ul>
 * <li><b>Activa</b>: afecta al click (por ejemplo, subir clickIncremento o
 * auto-clicker)</li>
 * <li><b>Pasiva</b>: afecta a NPS (pizzas por segundo)</li>
 * </ul>
 *
 * La mejora aplica su efecto mediante una función {@link Consumer} que recibe
 * el {@link Datos} y lo modifica (sube NPS, sube click, etc.).
 */
public class Mejora {

	/**
	 * Acción a ejecutar al comprar. Se inyecta desde ProtoPizzaAPP como lambda.
	 * Ejemplo: {@code d -> d.subirNPS(0.5)}.
	 */
	private final Consumer<Datos> accion;

	/** Nombre visible de la mejora. */
	protected final String nombre;

	/** Nivel actual (cuántas veces se ha comprado). */
	protected int nivel;

	/** Coste actual (va subiendo al comprar). */
	protected double coste;

	/** Multiplicador del coste tras cada compra (ej. 1.21). */
	protected final double incrCoste;

	/**
	 * Umbral de desbloqueo (según récord máximo del jugador). Se compara con
	 * {@link Datos#getMaximo()}.
	 */
	protected final double requisitoDesbloqueo;

	/**
	 * Crea una mejora.
	 *
	 * @param nombre              nombre visible
	 * @param coste               coste inicial
	 * @param incrCoste           multiplicador del coste después de comprar (debe
	 *                            ser > 1 normalmente)
	 * @param requisitoDesbloqueo récord mínimo requerido para mostrar/desbloquear
	 *                            esta mejora
	 * @param accion              efecto a aplicar sobre el {@link Datos} al comprar
	 *                            (puede ser null)
	 */
	public Mejora(String nombre, double coste, double incrCoste, double requisitoDesbloqueo, Consumer<Datos> accion) {
		this.nombre = nombre;
		this.nivel = 0;
		this.coste = coste;
		this.incrCoste = incrCoste;
		this.requisitoDesbloqueo = requisitoDesbloqueo;
		this.accion = accion;
	}

	/**
	 * Intenta comprar la mejora.
	 * <p>
	 * Si hay suficientes pizzas:
	 * <ol>
	 * <li>Resta el coste actual</li>
	 * <li>Incrementa el nivel</li>
	 * <li>Aplica la acción sobre el modelo</li>
	 * <li>Actualiza el coste multiplicándolo por {@link #incrCoste}</li>
	 * </ol>
	 *
	 * @param datos modelo sobre el que se aplicará la mejora
	 * @return true si se compró; false si no había suficientes pizzas
	 */
	public boolean comprar(Datos datos) {
		if (!datos.verificarCompra(coste)) {
			return false;
		}

		datos.gastar(coste);
		nivel++;

		if (accion != null) {
			accion.accept(datos);
		}

		coste *= incrCoste;
		return true;
	}

	/**
	 * Indica si la mejora está desbloqueada en función del récord máximo alcanzado.
	 * <p>
	 * Se usa el <b>máximo histórico</b> para que una mejora no se vuelva a bloquear
	 * tras gastar pizzas.
	 *
	 * @param numActual normalmente {@link Datos#getMaximo()}
	 * @return true si {@code numActual >= requisitoDesbloqueo}
	 */
	public boolean desbloquado(double numActual) {
		return numActual >= requisitoDesbloqueo;
	}

	/** @return multiplicador de coste */
	public double getIncrCoste() {
		return incrCoste;
	}

	/** @return nombre visible */
	public String getNombre() {
		return nombre;
	}

	/** @return nivel actual */
	public int getNivel() {
		return nivel;
	}

	/** @return coste actual */
	public double getCoste() {
		return coste;
	}

	/**
	 * Cambia el coste actual (útil en tests o balanceo dinámico).
	 *
	 * @param coste nuevo coste actual
	 */
	public void setCoste(double coste) {
		this.coste = coste;
	}
}
