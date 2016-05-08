package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeSet;

public class Server {

	public static final SimpleDateFormat date = new SimpleDateFormat("[HH:mm:ss.SSS]");
	public static final Random rand = new Random();

	private static final List<Player> players = new ArrayList<>();

	public static boolean done = false;

	public static ArrayDeque<Integer> cards;

	public static final String[] cardNames = {"Tempura", "Sashimi", "Dumpling", "1 Maki Roll", "2 Maki Rolls", "3 Maki Rolls", "Egg Nigiri", "Salmon Nigiri", "Squid Nigiri", "Pudding", "Wasabi", "Chopsticks"};

	public static void main(String[] args) {
		try {
			ServerSocket socket = new ServerSocket();
			int port = 0;
			Scanner input = new Scanner(System.in);
			while (true) {
				try {
					System.out.println("Enter IP address:");
					String address = input.nextLine();
					while (true) {
						System.out.println("Enter port:");
						try {
							port = Integer.parseInt(input.nextLine());
							if (port > 0 && port <= 65535) break;
						} catch (Exception e) {}
					}
					socket.bind(new InetSocketAddress(address, port));
					break;
				} catch (Exception e) {
					print("Failed to bind to port. Perhaps another server is already running on that port?");
				}
			}
			input.close();
			print("Server bound to " + socket.getInetAddress().getHostAddress() + ":" + port);

			print("Shuffling cards...");
			List<Integer> cds = new ArrayList<>();
			int[] cardCts = {14, 14, 14, 6, 12, 8, 5, 10, 5, 10, 6, 4};
			for (int i = 0; i < cardCts.length; i++) {
				for (int n = 0; n < cardCts[i]; n++)
					cds.add(i);
			}
			Collections.shuffle(cds);
			cards = new ArrayDeque<>(cds);

			print("Waiting for players to connect...");
			addPlayer(socket);
			addPlayer(socket);

			socket.setSoTimeout(1000);
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					while (!done && players.size() < 5) {
						try {
							addPlayer(socket);
						} catch (IOException e) {}
					}
				}
			});
			thread.start();

			for (int i = 0; i < players.size(); i++) {
				try {
					players.get(i).ready();
				} catch (IOException e) {
					players.remove(i--);
				}
			}
			done = true;
			Collections.shuffle(players);

			for (int i = 0; i < players.size(); i++) {
				players.get(i).print("s" + players.size());
				for (Player p : players) {
					players.get(i).print(p.name);
				}
				players.get(i).print("" + (char) (i + 48));
			}

			int cardNo = 12 - players.size();

			for (int round = 0; round < 3; round++) {
				for (Player p : players) {
					p.cards.clear();
					p.played.clear();
					for (int i = 0; i < cardNo; i++)
						p.cards.add(cards.pop());
					String cardStr = "k";
					for (Integer i : p.cards)
						cardStr += (char) (i + 48);
					p.print(cardStr);
				}

				while (players.get(0).cards.size() > 1) {
					HashMap<Player, String> playLog = new HashMap<>();

					int[] ixs = getCardIndicies();
					for (int i = 0; i < ixs.length; i++) {
						if (ixs[i] > 128) {
							int j = (ixs[i] >> 8) & 255;
							int k = ixs[i] & 255;

							players.get(i).played.add(players.get(i).cards.remove(j));
							players.get(i).played.add(players.get(i).cards.remove(k));
							players.get(i).cards.add(11);
							players.get(i).played.remove(new Integer(11));

							playLog.put(players.get(i), "chopsticks to play " + cardNames[j] + " and " + cardNames[k]);
						} else {
							Integer card = players.get(i).cards.remove(ixs[i]);
							players.get(i).played.add(card);
							playLog.put(players.get(i), cardNames[card].toLowerCase());
						}
					}

					for (Player p : players) {
						p.print("q" + players.size());
						for (int n = 0; n < players.size(); n++) {
							p.print((char) (n + 48) + (players.get(n) == p ? "u" : ""));
							p.print(players.get(n).name);
							p.print("used " + playLog.get(players.get(n)));
							String cardStr = "l";
							for (Integer i : players.get(n).played)
								cardStr += (char) (i + 48);
							p.print(cardStr);
						}
					}

					List<Integer> p0cards = new ArrayList<>(players.get(0).cards);
					for (int i = 1; i < players.size(); i++) {
						players.get(i - 1).cards.clear();
						players.get(i - 1).cards.addAll(players.get(i).cards);
					}
					players.get(players.size() - 1).cards.clear();
					players.get(players.size() - 1).cards.addAll(p0cards);

					for (Player p : players) {
						String cardStr = "k";
						for (Integer i : p.cards)
							cardStr += (char) (i + 48);
						p.print(cardStr);
					}
				}
				for (Player p : players) {
					p.played.add(p.cards.remove(0));
					p.scoreSummary.clear();
				}

				int[] wasabis = new int[3];
				for (Player p : players) {
					p.cardCts = new int[12];
					for (Integer i : p.played) {
						if (i == 4 || i == 5) {
							p.cardCts[3] += i - 2;
						} else {
							p.cardCts[i]++;
						}

						if (i > 5 && i < 9 && p.cardCts[10] > 0) {
							p.cardCts[10]--;
							wasabis[i - 6]++;
						}
					}
				}

				TreeSet<Player> maki = new TreeSet<>(new PlayerComp(3));
				maki.addAll(players);

				List<Player> maki1 = new ArrayList<>(), maki2 = new ArrayList<>();

				int m2 = -1;
				for (Player p : maki) {
					if (p.cardCts[3] == maki.first().cardCts[3]) {
						maki1.add(p);
					} else if (m2 < 0 || p.cardCts[3] == m2) {
						maki2.add(p);
						m2 = p.cardCts[3];
					}
				}

				for (Player p : maki1)
					p.scoreSummary.put("Most maki rolls", 6 / maki1.size());
				for (Player p : maki2)
					p.scoreSummary.put("Second most maki rolls", 3 / maki2.size());

				for (Player p : players) {
					int tempura = p.cardCts[0] / 2;
					p.scoreSummary.put(p.cardCts[0] + " tempura (" + tempura + " pairs)", tempura * 5);

					int sashimi = p.cardCts[1] / 3;
					p.scoreSummary.put(p.cardCts[1] + " sashimi (" + sashimi + " sets)", sashimi * 10);

					int dumplings = 0;
					int n;
					for (n = p.cardCts[2]; n >= 5; n -= 5) {
						dumplings += 15;
					}
					dumplings += new int[] {0, 1, 3, 6, 10}[n];
					p.scoreSummary.put(p.cardCts[2] + " dumplings", dumplings);

					p.scoreSummary.put(p.cardCts[6] + " egg nigiri (" + wasabis[0] + " with wasabi)", p.cardCts[6] + wasabis[0] * 2);
					p.scoreSummary.put(p.cardCts[7] + " salmon nigiri (" + wasabis[1] + " with wasabi)", 2 * (p.cardCts[7] + wasabis[1] * 2));
					p.scoreSummary.put(p.cardCts[8] + " squid nigiri (" + wasabis[2] + " with wasabi)", 3 * (p.cardCts[8] + wasabis[2] * 2));

					p.pudding += p.cardCts[9];
					p.scoreSummary.put(p.cardCts[9] + " pudding (total of " + p.pudding + ")", 0);
					p.scoreSummary.put(p.cardCts[10] + " unused wasabi", 0);
					p.scoreSummary.put(p.cardCts[10] + " chopsticks", 0);
				}

				for (int i = 0; i < players.size(); i++) {
					System.out.println(players.get(i).name);
					System.out.println(players.get(i).played);
					Map<String, Integer> ss = players.get(i).scoreSummary;
					for (String key : ss.keySet()) {
						System.out.println(key + ": " + ss.get(key));
					}
					int total = ss.values().stream().mapToInt(Integer::intValue).sum();
					System.out.println("This round: " + total);
					players.get(i).score += total;
					System.out.println("Total: " + players.get(i).score);
				}
			}

			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected static void addPlayer(ServerSocket socket) throws IOException {
		Player player = new Player(socket.accept());
		print("Accepted player \"" + player.name + "\" at " + player.socket.getInetAddress().getHostAddress());
		players.add(player);

		List<String> ps = new ArrayList<>();
		players.stream().forEach(p -> ps.add(p.name));
		String pss = ps.toString();
		pss = pss.substring(1, pss.length() - 1);
		for (Player p : players) {
			p.print("Players connected: " + pss);
		}
	}

	protected static int[] getCardIndicies() {
		int[] ret = new int[players.size()];

		Thread[] threads = new Thread[ret.length];
		for (int t = 0; t < threads.length; t++) {
			final int ix = t;
			threads[t] = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						int i;
						do {
							players.get(ix).print("c");
							i = (int) players.get(ix).read().charAt(0) - 48;
						} while (i != 65 && (i < 0 || i >= players.get(ix).cards.size()));
						if (i == 65) {
							int j, k;
							do {
								players.get(ix).print("ch");
								j = (int) players.get(ix).read().charAt(0) - 48;
							} while (j < 0 || j >= players.get(ix).cards.size());
							do {
								players.get(ix).print("ch");
								k = (int) players.get(ix).read().charAt(0) - 48;
							} while (k < 0 || k >= players.get(ix).cards.size());
							ret[ix] = (1 << 16) | (j << 8) | k;
						} else {
							ret[ix] = i;
						}
					} catch (Exception e) {
						System.exit(0);
					}
				}
			});
			threads[t].start();
		}

		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {}
		}

		return ret;
	}

	/**
	 * <ul>
	 * <li><b><i>print</i></b><br>
	 * <br>
	 * {@code protected static void print(String text)}<br>
	 * <br>
	 * Outputs the given text as a server log<br>
	 * @param text - The text to output
	 *        </ul>
	 */
	protected static void print(String text) {
		System.out.println(date.format(new Date()) + " [INFO] " + text);
	}

	private static final class PlayerComp implements Comparator<Player> {

		private final int index;

		public PlayerComp(int index) {
			this.index = index;
		}

		@Override
		public int compare(Player o1, Player o2) {
			int d = -Integer.compare(o1.cardCts[index], o2.cardCts[index]);
			if (d == 0) d = Integer.compare(o1.hashCode(), o2.hashCode());
			return d;
		}

	}

}
