package protoPizza;

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
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

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
	private PizzaFXPane pizzaFX;

	// panel de mejoras
	private JPanel panelInferior;
	private JScrollPane scrollMejoras;

	// listas de filas mejoras y botones
	private List<Mejora> mejorasActivas = new ArrayList<>();
	private List<Mejora> mejorasPasivas = new ArrayList<>();
	private List<RoundedButton> botonesMejoras = new ArrayList<>();

	// fuente
	public static Font fuente = new Font("Gadugi", Font.BOLD, 17);
	public static Locale localeES = Locale.of("es", "ES");
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
		refrescarInterfaz();
	}

	// carga de icono pizza con su tamaño personalizado devuelve icono
	private ImageIcon cargarIconoRecurso(String ruta, int ancho, int alto) {
		try {
			URL url = getClass().getResource(ruta);
			// enlace de recurso y buffer de icono
			BufferedImage icono = ImageIO.read(url);
			// escala los iconos al tamaño necesario
			Image escalada = icono.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
			return new ImageIcon(escalada);

		} catch (IOException e) {
			System.err.println("Error cargando archivo: " + ruta);
			e.printStackTrace();
			return null;
		}
	}

	// generacion de todos los elementos de la interfaz
	private void construirInterfaz() {
		setTitle("ProtoPizza |  Clicker — Incremental");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(700, 920));
		getContentPane().setLayout(new BorderLayout(0, 0));

		// panel superior
		JPanel panelSuperior = new JPanel();
		panelSuperior.setPreferredSize(new Dimension(850, 380));
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
		pizzaFX = new PizzaFXPane(this.etiquetaPizza);
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

		// panel inferior de la interfaz, mejoras
		panelInferior = new JPanel();
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
//////////////////// fin de generar interfaz

	// feedback de hacer el boton de la pizza reaccione a click
	private void feedbackBotonPizza() {
		final int tiempoFeedback = 90;
		// si el ususario clicka la pizza
		etiquetaPizza.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				// se hace grande
				etiquetaPizza.setIcon(iconoPizzaGrande);
				// por 90ms
				new Timer(tiempoFeedback, ejecuta -> {
					// despues vuelve al tamaño normal
					etiquetaPizza.setIcon(iconoPizzaNormal);
					((Timer) ejecuta.getSource()).stop();
				}).start();
				// invoca pequeños iconos con el valor del click que desaparecen con el tiempo
				double npc = datos.getClickIncremento() + datos.getNps() / 50.0;
				pizzaFX.spawnClickFloat(npc, formatoAbreviado(npc, true, false));
				// ejecuta la accion de clickar
				datos.click();
				// refresca la interfaz
				refrescarInterfaz();
			}
		});
	}
//////////////////// fin de efectos feedback pizza

	// bucle que crea botones de mejora dinamicamente segun cuantas mejoras haya
	private RoundedButton crearBotonMejora(Mejora m) {
		// se crea el boton
		RoundedButton btn = new RoundedButton("", 40);
		btn.setLayout(new BorderLayout());
		btn.setFocusPainted(false);
		btn.setBackground(BTN_GRIS_NO);
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
		btn.setPreferredSize(new Dimension(10, 56));
		btn.setAlignmentX(Component.CENTER_ALIGNMENT);
		btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));

		// icono de la mejora
		JLabel lblIcon = new JLabel();
		lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
		lblIcon.setVerticalAlignment(SwingConstants.CENTER);
		lblIcon.setPreferredSize(new Dimension(62, 44));
		// nombre de la mejora
		JLabel lblLeft = new JLabel();
		lblLeft.setFont(fuente);
		lblLeft.setHorizontalAlignment(SwingConstants.LEFT);
		lblLeft.setVerticalAlignment(SwingConstants.CENTER);
		// coste de la mejora
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

		// se le adjunta la funcion de si el usuario clicka el boton verifica si se
		// puede usar (si puede permitirse la mejora)
		btn.addActionListener(ejecuta -> {
			if (!datos.verificarCompra(m)) {
				return;
			}
			// si se compra, se ejecuta el metodo comprar
			m.comprar(datos);
			// ligero flash verde para feedback de compra
			long hasta = System.currentTimeMillis() + 70;
			// añade el dato del tiempo en el boton para usar despues en los efectos
			btn.putClientProperty(PROP_FLASH_UNTIL, hasta);
			// se refresca la interfaz para añadior los cambios
			refrescarInterfaz();
		});

		return btn;
	}
