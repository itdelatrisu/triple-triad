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

import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.util.Log;

/**
 * Controller for all audio.
 */
public class AudioController {
	/** Cursor sound effect. */
	private static Sound effect;

	/** BGM track. */
	private static Music bgm;

	// This class should not be instantiated.
	private AudioController() {}

	/**
	 * Initializes sounds and starts the BGM track.
	 */
	public static void init() {
		try {
			effect = new Sound("cursor.wav");
			bgm = new Music("bgm.ogg");
			bgm.loop();
		} catch (SlickException e) {
			Log.error("Failed to load audio.", e);
		}
	}

	/**
	 * Plays the cursor sound effect.
	 */
	public static void playEffect() { if (effect != null) effect.play(); }
}