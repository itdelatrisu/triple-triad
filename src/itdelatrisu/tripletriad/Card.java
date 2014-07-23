/*
 * Triple Triad - a card game from FFVIII
 * Copyright (C) 2014 Jeffrey Han
 *
 * Triple Triad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Triple Triad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Triple Triad.  If not, see <http://www.gnu.org/licenses/>.
 */

package itdelatrisu.tripletriad;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.util.Log;

/**
 * Card data type.
 */
public class Card {
	/** Rank locations. */
	public enum Rank { TOP, LEFT, RIGHT, BOTTOM };

	/** Card ID. */
	private int id;

	/** Card name. */
	private String name;

	/** Card ranks. */
	private int rankTop, rankLeft, rankRight, rankBottom;

	/** Card element. */
	private Element element;

	/** Card level. */
	private int level;

	/** Card image. */
	private Image img;

	/** Card position [0, 8], or -1 if not played. */
	private int position = -1;

	/** Card owner (PLAYER or OPPONENT). */
	private boolean owner;

	/** Whether or not the owner of the card recently changed. */
	private boolean isNewColor = false;

	/** Color change animation progress [0, 2]. */
	private static float colorChangeAlpha = 0f;

	/**
	 * Returns the integer rank of a character.
	 * @param c the character
	 * @return the rank
	 */
	private static int getRank(char c) {
		return (c == 'A') ? 10 : Character.getNumericValue(c);
	}

	/**
	 * Constructor.
	 * @param id the card ID
	 * @param name the card name
	 * @param ranks the card ranks ({top}{left}{right}{bottom})
	 * @param element the card element
	 * @param level the card level
	 */
	public Card(int id, String name, String ranks, Element element, int level) {
		this.id = id;
		this.name = name;
		this.rankTop = getRank(ranks.charAt(0));
		this.rankLeft = getRank(ranks.charAt(1));
		this.rankRight = getRank(ranks.charAt(2));
		this.rankBottom = getRank(ranks.charAt(3));
		this.element = element;
		this.level = level;
	}

	/**
	 * Creates a copy of a card.
	 * @param original the original card
	 */
	public Card(Card original) {
		this.id = original.id;
		this.name = original.name;
		this.rankTop = original.rankTop;
		this.rankLeft = original.rankLeft;
		this.rankRight = original.rankRight;
		this.rankBottom = original.rankBottom;
		this.element = original.element;
		this.level = original.level;
		this.img = original.img;

		if (img == null)
			loadCardImage();
	}

	/**
	 * Loads card image data.
	 */
	private void loadCardImage() {
		try {
			// base image
			Image card = new Image(String.format("%03d.png", id));
			card = card.getScaledCopy(Options.getCardLength(), Options.getCardLength());
			Graphics g = card.getGraphics();

			// ranks
			int rankWidth = GameImage.RANK_0.getImage().getWidth();
			int rankHeight = GameImage.RANK_0.getImage().getHeight();
			float rankX = Options.getCardLength() * 0.06f;
			float rankY = Options.getCardLength() * 0.06f;
			g.drawImage(GameImage.getRank(rankTop).getImage(), rankX + (rankWidth / 2f), rankY);
			g.drawImage(GameImage.getRank(rankLeft).getImage(), rankX, rankY + rankHeight);
			g.drawImage(GameImage.getRank(rankRight).getImage(), rankX + rankWidth, rankY + rankHeight);
			g.drawImage(GameImage.getRank(rankBottom).getImage(), rankX + (rankWidth / 2f), rankY + (rankHeight * 2));

			// element
			if (element != Element.NEUTRAL)
				g.drawImage(element.getFrame(0), Options.getCardLength() * 0.7f, Options.getCardLength() * 0.05f);

			g.flush();
			this.img = card;
		} catch (Exception e) {
			Log.error(String.format("Failed to load card %d.", id), e);
		}
	}

	/**
	 * Returns the card ID.
	 * @return the ID
	 */
	public int getID() { return id; }

	/**
	 * Returns the card name.
	 * @return the name
	 */
	public String getName() { return name; }

	/**
	 * Returns the card rank.
	 * @param rank the rank location
	 * @return the rank
	 */
	public int getRank(Rank rank) {
		switch (rank) {
			case TOP: return rankTop;
			case LEFT: return rankLeft;
			case RIGHT: return rankRight;
			case BOTTOM: return rankBottom;
		}
		return -1;
	}

