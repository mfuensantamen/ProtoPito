package protoPizza;

// para que lo que envie una accion inmediata que se ejecuta sobre datos
// Se usa para aplicar efectos de mejoras mediante lambdas.
// para aplicar "efectos" o codigo de las mejoras, funciones que se pasan como parametro
interface EjecutorAccionDatos {
	void ejecutar(Datos datos);
}

// mejoras pasivas y activas con atributos y acciones que afectan a datos
public class Mejora {
// clase para la personalizacion y comportamiento de las mejoras pasivas y activas

	protected String nombre;

	// Nivel de la mejora (cuantas veces se ha comprado)
	protected int nivel;

	// coste para comprar el siguiente nivel
	protected double coste;

	// escala de incremento del coste al subir de nivel
	protected double incrCoste;

	// umbral para desbloquear la mejora
	protected double requisitoDesbloqueo;

	// ruta del icono de cada mejora
	private String iconPath;

	// accion asociada a las mejoras
	private EjecutorAccionDatos accion;

	// constructores
	public Mejora() {
	}

	public Mejora(String nombre, double coste, double incrCoste, double requisitoDesbloqueo, EjecutorAccionDatos accion,
			String iconPath) {
		this.nombre = nombre;
		this.nivel = 0;
		this.coste = coste;
		this.incrCoste = incrCoste;
		this.requisitoDesbloqueo = requisitoDesbloqueo;
		this.iconPath = iconPath;
		this.accion = accion;
	}

	// funcion que devuelve un boolean si se ha podido o no comprar
	public boolean comprar(Datos datos) {

		// si no se puede comprar sale del metodo
		if (!datos.verificarCompra(this)) {
			return false;
		}

		// si se puede comprar continua y gasta el recurso
		datos.gastar(coste);
		// y sube el nivel de la mejora
		nivel++;

		// para evitar errores si una mejora no tiene una accion asociada en sus
		// parametros
		if (accion != null) {
			// ejecuta la funcion y envia los datos por parametro
			accion.ejecutar(datos);
		}

		// aplica el escalado para aumentar el coste de la siguiente compra
		coste *= incrCoste;
		return true;
	}

	// guarda el estado actual de la mejora, si el recurso es insuficiente, la
	// mejora no sera clickable
	public boolean desbloquado(double num) {
		return num >= requisitoDesbloqueo;
	}

	//
	//
	// getters setters
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

}
