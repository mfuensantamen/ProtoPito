package protoPizzaa;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

/**
 * Ventana principal del juego (UI + coordinador).
 * <p>
 * Esta clase crea y organiza los componentes Swing, conecta eventos (click en
 * pizza y compra de mejoras) y "renderiza" los valores del modelo
 * {@link Datos}.
 *
 * <h2>Idea clave</h2>
 * <ul>
 * <li>El estado vive en {@link Datos}.</li>
 * <li>Las mejoras son instancias de {@link Mejora}.</li>
 * <li>La interfaz se refresca con {@link #render()} en cada tick del
 * Timer.</li>
 * </ul>
 */
@SuppressWarnings("serial")
public class Interfaz extends JFrame {

	// etiquetas
	/** Label principal que muestra el número de pizzas actuales. */
	private JLabel lblNum;
	/** Label que muestra la producción pasiva (pizzas por segundo). */
	private JLabel lblNps;
//	private JLabel lblNpc;

	// imagen clicker
	/** Icono normal (pizza a tamaño estándar). */
	ImageIcon iconNormal;
	/** Icono al hacer click (pizza ligeramente más grande) para feedback. */
	ImageIcon iconBig;
	/** Label que contiene la imagen de la pizza (se coloca en el centro). */
	private JLabel pizzaLabel;

	// capa de FX (floats + halo auto)
	/** Capa de efectos alrededor de la pizza (halo + textos flotantes). */
	private PizzaFXPane pizzaFX;

	// panel
	/** Panel contenedor de la lista de mejoras (con scroll). */
	private JPanel panelMejoras;
	// seccion con scroll
	/** Scroll que envuelve a {@link #panelMejoras}. */
	private JScrollPane scrollMejoras;
	// listas
	/**
	 * Lista de mejoras activas (click). Se usa para construir UI y actualizarla en
	 * render().
	 */
	private List<Mejora> mejorasClicker = new ArrayList<>();
	/**
	 * Lista de mejoras pasivas (NPS). Se usa para construir UI y actualizarla en
	 * render().
	 */
	private List<Mejora> mejoras = new ArrayList<>();
	/** Labels que muestran coste o requisito (uno por mejora). */
	private List<JLabel> labelsCoste = new ArrayList<>();
	/**
	 * Botones de compra (uno por mejora) para habilitar/deshabilitar según fondos.
	 */
	private List<JButton> botonesCompra = new ArrayList<>();
	// motor
	/** Modelo del juego (estado numérico y lógica base). */
	private Datos datos;

	/** Fuente emoji usada para textos con 🍕 y candados. */
	Font emoji = new Font("Segoe UI Emoji", Font.BOLD, 16);

	// colores centralizados para estados del boton
	private static final Color BTN_VERDE_OK = new Color(200, 255, 200);
	private static final Color BTN_GRIS_NO = new Color(210, 210, 210);
	private static final Color BTN_ROJO_LOCK = new Color(250, 180, 180);

