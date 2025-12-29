package protoPizza;

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
import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import java.io.File;
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

@SuppressWarnings("serial")
public class Interfaz extends JFrame {

	// etiquetas
	private JLabel lblNum;
	private JLabel lblNps;

	// cache UI (evita trabajo pesado cada frame)
	private long lastShownNum = Long.MIN_VALUE;
	private String lastShownNpsText = "";
	private boolean npsIconConfigured = false;

	// iconos pizza
	private ImageIcon iconNormal;
	private ImageIcon iconBig;

	// icono slice para NPS
	private ImageIcon pizzaSliceIcon;

	// pizza label
	private JLabel pizzaLabel;

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

	private final Font emoji = new Font("Segoe UI Emoji", Font.BOLD, 16);

	private static final Color BTN_FLASH = new Color(170, 255, 170);
	private static final String PROP_FLASH_UNTIL = "flashUntil";

	private static final Color BTN_VERDE_OK = new Color(200, 255, 200);
	private static final Color BTN_GRIS_NO = new Color(210, 210, 210);
	private static final Color BTN_ROJO_LOCK = new Color(250, 180, 180);

	private static final ImageIcon ICON_LOCK = new ImageIcon(Interfaz.class.getResource("/img/link.png"));

	// cache iconos mejoras
	private final Map<String, ImageIcon> iconCache = new HashMap<>();

	private ImageIcon loadIcon(String path, int size) {
		if (path == null || path.isBlank())
			return null;

		String normalized = path.startsWith("/") ? path : ("/" + path);
		String key = normalized + "@" + size;

		ImageIcon cached = iconCache.get(key);
		if (cached != null)
			return cached;

		try {
			var url = getClass().getResource(normalized);
			if (url != null) {
				BufferedImage img = ImageIO.read(url);
				Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
				ImageIcon icon = new ImageIcon(scaled);
				iconCache.put(key, icon);
				return icon;
			}

			// fallback file (solo para debug local)
			File f = new File(path);
			if (f.exists()) {
				BufferedImage img = ImageIO.read(f);
				Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
				ImageIcon icon = new ImageIcon(scaled);
				iconCache.put(key, icon);
				System.err.println("Icono cargado por File (debug): " + f.getAbsolutePath());
				return icon;
			}

			System.err.println("❌ Icono NO encontrado: " + path);
			return null;

		} catch (IOException e) {
			System.err.println("❌ Error leyendo icono: " + path);
			e.printStackTrace();
			return null;
		}
	}

