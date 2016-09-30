package server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import java.util.TreeSet;

public class Server {

	public static final SimpleDateFormat date = new SimpleDateFormat("[HH:mm:ss.SSS]");
	public static final Random rand = new Random();

	private static final List<Player> players = new ArrayList<>();

	public static boolean done = false;

	public static ArrayDeque<Integer> cards;

	public static final String[] cardNames = {"Tempura", "Sashimi", "Dumpling", "1 Maki Roll", "2 Maki Rolls", "3 Maki Rolls", "Egg Nigiri", "Salmon Nigiri", "Squid Nigiri", "Pudding", "Wasabi", "Chopsticks"};

	protected static PrintStream log;
	protected static LogWindow logWindow;

	public static void main(String[] args) {
		logWindow = new LogWindow();
		
		String path = "server/logs/" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".log";
		Path logPath = Paths.get(path);
		int origNum = 0;
		while (Files.exists(logPath, LinkOption.NOFOLLOW_LINKS)) {
			logPath = Paths.get(path + "_" + origNum++);
		}
		try {
			Files.createDirectories(logPath.getParent());
			Files.createFile(logPath);
			log = new PrintStream(logPath.toFile());
			print("Log file created at " + logPath.toAbsolutePath().toString());
		} catch (IOException e) {
			print("ERROR", "Failed to create log at " + logPath.toAbsolutePath().toString());
		}
		
		try {	
			ServerSocket socket = new ServerSocket();
			try {
				Files.createFile(Paths.get("server/config.ini"));
			} catch (IOException e) {}

			String address = null;
			int port = 11610;
			try {
				List<String> lines = Files.readAllLines(Paths.get("server/config.ini"));
				for (String line : lines) {
					if (line.startsWith("server-ip=")) {
						address = line.substring(line.indexOf('=') + 1);
					} else if (line.startsWith("server-port=")) {
						try {
							port = Integer.parseInt(line.substring(line.indexOf('=') + 1));
						} catch (NumberFormatException e) {}
					}
				}
			} catch (IOException e) {
				print("ERROR", "Failed to read from \"server/config.ini\".");
				System.exit(0);
			}

			if (address == null) {
				print("ERROR", "Could not find property \"server-ip\" in \"server/config.ini\"");
				System.exit(0);
			}

			print("Starting server on " + address + ":" + port + "...");

			try {
				socket.bind(new InetSocketAddress(address, port));
			} catch (Exception e) {
				print("Failed to bind to port. Perhaps another server is already running on that port?");
				System.exit(0);
			}
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
//			Collections.shuffle(players);

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
					p.print("k" + getCardsString(p.cards));
				}

				while (players.get(0).cards.size() > 1) {
					HashMap<Player, String> playLog = new HashMap<>();

					int[] ixs = getCardIndicies();
					for (int i = 0; i < ixs.length; i++) {
						if (ixs[i] > 128) {
							int j = (ixs[i] >> 8) & 255;
							int k = ixs[i] & 255;

							players.get(i).played.add(players.get(i).cards.get(j));
							players.get(i).played.add(players.get(i).cards.get(k));
							Integer card1 = players.get(i).cards.remove(Math.max(j, k));
							Integer card2 = players.get(i).cards.remove(Math.min(j, k));
							players.get(i).cards.add(11);
							players.get(i).played.remove(new Integer(11));

							playLog.put(players.get(i), "chopsticks to play " + cardNames[card1].toLowerCase() + " and " + cardNames[card2].toLowerCase());
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
							p.print("l" + getCardsString(players.get(n).played));
						}
					}

					List<Integer> p0cards = new ArrayList<>(players.get(0).cards);
					for (int i = 1; i < players.size(); i++) {
						players.get(i - 1).cards.clear();
						players.get(i - 1).cards.addAll(players.get(i).cards);
					}
					players.get(players.size() - 1).cards.clear();
					players.get(players.size() - 1).cards.addAll(p0cards);

					for (Player p : players)
						p.print("k" + getCardsString(p.cards));
				}
				for (Player p : players) {
					p.played.add(p.cards.remove(0));
					p.scoreSummary.clear();
				}

				for (Player p : players) {
					p.cardCts = new int[15];
					for (Integer i : p.played) {
						if (i == 4 || i == 5) {
							p.cardCts[3] += i - 2;
						} else {
							p.cardCts[i]++;
						}

						if (i > 5 && i < 9 && p.cardCts[10] > 0) {
							p.cardCts[10]--;
							p.cardCts[i + 6]++;
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

					p.scoreSummary.put(p.cardCts[6] + " egg nigiri (" + p.cardCts[12] + " with wasabi)", p.cardCts[6] + p.cardCts[12] * 2);
					p.scoreSummary.put(p.cardCts[7] + " salmon nigiri (" + p.cardCts[13] + " with wasabi)", 2 * (p.cardCts[7] + p.cardCts[13] * 2));
					p.scoreSummary.put(p.cardCts[8] + " squid nigiri (" + p.cardCts[14] + " with wasabi)", 3 * (p.cardCts[8] + p.cardCts[14] * 2));
					p.scoreSummary.put(p.cardCts[10] + " unused wasabi", 0);
					p.scoreSummary.put(p.cardCts[11] + " chopsticks", 0);
					p.pudding += p.cardCts[9];
					p.scoreSummary.put(p.cardCts[9] + " pudding (total of " + p.pudding + ")", 0);
				}

				if (round == 2) {
					TreeSet<Player> pudding = new TreeSet<>(new PlayerComp(-1));
					pudding.addAll(players);
					List<Player> pudding0 = new ArrayList<>(), pudding1 = new ArrayList<>();
					for (Player p : players) {
						if (p.pudding == pudding.first().pudding) {
							pudding0.add(p);
						} else if (p.pudding == pudding.last().pudding) {
							pudding1.add(p);
						}
					}

					for (Player p : pudding0)
						p.scoreSummary.put("Most pudding", 6 / pudding0.size());
					for (Player p : pudding1)
						p.scoreSummary.put("Least pudding", -6 / pudding1.size());
				}

				for (Player p : players) {
					p.print("g" + players.size());
				}
				for (int i = 0; i < players.size(); i++) {
					Map<String, Integer> ss = players.get(i).scoreSummary;
					int total = ss.values().stream().mapToInt(Integer::intValue).sum();
					players.get(i).score += total;
					for (Player p : players) {
						p.print("w" + i);
						p.print("p" + p.pudding);
						p.print("m" + getCardsString(players.get(i).played));
						p.print(" " + players.get(i).name + (players.get(i) == p ? " (You)" : ""));
						p.print("");
						for (String key : ss.keySet()) {
							p.print(" " + key + ": " + ss.get(key));
						}
						p.print(" This round: " + total);
						p.print(" Total: " + players.get(i).score);
					}
				}
				if (round == 2) {
					TreeSet<Player> rankings = new TreeSet<>(new PlayerComp(-2));
					rankings.addAll(players);
					for (Player p : players) {
						p.print("h" + rankings.first().name);
					}
				}
				for (Player p : players) {
					p.print("d");
				}

				if (round < 2) {
					for (Player p : players) {
						try {
							p.ready();
						} catch (IOException e) {}
					}
				}
			}

			for (Player p : players) {
				p.print("e");
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

	private static String getCardsString(Iterable<? extends Number> cards) {
		String ret = "";
		for (Number i : cards) {
			ret += (char) (i.intValue() + 48);
		}
		return ret;
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
						} while (i != 17 && (i < 0 || i >= players.get(ix).cards.size()));
						if (i == 17) {
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
	
	public static void print(String name, String text) {
		System.out.println(date.format(new Date()) + " [" + name + "] " + text);
		if (log != null) log.println(date.format(new Date()) + " [" + name + "] " + text);
		if (logWindow != null) logWindow.print(date.format(new Date()) + " [" + name + "] " + text);
	}
	
	public static void print(String text) {
		print("INFO", text);
	}

	private static final class PlayerComp implements Comparator<Player> {

		private final int index;

		public PlayerComp(int index) {
			this.index = index;
		}

		@Override
		public int compare(Player o1, Player o2) {
			int d = -Integer.compare(index == -2 ? o1.score : (index == -1 ? o1.pudding : o1.cardCts[index]), index == -2 ? o2.score : (index == -1 ? o2.pudding : o2.cardCts[index]));
			if (d == 0) d = Integer.compare(o1.hashCode(), o2.hashCode());
			return d;
		}

	}

}
