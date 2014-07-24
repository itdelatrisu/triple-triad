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

import org.newdawn.slick.Animation;
import org.newdawn.slick.Image;
import org.newdawn.slick.util.Log;

/**
 * Character spinners.
 */
public enum Spinner {
	SQUALL, RINOA, ULTIMECIA, EDEA, IRVINE, SELPHIE, QUISTIS, ZELL;

	/** Number of frames per animation. */
	private static final int FRAMES = 4;

	/** Total number of spinners. */
	private static final int SIZE = Spinner.values().length;

	/** Spinner animation. */
	private Animation animation;

	/**
	 * Initializes all spinner animations.
	 */
	public static void init() {
		float scale = Options.getCardLength() / 256f * 2.5f;
		for (Spinner s : Spinner.values()) {
			String name = s.toString().toLowerCase();
			Image[] frames = new Image[FRAMES];
			try {
				for (int i = 1; i <= frames.length; i++) {
					Image frame = new Image(String.format("sprite-%s%d.png", name, i));
					frames[i - 1] = frame.getScaledCopy(scale);
				}
				s.animation = new Animation(frames, 200);
			} catch (Exception e) {
				Log.error(String.format("Failed to load sprites for spinner '%s'.", s.toString()), e);
			}
		}
	}

	/**
	 * Returns a random spinner.
	 * @return a random Spinner
	 */
	public static Spinner getRandomSpinner() {
		return Spinner.values()[(int) (Math.random() * SIZE)];
	}

	/**
	 * Returns the frame at an index.
	 * @param frame the frame [0, 3]
	 * @return the image
	 */
	public Image getFrame(int frame) {
		return (animation != null) ? animation.getImage(frame) : null;
	}

	/**
	 * Sets the animation speed.
	 * @param speed the speed (default: 1.0)
	 */
	public void setSpeed(float speed) {
		if (animation != null)
			animation.setSpeed(speed);
	}

	/**
	 * Draws the spinner centered at a location.
	 * @param x the center x coordinate
	 * @param y the center y coordinate
	 */
	public void drawCentered(float x, float y) {
		if (animation != null)
			animation.draw(
					x - (animation.getWidth() / 2),
					y - (animation.getHeight() / 2)
			);
	}
}