	// NumberFormat en español sin decimales + con separador de miles
	/**
	 * Formateador numérico (locale es-ES) para mostrar decimales de forma
	 * consistente.
	 */
	private final NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("es-ES"));
	{
		nf.setMaximumFractionDigits(0);
		nf.setMinimumFractionDigits(0);
		nf.setGroupingUsed(true);
	}

	/**
	 * Construye la ventana principal.
	 *
	 * @param datos          modelo del juego
	 * @param mejoras        lista de mejoras pasivas
	 * @param mejorasClicker lista de mejoras activas
	 * @throws IOException si falla la carga de recursos (pizza.png)
	 */
	public Interfaz(Datos datos, List<Mejora> mejoras, List<Mejora> mejorasClicker) throws IOException {
		this.datos = datos;
		this.mejoras = mejoras;
		this.mejorasClicker = mejorasClicker;
		construirUI();
		construirMejorasUI();
		conectarEventos();
		render();
	}

	private void construirUI() throws IOException {
		setTitle("ProtoPito - Incremental");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(600, 850));

		getContentPane().setLayout(new BorderLayout(0, 0));

		// panel de arriba
		JPanel panelSuperior = new JPanel();
		panelSuperior.setPreferredSize(new Dimension(850, 380));
		panelSuperior.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelSuperior.setBackground(new Color(150, 150, 170));
		panelSuperior.setLayout(new BorderLayout(0, 0));
		getContentPane().add(panelSuperior, BorderLayout.NORTH);

		// contenedor para num + nps + pizza (CENTRO)
		JPanel panelNums = new JPanel();
		panelNums.setBorder(new EmptyBorder(16, 20, 8, 20)); // CAMBIO: menos padding abajo
		panelNums.setOpaque(false);
		panelNums.setLayout(new BoxLayout(panelNums, BoxLayout.Y_AXIS));
		panelSuperior.add(panelNums, BorderLayout.CENTER);

		// CAMBIO: footer abajo del todo (SOUTH), no ocupa hueco en el centro
		JLabel pieBoton = new JLabel("Pulsa para cocinar Pizzas");
		pieBoton.setForeground(new Color(225, 208, 205));
		pieBoton.setFont(new Font("Segoe UI Emoji", Font.BOLD, 9)); // CAMBIO: más pequeño
		pieBoton.setHorizontalAlignment(SwingConstants.CENTER);
		pieBoton.setBorder(new EmptyBorder(0, 0, 6, 0)); // CAMBIO: pegado al borde inferior
		panelSuperior.add(pieBoton, BorderLayout.SOUTH);

		// etiqueta num
		lblNum = new JLabel("0");
		lblNum.setForeground(new Color(255, 215, 0));
		lblNum.setFont(new Font("Consolas", Font.BOLD, 46));
		lblNum.setAlignmentX(Component.CENTER_ALIGNMENT);
		panelNums.add(lblNum);

		// etiqueta nps
		lblNps = new JLabel("ERROR");
		lblNps.setForeground(new Color(255, 228, 225));
		lblNps.setFont(emoji);
		lblNps.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblNps.setBorder(new EmptyBorder(6, 0, 6, 0));

//		lblNpc = new JLabel("ERROR");
//		lblNpc.setForeground(new Color(255, 228, 225));
//		lblNpc.setFont(emoji);
//		lblNpc.setAlignmentX(Component.CENTER_ALIGNMENT);
//		lblNpc.setBorder(new EmptyBorder(6, 0, 6, 0));

		panelNums.add(lblNps);
