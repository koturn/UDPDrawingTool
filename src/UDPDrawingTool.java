import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.UIManager.*;


/**
 * ウィンドウのクラス
 */
public class UDPDrawingTool extends JFrame implements Runnable {
	private CanvasLabel       canvas;  // 描画領域
	private LookAndFeelInfo[] lf;      // 使用可能ルックアンドフィール
	private DatagramSocket    sendSocket;
	private InetAddress       sendAddress;
	private int               sendPort;
	private DatagramSocket    recvSocket;

	/**
	 * このプログラムのエントリポイント
	 * @param args  コマンドライン引数
	 */
	public static void main(final String[] args) {
		UIManager.put("swing.boldMetal", Boolean.FALSE);
		if (args.length != 3) {
			String[] message = {
				"コマンドライン引数が不正です",
				"[Usage]",
				"  java -jar UDPDrawingTool.jar [ip-address] [port for sending] [port for recieving]"
			};
			JOptionPane.showMessageDialog(null, message, "警告", JOptionPane.WARNING_MESSAGE);
			return;
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				String sendAddressString = args[0];
				int sendPort             = Integer.parseInt(args[1]);
				int recvPort             = Integer.parseInt(args[2]);
				try {
					new UDPDrawingTool("UDPDrawingTool", sendAddressString, sendPort, recvPort).setVisible(true);
				} catch (SocketException ex) {
					ex.printStackTrace();
				} catch (UnknownHostException ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	/**
	 * ウィンドウのコンストラクタ<br>
	 * コンポーネントの配置などを行っている
	 * @param title              ウィンドウのタイトル
	 * @param sendAddressString  送信先IPアドレスの文字列("127.0.0.1" など)
	 * @param sendPort           送信先のポート番号
	 * @param recvPort           受信ポート番号
	 * @throws SocketException      使用しているプロトコルでエラーが発生したことを表す例外
	 * @throws UnknownHostException ホストのIPアドレスが判定できなかった場合にスローされる例外
	 */
	public UDPDrawingTool(String title, String sendAddressString, int sendPort, int recvPort) throws SocketException, UnknownHostException {
		super(title);

		sendSocket    = new DatagramSocket();
		sendAddress   = InetAddress.getByName(sendAddressString);
		this.sendPort = sendPort;
		recvSocket    = new DatagramSocket(recvPort);

		/* ==================== メニューバーについて ==================== */
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu[] menus = {
			new JMenu("ファイル(F)"),
			new JMenu("設定(S)"),
		};
		menuBar.add(menus[0]);
		menuBar.add(menus[1]);
		menus[0].setMnemonic(KeyEvent.VK_F);
		menus[1].setMnemonic(KeyEvent.VK_S);

		JMenuItem[] menuItems0 = {
			new JMenuItem("画像ファイルを読み込む"),
			new JMenuItem("保存する"),
		};
		menus[0].add(menuItems0[0]);
		menuItems0[0].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
		menuItems0[0].addActionListener(new ImageFileChooseEventHandler());

		menus[0].add(menuItems0[1]);
		menuItems0[1].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
		menuItems0[1].addActionListener(new ImageFileSaveEventHandler());

		JMenu menu = new JMenu("Look&Feel");
		menus[1].add(menu);
		lf = UIManager.getInstalledLookAndFeels();
		JCheckBoxMenuItem[] LFMenuItems = new JCheckBoxMenuItem[lf.length];
		LFChangeEventHandler lfceh = new LFChangeEventHandler();
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0; i < lf.length; i++) {
			LFMenuItems[i] = new JCheckBoxMenuItem(lf[i].getName());
			LFMenuItems[i].addActionListener(lfceh);
			bg.add(LFMenuItems[i]);
			menu.add(LFMenuItems[i]);
		}
		LFMenuItems[0].setSelected(true);
		/* ============================================================== */


		Container container = getContentPane();
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2));
		container.add(panel, BorderLayout.SOUTH);

		JButton bt = new JButton("Color");
		bt.addActionListener(new ColorChooseEventHandler());
		panel.add(bt);

		bt = new JButton("線の太さ");
		bt.addActionListener(new ThicknessChooseEventHandler());
		panel.add(bt);

		canvas = new CanvasLabel();
		container.add(canvas, BorderLayout.CENTER);

		setIconImage(getImageIconInJar("resource/star.png").getImage());
		setBounds(this.getX() + 100, this.getY() + 100, 400, 400);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		new Thread(this).start();
	}

