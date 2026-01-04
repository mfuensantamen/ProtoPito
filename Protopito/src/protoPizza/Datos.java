package protoPizza;

public class Datos {
// clase donde se guardas los datos base y funcionalidades esenciales del juego principal
// Almacenaje del los estados numericos y los valores nps npc y tal. 
// no se conecta con interfaz solo se actualiza con input de usuario y timer

	// uso para saber si se ha usado el autocliker
	private boolean inicializarAutoclicker = false;

	// n de pizzas inicial
	private double num = 99999999990.;

	// n de pizzas/s iniciales
	private double nps = 0;

	// guarda el maximo historico
	private double recordMaximo = 0;

	// el valor de cada click
	private double clickIncremento = 1;

	// el clicker clickara automaticamente cada 1 segundo a nivel 1
	private double periodoInicial = 1.0;

	// periodo de autoclicker
	private double periodoAutoClicker = periodoInicial;

	// cuenta el tiempo que pasa desde ultimo clik
	private double contadorAutoClicker = 0.0;

	// bajada de tiempo de la frecuencia del autoclicker al subir de nivel
	private double decrementoNivel = 0.033;

	// autoclick no clickara mas rapido que esto (1 click cada 50ms)
	private double periodoMinimo = 0.05;

	// nivel inicial de autoclick osea, desactivado
	private int nivelAutoClicker = 0;

	//
	//
	// activacion de autoclicker
	// si ya esta activo sube el nivel y mejora atributos a corde
	public void subirAutoClicker() {
		nivelAutoClicker++;
		// nivel maximo 30
		// si el nivel es 0, multiplica por cero y hace un decremento de 0
		// si el nivel es 1 multiplica por 0.033 y restara a 1(periodo inicial) = 0,967
		periodoAutoClicker = periodoInicial - (nivelAutoClicker - 1) * decrementoNivel;

		// esto es si se pasa por debajo de 50ms
		if (periodoAutoClicker < periodoMinimo) {
			periodoAutoClicker = periodoMinimo;
		}
	}

	// cada click aumenta el num en la cantidad de incremento
	// tambien escala cuantos mas pasivos tengas nps/50 para que no escale demasiado
	public void click() {
		// numeros actuales = numeros actuales + potencia + nums pasivos / 50
		num += clickIncremento + nps / 50;
		// y guarda numero maximo alcanzado
		if (num > recordMaximo) {
			recordMaximo = num;
		}
	}

	// numeros actuales + numeros + numeros/s * 0.015 frecuencia refresco interfaz
	public void reloj(double diferenciaTiempo) {

		num += nps * diferenciaTiempo;
		// almacenar maximo para umbrales de desbloqueos
		if (num > recordMaximo) {
			recordMaximo = num;
		}

		// si el nivel del autoclicker es 0, no se ejecuta su funcion
		if (nivelAutoClicker == 0) {
			return;
		}

		// funcion autoclicker
		// contador = contador + el tiempo que ha pasado desde la ultima vez
		contadorAutoClicker += diferenciaTiempo;
		// mientras que el contador sea mayor o igual al periodo/velocidad del
		// autoclicker
		while (contadorAutoClicker >= periodoAutoClicker) {
			// el autoclicker clickara por el usuario
			this.click();
			// se cambia el booleano para que active el efecto visual del clickado
			inicializarAutoclicker = true;
			// y se reinicia el contador
			contadorAutoClicker -= periodoAutoClicker;
		}
	}

	public boolean autoClickerPulsado() {
		// inicia el autoclicker y todos los eventos relacionados(calculo y efectos)
		// si esta off lo deja siempre off pero si se pone On lo deja siempre On
		// un "interrupotor"
		if (inicializarAutoclicker) {
			inicializarAutoclicker = false;
			return true;
		}
		return false;
	}

	// verifica el saldo acctual con el coste de la mejora que solicita comprar
	public boolean verificarCompra(Mejora mejora) {
		// Bloqueo de "Contratar Cocineros" a partir de nivel 30
		if (mejora.getNombre().equalsIgnoreCase("Contratar Cocineros") && mejora.getNivel() >= 30) {
			return false;
		}

		// si no llega devuelve false
		return num >= mejora.getCoste();
	}

	public void subirPotenciaClick(double incremento) {
		// sube la potencia del click manual
		clickIncremento += incremento;
	}

	public void subirNPS(double incremento) {
		// sube la cantidad obtenida por segundo
		nps += incremento;
	}

	public void gastar(double cantidad) {
		// resta al saldo la cantidad del coste de la mejora
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
