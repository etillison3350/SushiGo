package client;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

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

		this.names = names;
		this.hands = new CardHand[names.length];
		this.labels = new JLabel[names.length][3];
		this.pudding = new int[names.length];
		this.scores = new int[names.length];

		JPanel[] panels = new JPanel[names.length];
		for (int i = 0; i < names.length; i++) {
			int n = (i + index) % names.length;

			panels[i] = new JPanel(new BorderLayout());
			hands[n] = new CardHand(128, new int[0]);
			panels[i].add(hands[n]);
			labels[n][0] = new JLabel(names[n] + (i == 0 ? " (You)" : "") + ": 0");
			labels[n][1] = new JLabel("Pudding: 0");
			labels[n][2] = new JLabel();
			JPanel labelPanel = new JPanel(new GridLayout(0, 1));
			panels[i].add(labelPanel, BorderLayout.PAGE_START);
			labelPanel.add(labels[n][0]);
			labelPanel.add(labels[n][1]);
			labelPanel.add(labels[n][2]);
		}

		switch (names.length) {
			case 2:
				this.add(panels[1]);
				break;
			case 3: {
				JPanel panel = new JPanel(new GridLayout(1, 2));
				panel.add(panels[1]);
				panel.add(panels[2]);
				this.add(panel);
				break;
			}
			case 4: {
				this.add(panels[2]);
				JPanel panel = new JPanel(new GridLayout(1, 2));
				panel.add(panels[1]);
				panel.add(panels[3]);
				this.add(panel);
				break;
			}
			case 5: {
				JPanel panel1 = new JPanel(new GridLayout(1, 2));
				panel1.add(panels[2]);
				panel1.add(panels[3]);
				this.add(panel1);
				JPanel panel2 = new JPanel(new GridLayout(1, 2));
				panel2.add(panels[1]);
				panel2.add(panels[4]);
				this.add(panel2);
				break;
			}
		}
		this.add(panels[0]);
	}

	public CardHand getPlayerHand() {
		return hands[index % hands.length];
	}

	public void setCards(int index, int[] cards) {
		this.hands[(index + this.index) % this.hands.length].setCards(cards);
	}

	public int[] getPudding() {
		return pudding;
	}

	public void setPudding(int index, int pudding) {
		this.pudding[(index + this.index) % this.pudding.length] = pudding;
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

}
