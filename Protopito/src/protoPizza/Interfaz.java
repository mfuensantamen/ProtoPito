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

/**
 * Clase con el estilo principal del frente de la aplicación: refresco y
 * generación de elementos de la ventana.
 */
@SuppressWarnings("serial")
public class Interfaz extends JFrame {

	// etiquetas
	private JLabel lblNum;
	private JLabel lblNps;

	// "mini-cache" UI (evita machacar Swing cada frame)
	private long ultimoNumeroMostrado = Long.MIN_VALUE;
	private String ultimoTextoNps = "";

	// iconos pizza
	private ImageIcon iconoPizzaNormal;
	private ImageIcon iconoPizzaGrande;

	// icono slice para NPS
	private ImageIcon iconoSlice;

	// pizza label
	private JLabel etiquetaPizza;

	// FX (floats + halo auto)
	private PizzaFXPane pizzaFX;

	// panel mejoras
	private JPanel panelMejoras;
	private JScrollPane scrollMejoras;

	// listas
	private List<Mejora> mejorasClicker = new ArrayList<>();
	private List<Mejora> mejoras = new ArrayList<>();
	private List<RoundedButton> botonesMejoras = new ArrayList<>();

	// motor
	private Datos datos;

	private final Font fuenteEmoji = new Font("Segoe UI Emoji", Font.BOLD, 16);

	private static final Color BTN_FLASH = new Color(170, 255, 170);
	private static final String PROP_FLASH_UNTIL = "flashUntil";

	private static final Color BTN_VERDE_OK = new Color(200, 255, 200);
	private static final Color BTN_GRIS_NO = new Color(210, 210, 210);
	private static final Color BTN_ROJO_LOCK = new Color(250, 180, 180);

	// icono que se usa cuando una mejora está bloqueada (si existe el recurso)
	private ImageIcon iconoBloqueo;