	/**
	 * Returns the card element.
	 * @return the element
	 */
	public Element getElement() { return element; }

	/**
	 * Returns the card level.
	 * @return the level
	 */
	public int getLevel() { return level; }

	/**
	 * Returns the card image.
	 * @return the image
	 */
	public Image getImage() { return img; }

	/**
	 * Draws the card at a location.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public void draw(float x, float y) {
		if (img == null)
			loadCardImage();

		// draw background color
		if (isColorChange() && isNewColor) {
			Image colorImg;
			boolean color = (colorChangeAlpha <= 1f) ? owner : !owner;
			if (color == TripleTriad.PLAYER)
				colorImg = GameImage.CARD_BLUE.getImage();
			else
				colorImg = GameImage.CARD_RED.getImage();

			float alpha = (colorChangeAlpha <= 1f) ? 1f - colorChangeAlpha : colorChangeAlpha - 1f;
			colorImg.setAlpha(alpha);
			GameImage.CARD_GRAY.getImage().draw(x, y);
			colorImg.draw(x, y);
			colorImg.setAlpha(1f);
		} else {
			if (owner == TripleTriad.PLAYER)
				GameImage.CARD_BLUE.getImage().draw(x, y);
			else
				GameImage.CARD_RED.getImage().draw(x, y);
			isNewColor = false;
		}

		img.draw(x, y);
	}

	/**
	 * Draws the card centered at a location.
	 * @param x the center x coordinate
	 * @param y the center y coordinate
	 */
	public void drawCentered(float x, float y) {
		if (img == null)
			loadCardImage();

		draw(x - (img.getWidth() / 2) , y - (img.getHeight() / 2));
	}

	/**
	 * Draws the card at its position on the board.
	 */
	public void drawOnBoard() {
		if (position == -1)
			return;

		drawCentered(
			(Options.getWidth() / 2) - ((1 - (position % 3)) * Options.getCardLength()),
			(Options.getHeight() / 2) - ((1 - (position / 3)) * Options.getCardLength())
		);
	}

	/**
	 * Draws the card at a position in the owner's hand.
	 * @param pos the position [0, 4]
	 * @param selected true if the card is currently selected
	 */
	public void drawInHand(float pos, boolean selected) {
		if (position != -1)
			return;

		int posX = Options.getWidth() / 2;
		int posY = (Options.getHeight() / 2) - Options.getCardLength();
		float offsetX = Options.getCardLength() * ((selected) ? 1.95f : 2.1f);
		float offsetY = Options.getCardLength() / 2f;
		if (owner == TripleTriad.PLAYER)
			drawCentered(posX + offsetX, posY + (pos * offsetY));
		else {
			if (Rule.OPEN.isActive())
				drawCentered(posX - offsetX, posY + (pos * offsetY));
			else
				GameImage.CARD_BACK.getImage().drawCentered(posX - offsetX, posY + (pos * offsetY));
		}
	}

	/**
	 * Returns whether or not the card has been played.
	 * @return true if played
	 */
	public boolean isPlayed() { return (position != -1); }

	/**
	 * Returns the card position.
	 * @return the position [0, 8], or -1 if not played
	 */
	public int getPosition() { return position; }

	/**
	 * Sets the card position.
	 * @param position the position
	 */
	public void setPosition(int position) { this.position = position; }

	/**
	 * Returns the card owner.
	 * @return PLAYER or OPPONENT
	 */
	public boolean getOwner() { return owner; }

	/**
	 * Sets the card owner.
	 * @param owner PLAYER or OPPONENT
	 */
	public void setOwner(boolean owner) { this.owner = owner; }

	/**
	 * Changes the card owner and initiates color change animation.
	 */
	public void changeOwner() {
		owner = !owner;
		isNewColor = true;
		colorChangeAlpha = 2f;
	}

	/**
	 * Updates the card animations by a delta interval.
	 * @param delta the delta interval since the last call
	 */
	public static void update(int delta) {
		if (isColorChange())
			colorChangeAlpha -= delta / 300f;
	}

	/**
	 * Returns whether or not a color change animation is in progress.
	 * @return true if color changing
	 */
	public static boolean isColorChange() { return (colorChangeAlpha > 0f); }
}