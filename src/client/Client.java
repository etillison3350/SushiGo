package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import client.CardHand.CardEvent;
import client.CardHand.CardListener;

public class Client extends JFrame implements CardListener {

	private static final long serialVersionUID = 8715904577824128661L;

	public static final String[] cardNames = {"Tempura", "Sashimi", "Dumpling", "1 Maki Roll", "2 Maki Rolls", "3 Maki Rolls", "Egg Nigiri", "Salmon Nigiri", "Squid Nigiri", "Pudding", "Wasabi", "Chopsticks"};
	public static final Color[] cardColors = {new Color(179, 146, 195), new Color(171, 206, 59), new Color(94, 136, 190), new Color(200, 28, 38), new Color(200, 28, 38), new Color(200, 28, 38), new Color(246, 176, 51), new Color(246, 176, 51), new Color(246, 176, 51), new Color(239, 159, 167), new Color(246, 176, 51), new Color(130, 207, 206)};
	public static final Color[] cardTextColors = {new Color(237, 164, 201), new Color(201, 218, 43), new Color(113, 180, 218), new Color(238, 29, 35), new Color(238, 29, 35), new Color(238, 29, 35), new Color(254, 194, 16), new Color(254, 194, 16), new Color(254, 194, 16), new Color(248, 181, 181), new Color(254, 194, 16), new Color(158, 213, 207)};

	public static void main(String[] args) {
		Client client = new Client();
		client.setVisible(true);
	}

	private boolean acceptInput = false, acceptSticks = true;

	private CardHand hand;

	private BufferedReader in;
	private PrintWriter out;

	public Client() {
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setSize(640, 480);
		this.setMinimumSize(new Dimension(384, 192));

		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				if (JOptionPane.showConfirmDialog(Client.this, "Are you sure you want to quit?", "Confirm Exit", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
					Client.this.dispose();
					System.exit(0);
				}
			}

		});

		String name = JOptionPane.showInputDialog(null, "Enter your display name:", "Name", JOptionPane.PLAIN_MESSAGE).trim();
		String address = JOptionPane.showInputDialog(null, "Enter IP address:", "IP Address", JOptionPane.PLAIN_MESSAGE);

		if (name == null || address == null) System.exit(0);

		this.setTitle("SushiGo | " + name);

		String[] as = address.split(":");

		new Thread(new Runnable() {

			@Override
			public void run() {
				int port;
				try {
					port = Integer.parseInt(as[1]);
				} catch (Exception e) {
					port = 11610;
				}

				Socket socket;
				try {
					socket = new Socket(as[0], port);
					out = new PrintWriter(socket.getOutputStream());
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

					print(name);

					setVisible(true);
				} catch (IOException e) {
					Client.this.dispose();
					JOptionPane.showMessageDialog(null, "Failed to connect to " + as[0] + ":" + port, "Connection Failed", JOptionPane.ERROR_MESSAGE);
					return;
				}

				JButton ready = new JButton("Ready");
				ready.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						print("r ");
						ready.setFocusPainted(false);
						ready.setText("Waiting for other players...");
						ready.setEnabled(false);
					}
				});
				Client.this.add(ready, BorderLayout.PAGE_START);

				JLabel info = new JLabel();
				Client.this.add(info, BorderLayout.PAGE_END);

				String s;
				while (!(s = read()).startsWith("s")) {
					info.setText(s);
				}
				final int num = s.charAt(1) - 48;
				List<String> names = new ArrayList<>();
				for (int i = 0; i < num; i++) {
					names.add(read());
				}
				final int index = read().charAt(0) - 48;

				int[] cards = {0};
				while (!(s = read()).startsWith("k"));
				cards = new int[s.length() - 1];
				for (int i = 0; i < cards.length; i++) {
					cards[i] = s.charAt(i + 1) - 48;
				}

				Client.this.getContentPane().removeAll();
				hand = new CardHand(256, cards);
				hand.addCardListener(Client.this);
				Table table = new Table(index, names.toArray(new String[names.size()]));

				Client.this.add(table, BorderLayout.PAGE_START);
				Client.this.add(new JScrollPane(hand, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), BorderLayout.PAGE_END);
				Client.this.revalidate();
				Client.this.repaint();

				while (!(s = read()).equals("e")) {
					if (s.startsWith("k")) {
						cards = new int[s.length() - 1];
						for (int i = 0; i < cards.length; i++) {
							cards[i] = s.charAt(i + 1) - 48;
						}
						hand.setCards(cards);
					} else if (s.startsWith("c")) {
						acceptInput = true;
						acceptSticks = s.length() < 2;
						hand.setEnabled(true);
					} else if (s.startsWith("q")) {
						int ct = 4 * (s.charAt(1) - 48);
						int n = -1;
						for (int i = 0; i < ct; i++) {
							String str = read();
							try {
								switch (i % 4) {
									case 0:
										n = str.charAt(0) - 48;
										break;
									case 2:
										table.setText(n, str);
										break;
									case 3:
										int[] cs = new int[str.length() - 1];
										for (int c = 0; c < cs.length; c++) {
											cs[c] = str.charAt(c + 1) - 48;
										}
										table.setCards(n, cs);
										break;
								}
							} catch (Exception e) {}
						}
					}
				}

				try {
					socket.close();
				} catch (IOException e) {}
			}
		}).start();
	}

	public void print(String str) {
		out.println(str);
		out.flush();
	}

	public String read() {
		try {
			String ret = in.readLine();
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
			this.dispose();
			JOptionPane.showMessageDialog(null, "Your connection to the server has been lost.", "Connection Lost", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	@Override
	public void cardSelected(CardEvent e) {
		if (!acceptInput || !(e.getSource() instanceof CardHand)) return;

		if (e.getSource() == hand) {
			((CardHand) e.getSource()).setEnabled(false);
			print("" + (char) (e.card + 48));
		} else if (acceptSticks && ((CardHand) e.getSource()).getCards()[e.card] == 11) {
			print("" + (char) 65);
			((CardHand) e.getSource()).setEnabled(false);
		}
	}

}