	/**
	 * 受信用のスレッドのタスク
	 */
	@Override
	public void run() {
		byte[] buf = new byte[8192];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		try {
			// ByteBuffer byteBuffer = ByteBuffer.allocate(8);
			boolean startFlag = true;
			int x1 = 0;
			int y1 = 0;
			int x2 = 0;
			int y2 = 0;
			while (true) {
				recvSocket.receive(packet);
				String msg = new String(buf, 0, packet.getLength());
				x2 = Integer.parseInt(msg);
				if (x2 == -1) {
					recvSocket.receive(packet);
					msg = new String(buf, 0, packet.getLength());
					Color color = new Color(Integer.parseInt(msg));

					recvSocket.receive(packet);
					msg = new String(buf, 0, packet.getLength());
					BasicStroke wideStroke = new BasicStroke(Float.parseFloat(msg));

					UDPDrawingTool.this.canvas.canvasGraphics.setStroke(wideStroke);
					UDPDrawingTool.this.canvas.canvasGraphics.setColor(color);

					startFlag = true;
					continue;
				}
				recvSocket.receive(packet);
				msg = new String(buf, 0, packet.getLength());
				y2 = Integer.parseInt(msg);

				if (startFlag) {
					startFlag = false;
				} else {
					canvas.canvasGraphics.drawLine(x1, y1, x2, y2);
					canvas.repaint();
				}
				x1 = x2;
				y1 = y2;
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	/**
	 * Jarファイル内の画像ファイルのImageIconアイコンを得る。
	 * Jarファイル内にファイルが存在しない場合、カレントディレクトリから、
	 * 同一指定先を探す。
	 * それでも無ければ、返り値はnullである。
	 * @param filePath  Jarファイルにおける画像へのファイルパス
	 * @return  Jarファイル内のpngファイルのImageIconオブジェクト
	 */
	public ImageIcon getImageIconInJar(String filePath) {
		ClassLoader cl = getClass().getClassLoader();
		URL url = cl.getResource(filePath);
		if (url != null) {
			return new ImageIcon(url);
		}
		return new ImageIcon(filePath);
	}

	/**
	 * ペイントの描画領域のクラス
	 */
	private class CanvasLabel extends JLabel implements MouseListener, MouseMotionListener {
		private int x1;
		private int y1;
		private int x2;
		private int y2;
		private Color color;             // 描画する線の色
		private BasicStroke wideStroke;  // 描画する線の太さ
		private BufferedImage canvasImage;
		private Graphics2D canvasGraphics;
		CanvasLabel() {
			addMouseListener(this);
			addMouseMotionListener(this);

			color = Color.BLACK;
			wideStroke = new BasicStroke(1.0f);
			canvasImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
			canvasGraphics = canvasImage.createGraphics();
			canvasGraphics.fillRect(0, 0, canvasImage.getWidth(), canvasImage.getHeight());
			repaint();
		}
		@Override
		public void paintComponent(Graphics g){
			g.drawImage(canvasImage, 0, 0, null);
		}
		@Override
		public void mouseClicked(MouseEvent ev) {
			// Do nothing
		}
		@Override
		public void mouseEntered(MouseEvent ev) {
			// Do nothing
		}
		@Override
		public void mouseExited(MouseEvent ev) {
			// Do nothing
		}
		@Override
		public void mousePressed(MouseEvent ev) {
			x1 = ev.getX();
			y1 = ev.getY();
			canvasGraphics.setStroke(wideStroke);
			canvasGraphics.setColor(color);

			try {
				byte[] buf = (-1 + "").getBytes();
				DatagramPacket packet = new DatagramPacket(buf, buf.length, UDPDrawingTool.this.sendAddress, UDPDrawingTool.this.sendPort);
				sendSocket.send(packet);

				buf = (color.getRGB() + "").getBytes();
				packet = new DatagramPacket(buf, buf.length, UDPDrawingTool.this.sendAddress, UDPDrawingTool.this.sendPort);
				sendSocket.send(packet);

				buf = (wideStroke.getLineWidth() + "").getBytes();
				packet = new DatagramPacket(buf, buf.length, UDPDrawingTool.this.sendAddress, UDPDrawingTool.this.sendPort);
				sendSocket.send(packet);

				buf = (x1 + "").getBytes();
				packet= new DatagramPacket(buf, buf.length, UDPDrawingTool.this.sendAddress, UDPDrawingTool.this.sendPort);
				sendSocket.send(packet);

				buf = (y1 + "").getBytes();
				packet = new DatagramPacket(buf, buf.length, UDPDrawingTool.this.sendAddress, UDPDrawingTool.this.sendPort);
				sendSocket.send(packet);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		@Override
		public void mouseReleased(MouseEvent ev) {
			// Do nothing
		}
		@Override
		public void mouseDragged(MouseEvent ev) {
			x2 = x1;
			y2 = y1;
			x1 = ev.getX();
			y1 = ev.getY();
			canvasGraphics.drawLine(x2, y2, x1, y1);
			try {
				byte[] buf = (x1 + "").getBytes();
				DatagramPacket packet = new DatagramPacket(buf, buf.length, UDPDrawingTool.this.sendAddress, UDPDrawingTool.this.sendPort);
				sendSocket.send(packet);

				buf = (y1 + "").getBytes();
				packet = new DatagramPacket(buf, buf.length, UDPDrawingTool.this.sendAddress, UDPDrawingTool.this.sendPort);
				sendSocket.send(packet);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			repaint();
		}
		@Override
		public void mouseMoved(MouseEvent ev) {
			// Do nothing
		}
	}

	/**
	 * 色の選択を行うクラス
	 */
	private class ColorChooseEventHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			Color newColor = JColorChooser.showDialog(UDPDrawingTool.this, "色の選択", UDPDrawingTool.this.canvas.color);
			if (newColor != null) {
				UDPDrawingTool.this.canvas.color = newColor;
			}
		}
	}

	/**
	 * 線の太さの選択を行うクラス
	 */
	private class ThicknessChooseEventHandler implements ActionListener {
		private JSpinner js;
		private float value;
		ThicknessChooseEventHandler() {
			SpinnerNumberModel snm = new SpinnerNumberModel(1.0, 1.0, 10.0, 0.5);
			js = new JSpinner(snm);
			value = 1;
		}
		@Override
		public void actionPerformed(ActionEvent ev) {
			js.setValue(value);
			int ret = JOptionPane.showConfirmDialog(UDPDrawingTool.this, js, "線の太さ", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (ret != JOptionPane.YES_OPTION) return;
			try {
				value = Float.parseFloat(js.getValue().toString());
				UDPDrawingTool.this.canvas.wideStroke = new BasicStroke(value);
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(UDPDrawingTool.this, "無効な値です", "警告", JOptionPane.WARNING_MESSAGE);
			}
		}
	}


	/**
	 * 画像ファイルを開くイベントを行うクラス
	 */
	private class ImageFileChooseEventHandler implements ActionListener {
		private String[] suffixes = {".png", ".jpg", ".jpeg", ".jpe", ".jfif", ".gif"};
		@Override
		public void actionPerformed(ActionEvent ev) {
			JFileChooser fc = new JFileChooser(".");
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setDialogTitle("ファイル選択");
			fc.setFileFilter(new FileChooserFilter("画像ファイル", suffixes));
			int ret = fc.showOpenDialog(UDPDrawingTool.this);
			if (ret != JFileChooser.APPROVE_OPTION) {
				return;
			}
			String filePath = fc.getSelectedFile().getAbsolutePath();
			try {
				BufferedImage image = ImageIO.read(new File(filePath));
				UDPDrawingTool.this.canvas.canvasGraphics.drawImage(image, 0, 0, null);
				repaint();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * キャンバスを画像ファイルとして保存するクラス
	 */
	private class ImageFileSaveEventHandler implements ActionListener {
		private String[] suffixes = {".png", ".jpg", ".jpeg", ".jpe", ".jfif", ".gif"};
		@Override
		public void actionPerformed(ActionEvent ev) {
			JFileChooser fc = new JFileChooser(".");
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setDialogTitle("画像の保存");
			fc.setFileFilter(new FileChooserFilter("画像ファイル", suffixes));
			int ret = fc.showSaveDialog(UDPDrawingTool.this);
			if (ret != JFileChooser.APPROVE_OPTION) {
				return;
			}
			String filePath = fc.getSelectedFile().getAbsolutePath();

			BufferedImage image = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = image.createGraphics();
			canvas.paint(g2);
			g2.dispose();
			try {
				ImageIO.write(image, "jpeg", new File(filePath));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * ファイルフィルタのクラス
	 */
	private class FileChooserFilter extends javax.swing.filechooser.FileFilter {
		private String[] suffixes;   // 許容する拡張子
		private String description;  // 表示テキスト

		FileChooserFilter(String description, String[] suffixes) {
			this.suffixes = suffixes;
			this.description = description + "(";
			for (int i = 0; i < suffixes.length - 1; i++) {
				this.description += "*" + suffixes[i] + ";";
			}
			this.description += "*" + suffixes[suffixes.length - 1] + ")";
		}

		@Override
		public boolean accept(File file) {
			/*
			 * 表示候補がディレクトリの場合には、trueを返すことが必要である。
			 * そうしなければ、ディレクトリが表示されなくなってしまい、
			 * JFileChooserの中で、他のディレクトリに移れなくなってしまう。
			 * ディレクトリ以外の場合には、拡張子のチェックを行う。
			 */
			if (file.isDirectory()) {
				return true;  // これを行わないと他のディレクトリに移れない
			}
			String name = file.getName().toLowerCase();
			for (String s : suffixes) {
				if (name.endsWith(s)) {
					return true;
				}
			}
			return false;
		}
		@Override
		public String getDescription() {
			return description;
		}
	}

	/**
	 * ルックアンドフィールの切り替えを行うクラス
	 */
	private class LFChangeEventHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			setLookAndFeel(ev.getActionCommand());
		}
	}
	/**
	 * ルックアンドフィールを切り替える
	 * @param LFName  ルックアンドフィール名
	 */
	public void setLookAndFeel(String LFName) {
		String newLFClassName = "";
		for (LookAndFeelInfo info : lf) {
			if (LFName.equals(info.getName())) {
				newLFClassName = info.getClassName();
				break;
			}
		}
		try {
			UIManager.setLookAndFeel(newLFClassName);
			SwingUtilities.updateComponentTreeUI(UDPDrawingTool.this);
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (InstantiationException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
		}
	}
}
