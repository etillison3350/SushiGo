package client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Scoresheet extends JDialog {

	private static final long serialVersionUID = -1270665022979493227L;

	private Scoresheet() {}

	public static void show(Component parent, int[][] cards, String winner, String[] scores) {
		JPanel textPanel = new JPanel(new GridLayout(1, scores.length));
		for (String score : scores) {
			JTextArea area = new JTextArea(score);
			area.setEditable(false);
			area.setLineWrap(true);
			area.setWrapStyleWord(true);
			textPanel.add(area);
		}

		JPanel cardPanel = new JPanel(new GridLayout(1, scores.length));
		for (int[] hand : cards) {
			CardHand ch = new CardHand(128, hand);
			JScrollPane scroll = new JScrollPane(ch, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			scroll.getHorizontalScrollBar().setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
			cardPanel.add(scroll);
		}

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(cardPanel, BorderLayout.PAGE_START);
		panel.add(new JScrollPane(textPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		if (winner != null) {
			JLabel win = new JLabel(winner + " wins!");
			win.setFont(win.getFont().deriveFont(30.0F));
			panel.add(win, BorderLayout.PAGE_END);
		}

		JOptionPane pane = new JOptionPane(panel);
		JDialog dialog = pane.createDialog("Scoresheet");
		dialog.setSize(640, 480);
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
	}
}
