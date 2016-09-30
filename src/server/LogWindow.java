package server;

import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class LogWindow extends JFrame {

	private static final long serialVersionUID = -7232424928810300546L;

	private JTextArea text;
	
	public LogWindow() {
		super("SushiGo Server");
		
		this.setSize(640, 480);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		text = new JTextArea();
		text.setEditable(false);
		text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
		this.add(new JScrollPane(text));
		
		this.setVisible(true);
	}
	
	public void print(String text) {
		this.text.append(text + "\n");
	}
	
}
