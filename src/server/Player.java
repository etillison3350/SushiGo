package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Player {

	public final String name;
	
	protected final Socket socket;
	protected final PrintWriter out;
	protected final BufferedReader in;

	public final List<Integer> cards = new ArrayList<>(), played = new ArrayList<>();
	public int pudding, score;
	public int[] cardCts;
	
	public final HashMap<String, Integer> scoreSummary = new HashMap<>();
	
	public Player(Socket socket) throws IOException {
		this.socket = socket;
		this.out = new PrintWriter(socket.getOutputStream());
		this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		this.name = in.readLine();
	}

	/**
	 * <ul>
	 * <li><b><i>print</i></b><br>
	 * <br>
	 * {@code public void print(String text)}<br>
	 * <br>
	 * Outputs the given text to the client.<br>
	 * @param text The text to output
	 *        </ul>
	 */
	public void print(String text) {
		if (text.contains("\n")) {
			for (String s : text.split("\n")) {
				print(s);
			}
		} else {
			out.println(text);
			out.flush();
			System.out.println(Server.date.format(new Date()) + " [SERVER -> " + this.name + "] " + text);
		}
	}

	/**
	 * <ul>
	 * <li><b><i>read</i></b><br>
	 * <br>
	 * {@code public String read() throws IOException}<br>
	 * <br>
	 * Reads from the client<br>
	 * @return input from the client, as returned by {@link BufferedReader#readLine()}
	 * @throws IOException If there is an IOException while reading.
	 *         </ul>
	 */
	public String read() throws IOException {
		String ret = in.readLine();
		System.out.println(Server.date.format(new Date()) + " [" + this.name + "] " + ret);
		return ret;
	}

	/**
	 * <ul>
	 * <li><b><i>ready</i></b><br><br>
	 * {@code void ready()}<br><br>
	 * Waits until the player is ready (i.e. sends <code>"r"</code>)<br>
	 * </ul>
	 */
	public void ready() throws IOException {
		while (!read().equals("r "));
	}
	
	/**
	 * <ul>
	 * <li><b><i>close</i></b><br>
	 * <br>
	 * {@code public void close() throws IOException}<br>
	 * <br>
	 * Closes the socket and all streams.<br>
	 * @throws IOException If an IOException occurs while closing the socket or streams.
	 *         </ul>
	 */
	public void close() throws IOException {
		out.println((char) 4);
		out.flush();
		out.close();
		in.close();
		socket.close();
	}
	
	@Override
	public String toString() {
		return this.name;
	}

}