	// NumberFormat en español sin decimales + separador
	private final NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("es-ES"));
	{
		nf.setMaximumFractionDigits(0);
		nf.setMinimumFractionDigits(0);
		nf.setGroupingUsed(true);
	}

	public Interfaz(Datos datos, List<Mejora> mejoras, List<Mejora> mejorasClicker) {
		this.datos = (datos != null) ? datos : new Datos();
		this.mejoras = (mejoras != null) ? mejoras : new ArrayList<>();
		this.mejorasClicker = (mejorasClicker != null) ? mejorasClicker : new ArrayList<>();

		construirUI();
		construirMejorasUI();
		conectarEventos();
		refrescarInterfaz();
	}

	/**
	 * Carga un recurso de /img/... y lo escala. Si no existe, devuelve null (y NO
	 * revienta la app).
	 */
	private ImageIcon cargarIconoRecurso(String ruta, int ancho, int alto) {
		try {
			URL url = getClass().getResource(ruta);
			if (url == null) {
				System.err.println("No se encontró el recurso: " + ruta);
				return null;
			}
			BufferedImage img = ImageIO.read(url);
			Image escalada = img.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
			return new ImageIcon(escalada);
		} catch (IOException e) {
			System.err.println("Error cargando recurso: " + ruta);
			e.printStackTrace();
			return null;
		}
	}

	private void construirUI() {
		setTitle("ProtoPizza - Clicker/Incremental");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(600, 850));
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panelSuperior = new JPanel();
		panelSuperior.setPreferredSize(new Dimension(850, 380));
		panelSuperior.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelSuperior.setBackground(new Color(150, 150, 170));
		panelSuperior.setLayout(new BorderLayout(0, 0));
		getContentPane().add(panelSuperior, BorderLayout.NORTH);

		JPanel panelNums = new JPanel();
		panelNums.setBorder(new EmptyBorder(16, 20, 8, 20));
		panelNums.setOpaque(false);
		panelNums.setLayout(new BoxLayout(panelNums, BoxLayout.Y_AXIS));
		panelSuperior.add(panelNums, BorderLayout.CENTER);

		JLabel pieBoton = new JLabel("Pulsa para cocinar Pizzas");
		pieBoton.setForeground(new Color(225, 208, 205));
		pieBoton.setFont(new Font("Segoe UI Emoji", Font.BOLD, 9));
		pieBoton.setHorizontalAlignment(SwingConstants.CENTER);
		pieBoton.setBorder(new EmptyBorder(0, 0, 6, 0));
		panelSuperior.add(pieBoton, BorderLayout.SOUTH);

		lblNum = new JLabel("0");
		lblNum.setForeground(new Color(255, 215, 0));
		lblNum.setFont(new Font("Consolas", Font.BOLD, 46));
		lblNum.setAlignmentX(Component.CENTER_ALIGNMENT);
		panelNums.add(lblNum);

		// --- CARGA RECURSOS ---
		ImageIcon pizza200 = cargarIconoRecurso("/img/pizza.png", 200, 200);
		ImageIcon pizza207 = cargarIconoRecurso("/img/pizza.png", 207, 207);
		iconoPizzaNormal = (pizza200 != null) ? pizza200 : new ImageIcon(); // fallback vacío
		iconoPizzaGrande = (pizza207 != null) ? pizza207 : new ImageIcon();

		iconoSlice = cargarIconoRecurso("/img/pizza_slice.png", 20, 20);

		// icono bloqueo (link)
		iconoBloqueo = cargarIconoRecurso("/img/link.png", 16, 16);

		// NPS (icono + texto)
		lblNps = new JLabel("/s 0");
		lblNps.setForeground(new Color(255, 228, 225));
		lblNps.setFont(fuenteEmoji);
		lblNps.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblNps.setBorder(new EmptyBorder(6, 0, 6, 0));
		lblNps.setIcon(iconoSlice);
		lblNps.setHorizontalTextPosition(SwingConstants.RIGHT);
		lblNps.setIconTextGap(6);
		panelNums.add(lblNps);

		// PIZZA label
		this.etiquetaPizza = new JLabel(this.iconoPizzaNormal);
		this.etiquetaPizza.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		this.etiquetaPizza.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.etiquetaPizza.setPreferredSize(new Dimension(208, 208));

		// FX pane
		pizzaFX = new PizzaFXPane(this.etiquetaPizza);
		pizzaFX.setOpaque(false);

		// halo
		pizzaFX.setHaloColors(new Color(255, 215, 0), new Color(255, 245, 200));
		pizzaFX.setHaloStrokes(12f, 8f);
		pizzaFX.setHaloPadding(2);

		// slice icon en floats
		pizzaFX.setSliceIcon("/img/pizza_slice.png", 22);

		pizzaFX.setAlignmentX(Component.CENTER_ALIGNMENT);
		pizzaFX.setPreferredSize(new Dimension(260, 260));
		pizzaFX.setMaximumSize(new Dimension(280, 280));

		panelNums.add(pizzaFX);

		// zona central mejoras
		panelMejoras = new JPanel();
		panelMejoras.setLayout(new BoxLayout(panelMejoras, BoxLayout.Y_AXIS));
		panelMejoras.setBackground(new Color(245, 245, 245));
		panelMejoras.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 80, 0, 80, new Color(230, 225, 245)),
				BorderFactory.createEmptyBorder(15, 20, 0, 20)));

		scrollMejoras = new JScrollPane(panelMejoras);
		scrollMejoras.setBorder(null);
		getContentPane().add(scrollMejoras, BorderLayout.CENTER);

		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void conectarEventos() {
		feedbackBotonPizza();
	}

	public void notifyAutoClickFX() {
		if (pizzaFX != null) {
			pizzaFX.notifyAutoClickTick();
		}
	}

	private void feedbackBotonPizza() {
		final int feedbackMs = 90;

		etiquetaPizza.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				etiquetaPizza.setIcon(iconoPizzaGrande);

				new Timer(feedbackMs, ev -> {
					etiquetaPizza.setIcon(iconoPizzaNormal);
					((Timer) ev.getSource()).stop();
				}).start();

				double npc = datos.getClickIncremento() + datos.getNps() / 50.0;
				if (pizzaFX != null) {
					pizzaFX.spawnClickFloat(npc);
				}

				datos.click();
				refrescarInterfaz();
			}
		});
	}

	private RoundedButton crearBotonMejora(Mejora m) {
		RoundedButton btn = new RoundedButton("", 40);
		btn.setLayout(new BorderLayout());
		btn.setFocusPainted(false);
		btn.setBackground(BTN_GRIS_NO);
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
		btn.setPreferredSize(new Dimension(10, 56));
		btn.setAlignmentX(Component.CENTER_ALIGNMENT);
		btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));

		// icono fijo (izquierda)
		JLabel lblIcon = new JLabel();
		lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
		lblIcon.setVerticalAlignment(SwingConstants.CENTER);
		lblIcon.setPreferredSize(new Dimension(62, 44));

		JLabel lblLeft = new JLabel();
		lblLeft.setFont(fuenteEmoji);
		lblLeft.setHorizontalAlignment(SwingConstants.LEFT);
		lblLeft.setVerticalAlignment(SwingConstants.CENTER);

		JLabel lblRight = new JLabel();
		lblRight.setFont(fuenteEmoji);
		lblRight.setHorizontalAlignment(SwingConstants.RIGHT);
		lblRight.setVerticalAlignment(SwingConstants.CENTER);
		lblRight.setPreferredSize(new Dimension(65, 34));

		btn.add(lblIcon, BorderLayout.WEST);
		btn.add(lblLeft, BorderLayout.CENTER);
		btn.add(lblRight, BorderLayout.EAST);

		btn.putClientProperty("icon", lblIcon);
		btn.putClientProperty("left", lblLeft);
		btn.putClientProperty("right", lblRight);

		btn.addActionListener(ev -> {
			if (!datos.verificarCompra(m)) {
				return;
			}

			m.comprar(datos);

			long hasta = System.currentTimeMillis() + 70;
			btn.putClientProperty(PROP_FLASH_UNTIL, hasta);

			refrescarInterfaz();
		});

		return btn;
	}

	private void construirMejorasUI() {
		panelMejoras.removeAll();
		botonesMejoras.clear();

		for (Mejora m : mejorasClicker) {
			RoundedButton btn = crearBotonMejora(m);
			botonesMejoras.add(btn);
			panelMejoras.add(btn);
			panelMejoras.add(Box.createVerticalStrut(10));
		}

		for (Mejora m : mejoras) {
			RoundedButton btn = crearBotonMejora(m);
			botonesMejoras.add(btn);
			panelMejoras.add(btn);
			panelMejoras.add(Box.createVerticalStrut(10));
		}

		panelMejoras.revalidate();
		panelMejoras.repaint();
	}

	private String formatAbreviado(double n) {
		final String[] sufijos = { "", "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No" };

		if (n < 100) {
			return String.format(Locale.US, "%.2f", n);
		}

		double valor = n;
		int indice = 0;

		while (valor >= 1_000.0 && indice < sufijos.length - 1) {
			valor /= 1_000.0;
			indice++;
		}

		if (valor >= 999.5 && indice < sufijos.length - 1) {
			valor /= 1_000.0;
			indice++;
		}

		if (valor >= 100)
			return String.format(Locale.US, "%.0f%s", valor, sufijos[indice]);
		if (valor >= 10)
			return String.format(Locale.US, "%.1f%s", valor, sufijos[indice]);
		return String.format(Locale.US, "%.2f%s", valor, sufijos[indice]);
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
			left.setText("            Requiere " + formatAbreviado(m.getCoste()));
			right.setText("");
			left.setFont(new Font("Ebrima", Font.BOLD, 15));
			btn.setBackground(flasheando ? BTN_FLASH : BTN_ROJO_LOCK);
			btn.setEnabled(false);
			btn.setCursor(Cursor.getDefaultCursor());
			return;
		}

		ImageIcon ico = cargarIconoRecurso(m.getIconPath(), 32, 32);
		icon.setIcon(ico);

		left.setText(m.getNombre() + "  [ " + nivel + " ]");
		left.setFont(new Font("Ebrima", Font.BOLD, 15));
		right.setText(formatAbreviado((long) m.getCoste()));
		right.setFont(new Font("Ebrima", Font.BOLD, 15));

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

		long num = (long) datos.getNum();
		if (num != ultimoNumeroMostrado) {
			ultimoNumeroMostrado = num;
			lblNum.setText(nf.format(num));
		}

		String npsBase = "/s " + formatAbreviado(nps);

		double periodoAuto = datos.getPeriodoAutoClicker();
		String texto;
		if (datos.getNivelAutoClicker() == 0) {
			texto = npsBase;
		} else {
			texto = npsBase
					+ String.format(Locale.US, "  |  Cocineros +%s cada %.2fs", formatAbreviado(npc), periodoAuto);
		}

		if (!texto.equals(ultimoTextoNps)) {
			ultimoTextoNps = texto;
			lblNps.setText(texto);
		}

		if (datos.autoClickerPulsado()) {
			notifyAutoClickFX();
		}

		int idx = 0;
		for (Mejora m : mejorasClicker) {
			actualizarBotonMejora(botonesMejoras.get(idx++), m);
		}
		for (Mejora m : mejoras) {
			actualizarBotonMejora(botonesMejoras.get(idx++), m);
		}
	}
}