	// NumberFormat en español sin decimales + separador
	private final NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("es-ES"));
	{
		nf.setMaximumFractionDigits(0);
		nf.setMinimumFractionDigits(0);
		nf.setGroupingUsed(true);
	}

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

		// --- CARGA RECURSOS (ANTES de usar icons) ---
		var urlPizza = getClass().getResource("/img/pizza.png");
		if (urlPizza == null)
			throw new RuntimeException("No se encuentra /img/pizza.png");

		var urlSlice = getClass().getResource("/img/pizza_slice.png");
		if (urlSlice == null)
			throw new RuntimeException("No se encuentra /img/pizza_slice.png");

		BufferedImage sliceImg = ImageIO.read(urlSlice);
		pizzaSliceIcon = new ImageIcon(sliceImg.getScaledInstance(20, 20, Image.SCALE_SMOOTH));

		BufferedImage pizzaImg = ImageIO.read(urlPizza);
		this.iconNormal = new ImageIcon(pizzaImg.getScaledInstance(200, 200, Image.SCALE_SMOOTH));
		this.iconBig = new ImageIcon(pizzaImg.getScaledInstance(207, 207, Image.SCALE_SMOOTH));

		// NPS (icono + texto)
		lblNps = new JLabel("/s 0,00");
		lblNps.setForeground(new Color(255, 228, 225));
		lblNps.setFont(emoji);
		lblNps.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblNps.setBorder(new EmptyBorder(6, 0, 6, 0));
		lblNps.setIcon(pizzaSliceIcon);
		lblNps.setHorizontalTextPosition(SwingConstants.RIGHT);
		lblNps.setIconTextGap(6);
		panelNums.add(lblNps);

		// PIZZA label
		this.pizzaLabel = new JLabel(this.iconNormal);
		this.pizzaLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		this.pizzaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.pizzaLabel.setPreferredSize(new Dimension(208, 208));

		// FX pane
		pizzaFX = new PizzaFXPane(this.pizzaLabel);
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
				BorderFactory.createMatteBorder(0, 80, 0, 80, new Color(230, 225, 245)), // <-- antes 60
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
		if (pizzaFX != null)
			pizzaFX.notifyAutoClickTick();
	}

	private void feedbackBotonPizza() {
		int feedbackMs = 90;

		pizzaLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				pizzaLabel.setIcon(iconBig);
				pizzaFX.revalidate();
				pizzaFX.repaint();

				new Timer(feedbackMs, ev -> {
					pizzaLabel.setIcon(iconNormal);
					pizzaFX.revalidate();
					pizzaFX.repaint();
					((Timer) ev.getSource()).stop();
				}).start();

				double npc = datos.getClickIncremento() + datos.getNps() / 50.0;
				if (pizzaFX != null)
					pizzaFX.spawnClickFloat(npc);

				datos.click();
				render();
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
		lblIcon.setMinimumSize(new Dimension(62, 44));
		lblIcon.setMaximumSize(new Dimension(62, 44));

		JLabel lblLeft = new JLabel();
		lblLeft.setFont(emoji);
		lblLeft.setHorizontalAlignment(SwingConstants.LEFT);
		lblLeft.setVerticalAlignment(SwingConstants.CENTER);

		JLabel lblRight = new JLabel();
		lblRight.setFont(emoji);
		lblRight.setHorizontalAlignment(SwingConstants.RIGHT);
		lblRight.setVerticalAlignment(SwingConstants.CENTER);
		lblRight.setPreferredSize(new Dimension(65, 34)); // <-- antes 110
		lblRight.setMinimumSize(new Dimension(65, 34));
		lblRight.setMaximumSize(new Dimension(65, 34));

		// ✅ ORDEN CORRECTO
		btn.add(lblIcon, BorderLayout.WEST);
		btn.add(lblLeft, BorderLayout.CENTER);
		btn.add(lblRight, BorderLayout.EAST);

		btn.putClientProperty("icon", lblIcon);
		btn.putClientProperty("left", lblLeft);
		btn.putClientProperty("right", lblRight);

		btn.addActionListener(ev -> {
			if (!datos.verificarCompra(m.getCoste()))
				return;

			m.comprar(datos);

			long until = System.currentTimeMillis() + 70;
			btn.putClientProperty(PROP_FLASH_UNTIL, until);

			render();
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

	private void actualizarBotonMejora(RoundedButton btn, Mejora m) {
		JLabel icon = (JLabel) btn.getClientProperty("icon");
		JLabel left = (JLabel) btn.getClientProperty("left");
		JLabel right = (JLabel) btn.getClientProperty("right");

		// --- Estado actual ---
		boolean unlock = m.desbloquado(datos.getMaximo());
		long coste = (long) m.getCoste();
		int nivel = m.getNivel();
		boolean canBuy = unlock && datos.verificarCompra(m.getCoste());

		// Guardamos un snapshot mínimo para no machacar Swing cada frame
		String state = unlock + "|" + canBuy + "|" + nivel + "|" + coste;
		Object prev = btn.getClientProperty("state");
		boolean stateChanged = !state.equals(prev);

		// --- Flash de compra (tiene prioridad visual) ---
		Object v = btn.getClientProperty(PROP_FLASH_UNTIL);
		long now = System.currentTimeMillis();

		boolean hasFlash = (v instanceof Long);
		boolean flashing = hasFlash && now < (Long) v;
		boolean expired = hasFlash && now >= (Long) v;

		// Si acaba de expirar, limpiamos la prop y FORZAMOS 1 actualización
		boolean forceRefresh = false;
		if (expired) {
			btn.putClientProperty(PROP_FLASH_UNTIL, null);
			forceRefresh = true;
		}

		if (!stateChanged && !flashing && !forceRefresh) {
			return; // nada que actualizar
		}

		btn.putClientProperty("state", state);

		if (!canBuy) {
			if (btn.getIcon() != ICON_LOCK) {
				btn.setIcon(ICON_LOCK);
			}
		} else {
			if (btn.getIcon() != null) {
				btn.setIcon(null);
			}
		}

		if (!unlock) {
			// Locked
			if (icon.getIcon() != null)
				icon.setIcon(null);
			left.setText("            Requiere " + formatAbreviado((long) m.getCoste()));
			ImageIcon ico = loadIcon("/img/link.png", 32);
			icon.setIcon(ico);
			right.setText("");
			left.setFont(new Font("Ebrima", Font.BOLD, 15));
			btn.setBackground(flashing ? BTN_FLASH : BTN_ROJO_LOCK);
			btn.setEnabled(false);
			btn.setCursor(Cursor.getDefaultCursor());
			return;
		}

		// Icono (cacheado por ruta+tamaño en loadIcon)
		String iconKey = m.getIconPath();
		Object prevIconKey = btn.getClientProperty("iconKey");
		if (iconKey == null)
			iconKey = "";
		if (!iconKey.equals(prevIconKey)) {
			ImageIcon ico = loadIcon(m.getIconPath(), 32);
			icon.setIcon(ico);
			btn.putClientProperty("iconKey", iconKey);
		}

		left.setText(m.getNombre() + "  [ " + nivel + " ]");
		left.setFont(new Font("Ebrima", Font.BOLD, 15));
		right.setText(formatAbreviado((long) m.getCoste()));
		right.setFont(new Font("Ebrima", Font.BOLD, 15));

		if (flashing) {
			btn.setBackground(BTN_FLASH);
		} else if (canBuy) {
			btn.setBackground(BTN_VERDE_OK);
		} else {
			btn.setBackground(BTN_GRIS_NO);
		}

		btn.setEnabled(canBuy);
		btn.setCursor(canBuy ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
	}

	public void render() {
		if (botonesMejoras.isEmpty())
			return;

		// --- Número principal (solo si cambia) ---
		long num = (long) datos.getNum();
		if (num != lastShownNum) {
			lastShownNum = num;
			lblNum.setText(nf.format(num));
		}

		// --- NPS: configurar icono 1 vez ---
		if (!npsIconConfigured && pizzaSliceIcon != null) {
			npsIconConfigured = true;
			lblNps.setIcon(pizzaSliceIcon);
			lblNps.setHorizontalTextPosition(SwingConstants.RIGHT);
			lblNps.setIconTextGap(6);
		}

		String npsBase = String.format("/s %.2f ", datos.getNps());

		double npc = datos.getClickIncremento() + datos.getNps() / 50;
		double npcAuto = datos.getPeriodoAutoClicker();

		String npsText;
		if (datos.getNivelAutoClicker() == 0) {
			npsText = npsBase;
		} else {
			npsText = npsBase + String.format("  |  Cocineros +%.2f cada %.2fs ", npc, npcAuto);
		}

		if (!npsText.equals(lastShownNpsText)) {
			lastShownNpsText = npsText;
			lblNps.setText(npsText);
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
