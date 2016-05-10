package client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.SwingConstants;

public class CardHand extends JComponent {

	private static final long serialVersionUID = -8449701974752686779L;

	private int preferredHeight, align;

	private int[] cards;
	private Set<Integer> disabled = new HashSet<>();

	private int dx;
	
	public final Set<CardListener> listeners = new HashSet<>();

	public CardHand(int... cards) {
		this(435, SwingConstants.LEFT, cards);
	}

	public CardHand(int preferredHeight, int... cards) {
		this(preferredHeight, SwingConstants.LEFT, cards);
	}

	public CardHand(int preferredHeight, int alignment, int... cards) {
		this.preferredHeight = -preferredHeight;
		this.align = alignment;

		this.cards = cards;
		MouseAdapter ma = new MouseAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				if (isEnabled()) {
					int w = (int) (getHeight() / 6.75);
					int s = selected;
					selected = (e.getX() - dx) / w;
					if (selected > CardHand.this.cards.length + 3) {
						selected = -1;
					} else if (selected >= CardHand.this.cards.length) {
						selected = CardHand.this.cards.length - 1;
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
			public void mouseReleased(MouseEvent e) {
				if (isEnabled() && selected >= 0 && !disabled.contains(selected)) {
					for (CardListener l : listeners) {
						l.cardSelected(new CardEvent(CardHand.this, selected));
					}
				}
			}

		};
		this.addMouseListener(ma);
		this.addMouseMotionListener(ma);

		this.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				recalculateDx();
				repaint();
			}

		});
	}

	private void recalculateDx() {
		dx = 0;
		switch (align) {
			case SwingConstants.CENTER:
				dx = (int) (0.5 * (getWidth() - (cards.length + 3.5) * getHeight() / 6.75));
				break;
			case SwingConstants.RIGHT:
				dx = (int) (getWidth() - (cards.length + 3.5) * getHeight() / 6.75);
				break;
		}		
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
			g.drawRoundRect(dx + n * d - 1, 0, w, this.getHeight(), c, c);

			Color col = Client.cardColors[cards[n]].darker();
			Color textCol = Client.cardTextColors[cards[n]];
			if (!isEnabled() || disabled.contains(n)) {
				col = Color.getHSBColor(0, 0, Color.RGBtoHSB(col.getRed(), col.getGreen(), col.getBlue(), new float[3])[2]);
				textCol = Color.getHSBColor(0, 0, Color.RGBtoHSB(textCol.getRed(), textCol.getGreen(), textCol.getBlue(), new float[3])[2]);
			}
			g.setColor(col);

			g.fillRoundRect(dx + n * d, 0, w, this.getHeight(), c, c);
			g.setColor(textCol);
			String str = Client.cardNames[cards[n]];
			AffineTransform orig = g.getTransform();
			g.rotate(-Math.PI / 2, dx + (n + 0.625) * d, (this.getHeight() + fm.stringWidth(str)) / 2);
			g.drawString(str, dx + (int) ((n + 0.625) * d), (this.getHeight() + fm.stringWidth(str)) / 2);
			g.setTransform(orig);
		}

		if (selected >= 0 && selected < cards.length) {
			Color col = Client.cardColors[cards[selected]].darker();
			Color textCol = Client.cardTextColors[cards[selected]];
			if (disabled.contains(selected)) {
				col = Color.getHSBColor(0, 0, Color.RGBtoHSB(col.getRed(), col.getGreen(), col.getBlue(), new float[3])[2]);
				textCol = Color.getHSBColor(0, 0, Color.RGBtoHSB(textCol.getRed(), textCol.getGreen(), textCol.getBlue(), new float[3])[2]);
			}
			g.setColor(col);

			g.fillRoundRect(dx + selected * d, 0, w, this.getHeight(), c, c);
			g.setColor(Color.GRAY);
			g.drawRoundRect(dx + selected * d, 0, w, this.getHeight(), c, c);
			g.setColor(textCol);
			String str = Client.cardNames[cards[selected]];
			g.drawString(str, dx + (int) ((selected + 2.25) * d - 0.5 * fm.stringWidth(str)), this.getHeight() - d / 2);
		}
	}

	public int[] getCards() {
		return cards;
	}

	public void setCards(int... cards) {
		this.cards = cards;
		selected = -1;
		recalculateDx();
		this.repaint();
	}

	public void addCardListener(CardListener l) {
		this.listeners.add(l);
	}

	public void removeCardListener(CardListener l) {
		this.listeners.remove(l);
	}

	public void enableAll() {
		disabled.clear();
	}

	public void disable(int index) {
		disabled.add(index);
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