//////////////////// fin de crear boton mejora

	// por cada mejora que haya, añade los botones necesarios para que aparezcan
	private void generarFilasDeMejoras() {
		// se limpia infromacion anterior
		panelInferior.removeAll();
		botonesMejoras.clear();
		// bucle para lasmejoras activas
		for (Mejora m : mejorasActivas) {
			RoundedButton btn = crearBotonMejora(m);
			botonesMejoras.add(btn);
			panelInferior.add(btn);
			panelInferior.add(Box.createVerticalStrut(10));
		}
		// bucle para las mejoras pasivas
		for (Mejora m : mejorasPasivas) {
			RoundedButton btn = crearBotonMejora(m);
			botonesMejoras.add(btn);
			panelInferior.add(btn);
			panelInferior.add(Box.createVerticalStrut(10));
		}
		// se renderizan los botonerds
		panelInferior.revalidate();
		panelInferior.repaint();
	}
//////////////////// fin de creacion de filas de mejoras

	private String formatoAbreviado(double n, boolean conDecimales, boolean numPrincipal) {
		final String[] sufijos = { "", "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No" };

		// Por si llega negativo (mantener el signo)
		boolean negativo = n < 0;
		double abs = Math.abs(n);

		// =========================
		// CASO: NÚMERO PRINCIPAL
		// =========================
		if (numPrincipal) {
			// 1) < 1000 => con decimales (2)
			if (abs < 1_000) {
				String s = FORMATO_ENTERO_ES.format(abs);
				return negativo ? "-" + s : s;
			}

			// 2) 1000..99.999.999 => sin abreviar y sin decimales
			if (abs <= 99_999_999) {
				String s = FORMATO_ENTERO_ES.format(abs);
				return negativo ? "-" + s : s;
			}

			// 3) > 99.999.999 => abreviado CON decimales
			String s = abreviarNumeros(abs, true, sufijos);

		}

		// =========================
		// CASO: NO PRINCIPAL
		// =========================
		if (abs < 1_000) {
			String s = conDecimales ? String.format(localeES, "%.2f", abs) : String.format(localeES, "%.0f", abs);
			return negativo ? "-" + s : s;
		}

		// >= 1000 => abrevia (con o sin decimales según conDecimales)
		String s = abreviarNumeros(abs, conDecimales, sufijos);
		return negativo ? "-" + s : s;
	}

	// Abrevia dividiendo entre 1000 y añadiendo sufijo.
	// Si conDecimales=false => 0 decimales
	// Si conDecimales=true => decimales según tamaño (como venías usando)
	private String abreviarNumeros(double n, boolean conDecimales, String[] sufijos) {
		double valor = n;
		int indice = 0;

		while (valor >= 1_000.0 && indice < sufijos.length - 1) {
			valor /= 1_000.0;
			indice++;
		}

		// SIN decimales
		if (!conDecimales) {
			return String.format(localeES, "%.0f%s", valor, sufijos[indice]);
		}

		// 🔥 CON decimales
		// si está abreviado (M, B, T...) → siempre 2 decimales
		if (indice > 0) {
			return String.format(localeES, "%.2f%s", valor, sufijos[indice]);
		}

		// (esto solo pasaría si no hubiera abreviación, por seguridad)
		return String.format(localeES, "%.2f", valor);
	}

	private void actualizarBotonMejora(RoundedButton btn, Mejora m) {
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
		boolean flasheando = tieneFlash && ahora < (Long) v;
		boolean expirado = tieneFlash && ahora >= (Long) v;

		boolean forzar = false;
		if (expirado) {
			btn.putClientProperty(PROP_FLASH_UNTIL, null);
			forzar = true;
		}

		if (!cambio && !flasheando && !forzar)
			return;

		btn.putClientProperty("estado", estado);

		if (!puedeComprar) {
			btn.setIcon(iconoBloqueo);
		} else {
			btn.setIcon(null);
		}

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
	}

	public void refrescarInterfaz() {
		double nps = datos.getNps();
		double npc = datos.getClickIncremento() + nps / 50.0;

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
