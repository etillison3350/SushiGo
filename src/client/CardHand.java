package client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

public class CardHand extends JComponent {

	private static final long serialVersionUID = -8449701974752686779L;

	private int preferredHeight;
	
	private int[] cards;

	public final Set<CardListener> listeners = new HashSet<>();

	public CardHand(int... cards) {
		this(435, cards);
	}
	
	public CardHand(int preferredHeight, int... cards) {
		this.preferredHeight = -preferredHeight;
		
		this.cards = cards;
		MouseAdapter ma = new MouseAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				if (isEnabled()) {
					int w = (int) (getHeight() / 6.75);
					int s = selected;
					selected = e.getX() / w;
					if (selected > cards.length + 3) {
						selected = -1;
					} else if (selected >= cards.length) {
						selected = cards.length - 1;
					}
					if (selected != s) repaint();
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (isEnabled()) {
					selected = -1;
					repaint();
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (isEnabled() && selected > 0) {
					for (CardListener l : listeners) {
						l.cardSelected(new CardEvent(CardHand.this, selected));
					}
				}
			}

		};
		this.addMouseListener(ma);
		this.addMouseMotionListener(ma);
	}

	@Override
	public Dimension getPreferredSize() {
		int h = this.getHeight() == 0 || this.preferredHeight < 0 ? Math.abs(preferredHeight) : this.getHeight();
		return new Dimension((int) ((cards.length + 3.5) * h / 6.75), h);
	}
	
	public void setPreferredHeight(int preferredHeight) {
		this.preferredHeight = -preferredHeight;
	}
	
	private int selected = -1;

	@Override
	protected void paintComponent(Graphics gg) {
		if (!(gg instanceof Graphics2D)) return;

		Graphics2D g = (Graphics2D) gg;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int w = (int) (this.getHeight() / 1.5);
		int d = (int) (w / 4.5);
		int c = (int) (this.getHeight() * 0.0575);

		FontMetrics fm;
		int fsize = 0;
		do {
			fm = g.getFontMetrics(new Font(Font.SANS_SERIF, Font.PLAIN, ++fsize));
		} while (fm.getHeight() < d * 0.75);
		g.setFont(fm.getFont());

		for (int n = 0; n < cards.length; n++) {
			g.setColor(Color.GRAY);
			g.drawRoundRect(n * d - 1, 0, w, this.getHeight(), c, c);
			g.setColor(Client.cardColors[cards[n]].darker());
			g.fillRoundRect(n * d, 0, w, this.getHeight(), c, c);
			g.setColor(Client.cardTextColors[cards[n]]);
			String str = Client.cardNames[cards[n]];
			AffineTransform orig = g.getTransform();
			g.rotate(-Math.PI / 2, (n + 0.625) * d, (this.getHeight() + fm.stringWidth(str)) / 2);
			g.drawString(str, (int) ((n + 0.625) * d), (this.getHeight() + fm.stringWidth(str)) / 2);
			g.setTransform(orig);
		}

		if (selected >= 0 && selected < cards.length) {
			g.setColor(Client.cardColors[cards[selected]].darker());
			g.fillRoundRect(selected * d, 0, w, this.getHeight(), c, c);
			g.setColor(Color.GRAY);
			g.drawRoundRect(selected * d, 0, w, this.getHeight(), c, c);
			g.setColor(Client.cardTextColors[cards[selected]]);
			String str = Client.cardNames[cards[selected]];
			g.drawString(str, (int) ((selected + 2.25) * d - 0.5 * fm.stringWidth(str)), this.getHeight() - d / 2);
		}
	}

	public int[] getCards() {
		return cards;
	}

	public void setCards(int... cards) {
		this.cards = cards;
		selected = -1;
		this.repaint();
	}

	public void addCardListener(CardListener l) {
		this.listeners.add(l);
	}

	public void removeCardListener(CardListener l) {
		this.listeners.remove(l);
	}

	public static interface CardListener extends EventListener {

		public void cardSelected(CardEvent e);

	}

	public static class CardEvent extends EventObject {

		private static final long serialVersionUID = 8474210383716732194L;

		public final int card;

		public CardEvent(Object source, int card) {
			super(source);

			this.card = card;
		}

	}

}
