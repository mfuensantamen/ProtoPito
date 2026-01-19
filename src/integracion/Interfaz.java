package integracion;

/**
 * Proyecto ProtoPizza.
 * Archivo: Interfaz.java
 *
 * Nota:
 * - Versión simplificada para que NO reviente si faltan recursos (/img/...).
 * - Sin "var" (por compatibilidad con proyectos configurados en Java 8).
 * - Sin cache con HashMap.
 * - Sin Consumer (eso va en Mejora, no aquí).
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import app.Datos;
import app.Mejora;
import visuales.PizzaEfectos;
import visuales.BotonRedondeado;

// clase que refresca y genera elementos de interfaz visual
public class Interfaz extends JFrame {

	// datos
	private Datos datos;

	// etiquetas de texto para numeros y numeros por segundo
	private JLabel lblNum;
	private JLabel lblNps;

	// almacena estos valores, si no cambian no seran refrescados
	private long ultimoNumeroMostrado = Long.MIN_VALUE;
	private String ultimoTextoNps = "";

	// icono de candado para mejoras bloqueadas
	private ImageIcon iconoBloqueo = cargarIconoRecurso("/img/link.png", 16, 16);

	// iconos pizza, el normal y el aumentado para efecto click
	private ImageIcon iconoPizzaNormal = cargarIconoRecurso("/img/pizza.png", 200, 200);
	private ImageIcon iconoPizzaGrande = cargarIconoRecurso("/img/pizza.png", 207, 207);

	// icono de porcion de pizza pequeño
	private ImageIcon iconoSlice = cargarIconoRecurso("/img/pizza_slice.png", 20, 20);

	// pizza etiqueta para el boton de pizza central
	private JLabel etiquetaPizza;

	// efectos visuales del boton
	private PizzaEfectos pizzaFX;

	JPanel panelSuperior;
	
	// panel de mejoras
	private JPanel panelInferior;
	private JScrollPane scrollMejoras;

	// listas de filas mejoras y botones
	private List<Mejora> mejorasActivas = new ArrayList<Mejora>();
	private List<Mejora> mejorasPasivas = new ArrayList<Mejora>();
	private List<BotonRedondeado> botonesMejoras = new ArrayList<BotonRedondeado>();

	// fuente
	public static Font fuente = new Font("Gadugi", Font.BOLD, 17);
	public static Locale localeES = Locale.forLanguageTag("es-ES");
	private static final NumberFormat FORMATO_ENTERO_ES;

	static {
		FORMATO_ENTERO_ES = NumberFormat.getInstance(localeES);
		FORMATO_ENTERO_ES.setGroupingUsed(true); // puntos de miles
		FORMATO_ENTERO_ES.setMaximumFractionDigits(0);
	}

	// flasheo verde de compra, feedback
	private static final Color BTN_FLASH = new Color(170, 255, 170);
	private static final String PROP_FLASH_UNTIL = "flashUntil";

	// colores de botones de mejoras
	private static final Color BTN_VERDE_OK = new Color(200, 255, 200);
	private static final Color BTN_GRIS_NO = new Color(210, 210, 210);
	private static final Color BTN_ROJO_LOCK = new Color(250, 180, 180);

	// formato de numeros en español sin decimales + separador de puntos
	private final NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("es-ES"));

	// tiempo de ejecucion
	public Timer timerPartida = null;
	private JPanel panelTiempo;
	private JLabel lblTiempo;
	private int segundos = 0;
	private int minutos = 0;
	private boolean finPartida = false;
	private JPanel panelDerecha;
	public JLabel lblClicks;

	// =========================
	// CPS (Clicks Por Segundo)
	// =========================
	private double cps = 0.0;

	// guardamos el último click en nanoTime (NO mezclar con currentTimeMillis)
	private long lastClickNano = 0L;

	// timer que baja CPS aunque no haya clicks
	private Timer timerCpsDecay;
	private long lastDecayTickNano = 0L;

	// Ajustes "game feel"
	private static final double CPS_MAX = 25.0; // tope razonable (ajusta si quieres)
	private static final double CLICK_FILTER_MS = 5.0; // ignora eventos <20ms (ruido/duplicados)
	private static final double SMOOTH_A = 0.86; // suavizado CPS (más alto = más estable)
	private static final double SMOOTH_B = 0.15;

	// Decay exponencial
	private static final double GRACE_MS = 850.0; // tiempo de gracia sin bajar
	private static final double DECAY_K = 1.2; // velocidad de caída (más alto = cae más rápido)

	// bloque init de nf
	{
		nf.setMaximumFractionDigits(0);
		nf.setMinimumFractionDigits(0);
		nf.setGroupingUsed(true);
	}

	// constructor de interfaz, construye la interfaz general, de mejoras, conecta
	// el resto de metodos y refresca la interfaz cada tick de reloj
	public Interfaz(Datos datos, List<Mejora> mejorasPasivas, List<Mejora> mejorasActivas) {
		this.datos = datos;
		this.mejorasPasivas = mejorasPasivas;
		this.mejorasActivas = mejorasActivas;
		construirInterfaz();
		generarFilasDeMejoras();
		feedbackBotonPizza();

		// ✅ esto hace que baje SOLO aunque el usuario no clique
		startCpsDecayTimer();

		refrescarInterfaz();
	}

	// carga de icono pizza con su tamaño personalizado devuelve icono
	private ImageIcon cargarIconoRecurso(String ruta, int ancho, int alto) {
		try {
			URL url = getClass().getResource(ruta);
			if (url == null) {
				// si falta recurso, NO revienta
				System.err.println("Recurso no encontrado: " + ruta);
				return null;
			}
			BufferedImage icono = ImageIO.read(url);
			if (icono == null) {
				System.err.println("No se pudo leer imagen: " + ruta);
				return null;
			}
			Image escalada = icono.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
			return new ImageIcon(escalada);

		} catch (IOException e) {
			System.err.println("Error cargando archivo: " + ruta);
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			System.err.println("Error cargando recurso: " + ruta);
			e.printStackTrace();
			return null;
		}
	}

	// =========================
	// CPS: registrar click
	// =========================
	public void contadorClicks() {
		long now = System.nanoTime();

		if (lastClickNano != 0L) {
			long deltaNs = now - lastClickNano;
			double deltaMs = deltaNs / 1_000_000.0;

			// filtro anti-duplicados/ruido
			if (deltaMs < CLICK_FILTER_MS) {
				return;
			}

			double cpsAhora = 1000.0 / deltaMs;
			if (cpsAhora > CPS_MAX)
				cpsAhora = CPS_MAX;

			// suavizado (EMA)
			cps = cps * SMOOTH_A + cpsAhora * SMOOTH_B;
		}

		lastClickNano = now;
	}

	// =========================
	// CPS: caída exponencial automática
	// =========================
	private void startCpsDecayTimer() {
		timerCpsDecay = new Timer(50, e -> {
			long now = System.nanoTime();

			if (lastDecayTickNano == 0L) {
				lastDecayTickNano = now;
				updateCpsLabel();
				return;
			}

			double dtSec = (now - lastDecayTickNano) / 1_000_000_000.0;
			lastDecayTickNano = now;

			if (lastClickNano != 0L) {
				double sinClickMs = (now - lastClickNano) / 1_000_000.0;

				// solo decae después del tiempo de gracia
				if (sinClickMs > GRACE_MS) {
					// decay exponencial: cps *= e^(-k * dt)
					double factor = Math.exp(-DECAY_K * dtSec);
					cps = cps * factor;

					// limpieza para que no se quede en 0.00001
					if (cps < 0.01)
						cps = 0.0;
				}
			}

			updateCpsLabel();
		});

		timerCpsDecay.start();
	}

	private void updateCpsLabel() {
		if (lblClicks == null)
			return;
		if (cps < 0.01) {
			lblClicks.setText("");
		} else if(cps > 10){
			lblClicks.setText(String.format(localeES, "%.2f", cps));
			lblClicks.setForeground(new Color(255, 28, 25));
		}else if(cps > 6){
			lblClicks.setText(String.format(localeES, "%.2f", cps));
			lblClicks.setForeground(new Color(255, 200, 115));
		}else {
			lblClicks.setText(String.format(localeES, "%.2f", cps));
			lblClicks.setForeground(new Color(255, 228, 225));
		}
	}

	public int timerPartida() {
		timerPartida = new Timer(1000, e -> {
			segundos++;
			if (segundos > 59) {
				segundos = 0;
				minutos++;
			}
		});
		return segundos;
	}

	// generacion de todos los elementos de la interfaz
	private void construirInterfaz() {
		setTitle("ProtoPizza |  Clicker — Incremental");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(700, 920));
		getContentPane().setLayout(new BorderLayout(0, 0));

		// panel superior
		panelSuperior = new JPanel();
		panelSuperior.setPreferredSize(new Dimension(850, 390));
		panelSuperior.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelSuperior.setBackground(new Color(150, 150, 170));
		panelSuperior.setLayout(new BorderLayout(0, 0));
		getContentPane().add(panelSuperior, BorderLayout.NORTH);

		// panel con info de los numeros superiores
		JPanel panelNums = new JPanel();
		panelNums.setBorder(new EmptyBorder(16, 20, 8, 20));
		panelNums.setOpaque(false);
		panelNums.setLayout(new BoxLayout(panelNums, BoxLayout.Y_AXIS));
		panelSuperior.add(panelNums, BorderLayout.CENTER);

		// pequeño texto en la parte inferior del panel superior
		JLabel pieBoton = new JLabel("Haz Click para cocinar Pizzas");
		pieBoton.setForeground(new Color(225, 208, 205));
		pieBoton.setFont(new Font("Consolas", Font.BOLD, 11));
		pieBoton.setHorizontalAlignment(SwingConstants.CENTER);
		pieBoton.setBorder(new EmptyBorder(0, 0, 6, 0));
		panelSuperior.add(pieBoton, BorderLayout.SOUTH);

		panelTiempo = new JPanel();
		panelTiempo.setPreferredSize(new Dimension(0, 40));
		panelTiempo.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panelTiempo.setAlignmentY(Component.TOP_ALIGNMENT);
		panelTiempo.setAlignmentX(Component.CENTER_ALIGNMENT);
		panelTiempo.setBorder(new EmptyBorder(0, 0, 0, 0));
		panelTiempo.setLayout(new BorderLayout(0, 0));
		panelTiempo.setOpaque(false);

		lblTiempo = new JLabel("");
		lblTiempo.setHorizontalAlignment(SwingConstants.CENTER);
		lblTiempo.setForeground(new Color(255, 215, 0));
		lblTiempo.setText(minutos + ":" + segundos);
		lblTiempo.setFont(new Font("Consolas", Font.BOLD, 20));

		panelTiempo.add(lblTiempo, BorderLayout.NORTH);
		panelNums.add(panelTiempo);

		// numero grande central del recurso
		lblNum = new JLabel("0");
		lblNum.setForeground(new Color(255, 215, 0));
		lblNum.setFont(new Font("Consolas", Font.BOLD, 46));
		lblNum.setAlignmentX(Component.CENTER_ALIGNMENT);
		panelNums.add(lblNum);

		// numero/s mas icono de pizza pequeño
		lblNps = new JLabel("/s 0");
		lblNps.setForeground(new Color(255, 228, 225));
		lblNps.setFont(fuente);
		lblNps.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblNps.setBorder(new EmptyBorder(6, 0, 6, 0));
		lblNps.setIcon(iconoSlice);
		lblNps.setHorizontalTextPosition(SwingConstants.RIGHT);
		lblNps.setIconTextGap(6);
		panelNums.add(lblNps);

		// imagen de la pizza grande central
		etiquetaPizza = new JLabel(this.iconoPizzaNormal);
		etiquetaPizza.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		etiquetaPizza.setAlignmentX(Component.CENTER_ALIGNMENT);
		etiquetaPizza.setPreferredSize(new Dimension(208, 208));

		// efectos de feedback visuales de la pizza
		pizzaFX = new PizzaEfectos(this.etiquetaPizza);
		pizzaFX.setOpaque(false);
		// halo efecto autoclick
		pizzaFX.setHaloColors(new Color(255, 215, 0), new Color(255, 245, 200));
		pizzaFX.setHaloStrokes(12f, 8f);
		pizzaFX.setHaloPadding(2);
		// pequeños iconos de pizzas
		pizzaFX.setSliceIcon("/img/pizza_slice.png", 22);
		pizzaFX.setAlignmentX(Component.CENTER_ALIGNMENT);
		pizzaFX.setPreferredSize(new Dimension(260, 260));
		pizzaFX.setMaximumSize(new Dimension(280, 280));
		panelNums.add(pizzaFX);

		panelDerecha = new JPanel();
		panelDerecha.setOpaque(false);
		panelDerecha.setBorder(null);
		panelDerecha.setLayout(new BoxLayout(panelDerecha, BoxLayout.LINE_AXIS));
		panelSuperior.add(panelDerecha, BorderLayout.EAST);
		Dimension anchoDerecha = new Dimension(100, 0);
		panelDerecha.setPreferredSize(anchoDerecha);
		panelDerecha.setMinimumSize(anchoDerecha);
		panelDerecha.setMaximumSize(new Dimension(100, Integer.MAX_VALUE));

		JPanel panelIzquierda = new JPanel();
		panelIzquierda.setOpaque(false);
		panelIzquierda.setPreferredSize(anchoDerecha);
		panelDerecha.setMinimumSize(anchoDerecha);
		panelDerecha.setMaximumSize(new Dimension(100, Integer.MAX_VALUE));
		panelSuperior.add(panelIzquierda, BorderLayout.WEST);

		lblClicks = new JLabel("00.00");
		lblClicks.setVerticalAlignment(SwingConstants.TOP);
		lblClicks.setForeground(new Color(255, 228, 225));
		lblClicks.setHorizontalAlignment(SwingConstants.CENTER);
		lblClicks.setFont(new Font("Consolas", Font.BOLD, 30));

		Dimension d = lblClicks.getPreferredSize();
		lblClicks.setPreferredSize(d);
		lblClicks.setMinimumSize(d);
		lblClicks.setMaximumSize(d);

		lblClicks.setText("");
		panelDerecha.add(lblClicks);

		// panel inferior de la interfaz, mejoras
		panelInferior = new JPanel();
		panelInferior.setOpaque(false);
		panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));
		panelInferior.setBackground(new Color(245, 245, 245));
		panelInferior.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 80, 0, 80, new Color(230, 225, 245)),
				BorderFactory.createEmptyBorder(15, 20, 0, 20)));

		scrollMejoras = new JScrollPane(panelInferior);
		scrollMejoras.setBorder(null);
		getContentPane().add(scrollMejoras, BorderLayout.CENTER);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	// feedback de hacer el boton de la pizza reaccione a click
	private void feedbackBotonPizza() {
		final int tiempoFeedback = 90;
		timerPartida();

		// si el ususario clicka la pizza
		etiquetaPizza.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {

				// ✅ registra CPS antes del click lógico

				contadorClicks();

				if (timerPartida != null && !finPartida) {
					timerPartida.start();
				}

				// se hace grande
				etiquetaPizza.setIcon(iconoPizzaGrande);

				// por 90ms
				new Timer(tiempoFeedback, ejecuta -> {
					etiquetaPizza.setIcon(iconoPizzaNormal);
					((Timer) ejecuta.getSource()).stop();
				}).start();

				// invoca pequeños iconos de pizzas
				double nps = datos.getNps();
				double npc = datos.getClickIncremento() + nps / 50.0;
				pizzaFX.spawnClickFloat(npc, formatoAbreviado(npc, true, false));

				// ejecuta la accion de clickar
				datos.click();

				// refresca interfaz
				refrescarInterfaz();
			}
		});
	}

	// bucle que crea botones de mejora dinamicamente segun cuantas mejoras haya
	private BotonRedondeado crearBotonMejora(Mejora m) {
		BotonRedondeado btn = new BotonRedondeado("", 40);
		btn.setLayout(new BorderLayout());
		btn.setFocusPainted(false);
		btn.setBackground(BTN_GRIS_NO);
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
		btn.setPreferredSize(new Dimension(10, 56));
		btn.setAlignmentX(Component.CENTER_ALIGNMENT);
		btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));

		JLabel lblIcon = new JLabel();
		lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
		lblIcon.setVerticalAlignment(SwingConstants.CENTER);
		lblIcon.setPreferredSize(new Dimension(62, 44));

		JLabel lblLeft = new JLabel();
		lblLeft.setFont(fuente);
		lblLeft.setHorizontalAlignment(SwingConstants.LEFT);
		lblLeft.setVerticalAlignment(SwingConstants.CENTER);

		JLabel lblRight = new JLabel();
		lblRight.setFont(fuente);
		lblRight.setHorizontalAlignment(SwingConstants.RIGHT);
		lblRight.setVerticalAlignment(SwingConstants.CENTER);
		lblRight.setPreferredSize(new Dimension(80, 34));

		btn.add(lblIcon, BorderLayout.WEST);
		btn.add(lblLeft, BorderLayout.CENTER);
		btn.add(lblRight, BorderLayout.EAST);

		btn.putClientProperty("icon", lblIcon);
		btn.putClientProperty("left", lblLeft);
		btn.putClientProperty("right", lblRight);

		btn.addActionListener(ejecuta -> {
			if (!datos.verificarCompra(m))
				return;

			m.comprar(datos);

			long hasta = System.currentTimeMillis() + 70;
			btn.putClientProperty(PROP_FLASH_UNTIL, hasta);

			refrescarInterfaz();
		});

		return btn;
	}

	// por cada mejora que haya, añade los botones necesarios para que aparezcan
	private void generarFilasDeMejoras() {
		panelInferior.removeAll();
		botonesMejoras.clear();

		for (Mejora m : mejorasActivas) {
			BotonRedondeado btn = crearBotonMejora(m);
			botonesMejoras.add(btn);
			panelInferior.add(btn);
			panelInferior.add(Box.createVerticalStrut(10));
		}
		for (Mejora m : mejorasPasivas) {
			BotonRedondeado btn = crearBotonMejora(m);
			botonesMejoras.add(btn);
			panelInferior.add(btn);
			panelInferior.add(Box.createVerticalStrut(10));
		}

		panelInferior.revalidate();
		panelInferior.repaint();
	}

	private String formatoAbreviado(double n, boolean conDecimales, boolean numPrincipal) {
		final String[] sufijos = { "", "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No" };
		double abs = Math.abs(n);

		if (numPrincipal) {
			if (abs < 1_000) {
				return FORMATO_ENTERO_ES.format(abs);
			}
			if (abs <= 99_999_999) {
				return FORMATO_ENTERO_ES.format(abs);
			}
			// mas de 100M abreviado con decimales
			@SuppressWarnings("unused")
			String s = abreviarNumeros(abs, true, sufijos);
		}

		if (abs < 1_000) {
			return conDecimales ? String.format(localeES, "%.2f", abs) : String.format(localeES, "%.0f", abs);
		}

		return abreviarNumeros(abs, conDecimales, sufijos);
	}

	private String abreviarNumeros(double n, boolean conDecimales, String[] sufijos) {
		double valor = n;
		int indice = 0;

		while (valor >= 1_000.0 && indice < sufijos.length - 1) {
			valor /= 1_000.0;
			indice++;
		}

		if (!conDecimales) {
			return String.format(localeES, "%.0f%s", valor, sufijos[indice]);
		}

		if (indice > 0) {
			return String.format(localeES, "%.2f%s", valor, sufijos[indice]);
		}

		return String.format(localeES, "%.2f", valor);
	}

	private void actualizarBotonMejora(BotonRedondeado btn, Mejora m) {
		JLabel icon = (JLabel) btn.getClientProperty("icon");
		JLabel left = (JLabel) btn.getClientProperty("left");
		JLabel right = (JLabel) btn.getClientProperty("right");

		boolean desbloqueado = m.desbloquado(datos.getMaximo());
		int nivel = m.getNivel();
		boolean puedeComprar = desbloqueado && datos.verificarCompra(m);

		String estado = desbloqueado + "|" + puedeComprar + "|" + nivel + "|" + (long) m.getCoste();
		Object prev = btn.getClientProperty("estado");
		boolean cambio = !estado.equals(prev);

		Object v = btn.getClientProperty(PROP_FLASH_UNTIL);
		long ahora = System.currentTimeMillis();
		boolean tieneFlash = (v instanceof Long);
		boolean flasheando = tieneFlash && ahora < ((Long) v).longValue();
		boolean expirado = tieneFlash && ahora >= ((Long) v).longValue();

		boolean forzar = false;
		if (expirado) {
			btn.putClientProperty(PROP_FLASH_UNTIL, null);
			forzar = true;
		}
		if (!cambio && !flasheando && !forzar)
			return;

		btn.putClientProperty("estado", estado);

		if (!puedeComprar)
			btn.setIcon(iconoBloqueo);
		else
			btn.setIcon(null);

		if (!desbloqueado) {
			icon.setIcon(cargarIconoRecurso("/img/link.png", 32, 32));
			left.setText("            Requiere " + formatoAbreviado(m.getCoste(), true, false));
			right.setText("");
			left.setFont(fuente);
			btn.setBackground(flasheando ? BTN_FLASH : BTN_ROJO_LOCK);
			btn.setEnabled(false);
			btn.setCursor(Cursor.getDefaultCursor());
			return;
		}

		ImageIcon ico = cargarIconoRecurso(m.getIconPath(), 32, 32);
		icon.setIcon(ico);

		left.setText(m.getNombre() + "  [ " + nivel + " ]");
		left.setFont(fuente);
		right.setText(formatoAbreviado(m.getCoste(), true, false));
		right.setFont(fuente);

		if (flasheando)
			btn.setBackground(BTN_FLASH);
		else if (puedeComprar)
			btn.setBackground(BTN_VERDE_OK);
		else
			btn.setBackground(BTN_GRIS_NO);

		btn.setEnabled(puedeComprar);
		btn.setCursor(puedeComprar ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());

		if (m.getNombre().equalsIgnoreCase("Impresora 3D de pizzas") && m.getNivel() == 1) {
			timerPartida.stop();
			finPartida = true;
		}
	}

	// procedimiento que se encarga de refrescar la informacion de la interfaz
	public void refrescarInterfaz() {
		double nps = datos.getNps();
		double npc = datos.getClickIncremento() + nps / 50.0;

		// CPS label (también lo actualiza el timer, pero aquí no molesta)
		updateCpsLabel();

		if (segundos < 10) {
			lblTiempo.setText("Tiempo de Partida: " + minutos + ":0" + segundos);
		} else {
			lblTiempo.setText("Tiempo de Partida: " + minutos + ":" + segundos);
		}

		if (botonesMejoras.isEmpty())
			return;

		if (datos.getNum() != ultimoNumeroMostrado) {
			ultimoNumeroMostrado = (long) datos.getNum();
			lblNum.setText(formatoAbreviado(datos.getNum(), true, true));
		}

		String npsBase = "/s " + formatoAbreviado(nps, true, false);

		double periodoAuto = datos.getPeriodoAutoClicker();
		String texto;

		if (datos.getNivelAutoClicker() == 0) {
			texto = npsBase;
		} else {
			texto = npsBase + String.format(localeES, "  |  Cocineros +%s cada %.2fs",
					formatoAbreviado(npc, true, false), periodoAuto);
		}

		if (!texto.equals(ultimoTextoNps)) {
			ultimoTextoNps = texto;
			lblNps.setText(texto);
		}

		if (datos.autoClickerPulsado()) {
			pizzaFX.efectoHaloTick();
		}

		int idx = 0;
		for (Mejora m : mejorasActivas) {
			actualizarBotonMejora(botonesMejoras.get(idx++), m);
		}

		for (Mejora m : mejorasPasivas) {
			actualizarBotonMejora(botonesMejoras.get(idx++), m);
		}
	}
}
