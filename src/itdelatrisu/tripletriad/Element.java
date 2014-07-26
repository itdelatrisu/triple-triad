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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Image;
import org.newdawn.slick.util.Log;

/**
 * Elements.
 */
public enum Element {
	NEUTRAL, FIRE, WATER, EARTH, THUNDER, ICE, WIND, POISON, HOLY;

	/** Number of frames per animation. */
	private static final int FRAMES = 4;

	/** Element animation. */
	private Animation animation;

	/** First frame (unscaled). */
	private Image firstFrame;

	/**
	 * Initializes all element animations.
	 */
	public static void init() {
		int length = Options.getCardLength() / 4;
		for (Element ele : Element.values()) {
			if (ele == NEUTRAL)
				continue;

			String name = ele.toString().toLowerCase();
			Image[] frames = new Image[FRAMES];
			try {
				for (int i = 1; i <= frames.length; i++) {
					Image frame = new Image(String.format("ele-%s%d.png", name, i));
					if (ele.firstFrame == null)
						ele.firstFrame = frame;
					frames[i - 1] = frame.getScaledCopy(length, length);
				}
				ele.animation = new Animation(frames, 100);
			} catch (Exception e) {
				Log.error(String.format("Failed to load images for element '%s'.", name), e);
			}
		}
	}

	/**
	 * Returns a random board of elements.
	 * @return an array of size 9, with each index containing an Element
	 */
	public static Element[] getRandomBoard() {
		// shuffle elements
		LinkedList<Element> elements = new LinkedList<Element>(Arrays.asList(Element.values()));
		Collections.shuffle(elements);

		// build array
		Element[] board = new Element[9];
		for (int i = 0; i < board.length; i++)
			board[i] = (Math.random() < 0.25) ? elements.remove() : NEUTRAL;
		return board;
	}

	/**
	 * Returns the first frame of the element animation (unscaled).
	 * @return the first unscaled frame
	 */
	public Image getFirstFrame() { return firstFrame; }

	/**
	 * Draws the element at a position on the board.
	 * If a card is in the position, a +1 or -1 bonus will be drawn instead.
	 * @param pos the board position [0, 8]
	 * @param card the card at the position
	 */
	public void drawOnBoard(int pos, Card card) {
		if (this == NEUTRAL || animation == null)
			return;

		float x = (Options.getWidth() / 2) - ((1 - (pos % 3)) * Options.getCardLength());
		float y = (Options.getHeight() / 2) - ((1 - (pos / 3)) * Options.getCardLength());
		if (card == null || card.isPlaying())
			animation.draw(x - (animation.getWidth() / 2), y - (animation.getHeight() / 2));
		else if (card.getElement() == this)
			GameImage.BONUS_PLUS.getImage().drawCentered(x, y);
		else
			GameImage.BONUS_MINUS.getImage().drawCentered(x, y);
	}
}
