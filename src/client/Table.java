package client;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class Table extends JPanel {

	private static final long serialVersionUID = 7195299477756680958L;

	public final int index;

	final String[] names;
	private final CardHand[] hands;
	private final JLabel[][] labels;
	private int[] pudding, scores;

	public Table(int index, String... names) {
		super(new GridLayout(0, 1));

		this.index = index;

		List<String> ns = Arrays.asList(names);
		Collections.rotate(ns, index);
		this.names = ns.toArray(new String[ns.size()]);
		this.hands = new CardHand[names.length];
		this.labels = new JLabel[names.length][3];
		this.pudding = new int[names.length];
		this.scores = new int[names.length];

		JPanel[] panels = new JPanel[names.length];
		for (int i = 0; i < names.length; i++) {
			int align = i == 0 || (names.length % 2 == 0 && i == names.length / 2) ? SwingConstants.CENTER : (i > names.length / 2 ? SwingConstants.RIGHT : SwingConstants.LEFT);

			panels[i] = new JPanel(new BorderLayout());
			hands[i] = new CardHand(128, align, new int[0]);
			panels[i].add(hands[i]);
			labels[i][0] = new JLabel(names[i] + (i == 0 ? " (You)" : "") + ": 0", align);
			labels[i][1] = new JLabel("Pudding: 0", align);
			labels[i][2] = new JLabel("", align);
			JPanel labelPanel = new JPanel(new GridLayout(0, 1));
			panels[i].add(labelPanel, BorderLayout.PAGE_START);
			labelPanel.add(labels[i][0]);
			labelPanel.add(labels[i][1]);
			labelPanel.add(labels[i][2]);
		}

		switch (names.length) {
			case 2:
				this.add(getPanel(panels[1]));
				break;
			case 3:
				this.add(getPanel(panels[1], panels[2]));
				break;
			case 4:
				this.add(getPanel(panels[2]));
				this.add(getPanel(panels[1], panels[3]));
				break;
			case 5:
				this.add(getPanel(panels[2], panels[3]));
				this.add(getPanel(panels[1], panels[4]));
				break;
		}
		this.add(getPanel(panels[0]));
	}

	private static JPanel getPanel(JPanel... panels) {
		JPanel ret = new JPanel(new GridLayout(1, panels.length));
		for (JPanel panel : panels)
			ret.add(panel);
		return ret;
	}

	public CardHand getPlayerHand() {
		return hands[0];
	}

	public void setCards(int index, int[] cards) {
		this.hands[(index + this.index) % this.hands.length].setCards(cards);
	}

	public int[] getPudding() {
		return pudding;
	}

	public void setPudding(int index, int pudding) {
		this.pudding[(index + this.index) % this.pudding.length] = pudding;
		this.labels[(index + this.index) % this.labels.length][1].setText("Pudding: " + pudding);
	}

	public int[] getScores() {
		return scores;
	}

	public void setScores(int index, int score) {
		this.scores[(index + this.index) % this.scores.length] = score;
	}

	public void setText(int index, String text) {
		this.labels[(index + this.index) % this.labels.length][2].setText(text);
	}

	public void clear() {
		for (CardHand hand : hands)
			hand.setCards();
	}

}
