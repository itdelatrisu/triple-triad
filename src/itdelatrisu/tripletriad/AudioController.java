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
	/** BGM track. */
	private static Music bgm;

	/** Sound effects. */
	public enum Effect {
		BACK ("sound-back.wav"),
		CARD ("sound-card.wav"),
		SELECT ("sound-select.wav"),
		SPECIAL ("sound-special.wav"),
		START ("sound-start.wav"),
		TURN ("sound-turn.wav");

		/** The associated file name. */
		private String filename;

		/** The associated sound. */
		private Sound sound;

		/**
		 * Initializes all sound effects.
		 */
		private static void init() {
			for (Effect effect : Effect.values()) {
				try {
					effect.sound = new Sound(effect.filename);
				} catch (Exception e) {
					Log.error(String.format("Failed to load sound '%s'.", effect.filename), e);
				}
			}
		}

		/**
		 * Constructor.
		 * @param filename the file name
		 */
		Effect(String filename) {
			this.filename = filename;
		}

		/**
		 * Plays the sound effect.
		 */
		public void play() { if (sound != null) sound.play(); }
	}

	// This class should not be instantiated.
	private AudioController() {}

	/**
	 * Initializes sounds and starts the BGM track.
	 */
	public static void init() {
		try {
			bgm = new Music("bgm.ogg");
			bgm.loop();
			Effect.init();
		} catch (SlickException e) {
			Log.error("Failed to load audio.", e);
		}
	}
}