//		panelNums.add(lblNpc);

		// PIZZA
		var url = getClass().getResource("/pizza.png");
		if (url == null)
			throw new RuntimeException("No se encuentra el archivo pizza.png");

		BufferedImage pizzaImg = ImageIO.read(url);
		this.iconNormal = new ImageIcon(pizzaImg.getScaledInstance(200, 200, Image.SCALE_SMOOTH));
		this.iconBig = new ImageIcon(pizzaImg.getScaledInstance(207, 207, Image.SCALE_SMOOTH));

		this.pizzaLabel = new JLabel(this.iconNormal);
		this.pizzaLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		this.pizzaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.pizzaLabel.setPreferredSize(new Dimension(208, 208));

		// FX Pane (floats + halo)
		pizzaFX = new PizzaFXPane(this.pizzaLabel);
		pizzaFX.setOpaque(false);

		// CAMBIO: halo más cerca, amarillo, y más ancho
		pizzaFX.setHaloColors(new Color(255, 215, 0), new Color(255, 245, 200));
		pizzaFX.setHaloStrokes(12f, 8f);
		pizzaFX.setHaloPadding(2);

		pizzaFX.setAlignmentX(Component.CENTER_ALIGNMENT);
		pizzaFX.setPreferredSize(new Dimension(260, 260)); // CAMBIO: evita corte abajo + ajusta al halo
		pizzaFX.setMaximumSize(new Dimension(280, 280));

		panelNums.add(pizzaFX);

		// zona central de mejoras
		panelMejoras = new JPanel();
		panelMejoras.setLayout(new BoxLayout(panelMejoras, BoxLayout.Y_AXIS));
		panelMejoras.setBorder(new EmptyBorder(15, 15, 15, 15));
		panelMejoras.setBackground(new Color(245, 245, 245));

		scrollMejoras = new JScrollPane(panelMejoras);
		scrollMejoras.setBorder(null);
		getContentPane().add(scrollMejoras, BorderLayout.CENTER);

		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void conectarEventos() {
		feedbackBotonPizza();
		render();
	}

	/**
	 * Dispara el halo/efecto visual asociado al auto-clicker. Se invoca cuando el
	 * modelo indica que ha ocurrido un auto-click.
	 */
	public void notifyAutoClickFX() {
		if (pizzaFX != null)
			pizzaFX.notifyAutoClickTick();
	}

	/**
	 * Añade el listener principal al click sobre la pizza.
	 * <p>
	 * En mousePressed:
	 * <ul>
	 * <li>llama a {@link Datos#click()}</li>
	 * <li>calcula pizzasPorClick (NPC efectivo)</li>
	 * <li>crea el float "+X 🍕" en {@link PizzaFXPane}</li>
	 * </ul>
	 */
	public void feedbackBotonPizza() {
		int feedbackMs = 90;

		pizzaLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				pizzaLabel.setIcon(iconBig);
				pizzaLabel.revalidate();
				pizzaLabel.repaint();

				new Timer(feedbackMs, ev -> {
					pizzaLabel.setIcon(iconNormal);
					pizzaLabel.revalidate();
					pizzaLabel.repaint();
					((Timer) ev.getSource()).stop();
				}).start();

				// float click manual
				double npc = datos.getClickIncremento() + datos.getNps() / 50.0;
				if (pizzaFX != null)
					pizzaFX.spawnClickFloat(npc);

				datos.click();
				render();
			}
		});
	}

	// filas de mejoras
	private void construirMejorasUI() {
		panelMejoras.removeAll();
		labelsCoste.clear();
		botonesCompra.clear();

		for (int i = 0; i < mejorasClicker.size(); i++) {
			Mejora m2 = mejorasClicker.get(i);

			FlashPanel fila = new FlashPanel(new Color(245, 245, 245));
			fila.setLayout(new BorderLayout(10, 0));
			fila.setBorder(new EmptyBorder(4, 4, 4, 4));

			JLabel lblCoste = new JLabel("ERROR");
			lblCoste.setFont(emoji);
			lblCoste.setForeground(Color.DARK_GRAY);

			RoundedButton btnCompra = new RoundedButton(nf.format(m2.getCoste()), 14);
			btnCompra.setFont(emoji);
			btnCompra.setFocusPainted(false);
			btnCompra.setBackground(BTN_VERDE_OK);
			btnCompra.setPreferredSize(new Dimension(72, 34));
			btnCompra.setMaximumSize(new Dimension(72, 34));
			btnCompra.setHorizontalAlignment(SwingConstants.CENTER);
			btnCompra.setVerticalAlignment(SwingConstants.CENTER);
			btnCompra.setMargin(new Insets(0, 12, 0, 12));

			btnCompra.addActionListener(ev -> {
				if (datos.verificarCompra(m2.getCoste())) {
					m2.comprar(datos);
					fila.flash(new Color(210, 255, 210), 120);
					render();
				}
			});

			labelsCoste.add(lblCoste);
			botonesCompra.add(btnCompra);

			fila.add(lblCoste, BorderLayout.CENTER);
			fila.add(btnCompra, BorderLayout.EAST);

			panelMejoras.add(fila);
			panelMejoras.add(Box.createVerticalStrut(6));
		}

		for (int i = 0; i < mejoras.size(); i++) {
			Mejora m = mejoras.get(i);

			FlashPanel fila = new FlashPanel(new Color(245, 245, 245));
			fila.setLayout(new BorderLayout(10, 0));
			fila.setBorder(new EmptyBorder(4, 4, 4, 4));

			JLabel lblCoste = new JLabel("ERROR");
			lblCoste.setFont(emoji);
			lblCoste.setForeground(Color.DARK_GRAY);

			RoundedButton btnCompra = new RoundedButton(nf.format(m.getCoste()), 14);
			btnCompra.setFont(emoji);
			btnCompra.setFocusPainted(false);
			btnCompra.setBackground(BTN_VERDE_OK);
			btnCompra.setPreferredSize(new Dimension(72, 34));
			btnCompra.setMaximumSize(new Dimension(72, 34));
			btnCompra.setHorizontalAlignment(SwingConstants.CENTER);
			btnCompra.setVerticalAlignment(SwingConstants.CENTER);
			btnCompra.setMargin(new Insets(0, 12, 0, 12));

			btnCompra.addActionListener(ev -> {
				if (datos.verificarCompra(m.getCoste())) {
					m.comprar(datos);
					fila.flash(new Color(210, 255, 210), 120);
					render();
				}
			});

			labelsCoste.add(lblCoste);
			botonesCompra.add(btnCompra);

			fila.add(lblCoste, BorderLayout.CENTER);
			fila.add(btnCompra, BorderLayout.EAST);

			panelMejoras.add(fila);
			panelMejoras.add(Box.createVerticalStrut(6));
		}

		panelMejoras.revalidate();
		panelMejoras.repaint();
	}

	private String formatAbreviado(long n) {
		final String[] sufijos = { "", "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No" };

		if (n < 10_000)
			return String.valueOf(n);

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

	/**
	 * Refresca toda la UI con el estado actual del modelo. Se llama en cada tick
	 * del Timer (aprox. cada 15 ms).
	 */
	public void render() {
		lblNum.setText(nf.format((long) datos.getNum()));
		String nps = String.format("🍕/s %.2f ", datos.getNps());

		double npc = datos.getClickIncremento() + datos.getNps() / 50;
		double npcAuto = datos.getPeriodoAutoClicker();
		if (datos.getNivelAutoClicker() == 0) {
			lblNps.setText(nps);
		} else {
			lblNps.setText(nps + String.format("  |  Cocineros +%.2f🍕 cada %.2fs ", npc, npcAuto));
		}

		// halo autoclick
		if (datos.autoClickerPulsado()) {
			notifyAutoClickFX();
		}

		// Render mejoras clicker
		for (int i = 0; i < mejorasClicker.size(); i++) {
			Mejora m = mejorasClicker.get(i);
			JLabel lbl = labelsCoste.get(i);
			JButton btn = botonesCompra.get(i);

			boolean unlock = m.desbloquado(datos.getMaximo());

			if (!unlock) {
				lbl.setText("🔒 Requiere " + (formatAbreviado((int) m.getCoste())) + " 🍕");
				btn.setText("🔒");
				btn.setBackground(BTN_ROJO_LOCK);
				btn.setEnabled(false);
				btn.setCursor(Cursor.getDefaultCursor());
			} else {
				lbl.setText(String.format("%s [ %d ]", m.getNombre(), m.getNivel()));
				btn.setText(formatAbreviado((long) m.getCoste()));

				boolean canBuy = datos.verificarCompra(m.getCoste());
				if (canBuy) {
					btn.setBackground(BTN_VERDE_OK);
					btn.setEnabled(true);
					btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				} else {
					btn.setBackground(BTN_GRIS_NO);
					btn.setEnabled(false);
					btn.setCursor(Cursor.getDefaultCursor());
				}
			}
		}

		// Render mejoras pasivas
		int offset = mejorasClicker.size();
		for (int i = 0; i < mejoras.size(); i++) {
			Mejora m = mejoras.get(i);
			JLabel lbl = labelsCoste.get(offset + i);
			JButton btn = botonesCompra.get(offset + i);

			boolean unlock = m.desbloquado(datos.getMaximo());

			if (!unlock) {
				lbl.setText("🔒 Requiere " + (formatAbreviado((int) m.getCoste())) + " 🍕");
				btn.setText("🔒");
				btn.setBackground(BTN_ROJO_LOCK);
				btn.setEnabled(false);
				btn.setCursor(Cursor.getDefaultCursor());
			} else {
				lbl.setText(String.format("%s [ %d ]", m.getNombre(), m.getNivel()));
				btn.setText(formatAbreviado((long) m.getCoste()));

				boolean canBuy = datos.verificarCompra(m.getCoste());
				if (canBuy) {
					btn.setBackground(BTN_VERDE_OK);
					btn.setEnabled(true);
					btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				} else {
					btn.setBackground(BTN_GRIS_NO);
					btn.setEnabled(false);
					btn.setCursor(Cursor.getDefaultCursor());
				}
			}
		}
	}
}
