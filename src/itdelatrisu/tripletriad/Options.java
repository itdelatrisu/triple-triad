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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.util.Log;

public class Options {
	/** Card data file. */
	public static final String DATA_FILE = "deck.txt";

	/** File for logging errors. */
	public static final File LOG_FILE = new File(".triple-triad.log");

	/** File for storing user options. */
	private static final File OPTIONS_FILE = new File(".triple-triad.cfg");

	/** Container dimensions. */
	private static int width = 1280, height = 720;

	/** Card length. */
	private static int cardLength = 256;

	/** Volume. */
	private static float musicVolume = 0.1f, soundVolume = 0.5f;

	/** Target frame rate. */
	private static int fps = 60;

	/** Default font. */
	private static UnicodeFont font;

	/** Font file. */
	private static File fontFile = new File("OpenSans-Light.ttf");

	/** AI types. */
	public enum AIType { RANDOM, OFFENSIVE, DEFENSIVE, BALANCED };

	/** Default AI types. */
	private static AIType playerAI = AIType.BALANCED, opponentAI = AIType.BALANCED;

	// This class should not be instantiated.
	private Options() {}

	/**
	 * Returns the container width.
	 * @return the width
	 */
	public static int getWidth() { return width; }

	/**
	 * Returns the container height.
	 * @return the height
	 */
	public static int getHeight() { return height; }

	/**
	 * Returns the card length.
	 * @return the card length
	 */
	public static int getCardLength() { return cardLength; }

	/**
	 * Returns the default font.
	 * @return the UnicodeFont
	 */
	public static UnicodeFont getFont() { return font; }

	/**
	 * Returns the player AI type.
	 * @return the AIType
	 */
	public static AIType getPlayerAI() { return playerAI; }

	/**
	 * Returns the opponent AI type.
	 * @return the AIType
	 */
	public static AIType getOpponentAI() { return opponentAI; }

	/**
	 * Sets the container size and makes the window borderless if the container
	 * size is identical to the screen resolution.
	 * <p>
	 * If the configured resolution is larger than the screen size, the screen
	 * resolution will be used.
	 * @param app the game container
	 * @throws SlickException failure to set display mode
	 */
	public static void setDisplayMode(AppGameContainer app) throws SlickException {
		int screenWidth = app.getScreenWidth();
		int screenHeight = app.getScreenHeight();
		if (screenWidth < width || screenHeight < height) {
			width = screenWidth;
			height = screenHeight;
		}

		app.setDisplayMode(width, height, false);
		if (screenWidth == width && screenHeight == height)
			System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");

		// set card length
		cardLength = (int) (width * 0.17f);
	}

	/**
	 * Initializes options.
	 * @param container the game container
	 */
	@SuppressWarnings("unchecked")
	public static void init(GameContainer container) {
		container.setTargetFrameRate(fps);
		container.setShowFPS(false);
		container.setAlwaysRender(true);
		container.getInput().enableKeyRepeat();
		container.setClearEachFrame(false);
		container.setMusicVolume(musicVolume);
		container.setSoundVolume(soundVolume);

		try {
			font = new UnicodeFont(fontFile.getName(), 32, false, false);
			font.addAsciiGlyphs();
			font.getEffects().add(new ColorEffect());
			font.loadGlyphs();
		} catch (SlickException e) {
			Log.error("Failed to load fonts.", e);
		}
	}

	/**
	 * Reads user options from the options file, if it exists.
	 */
	public static void parseOptions() {
		// if no config file, use default settings
		if (!OPTIONS_FILE.isFile()) {
			saveOptions();
			return;
		}

		try (BufferedReader in = new BufferedReader(new FileReader(OPTIONS_FILE))) {
			String line;
			String name, value;
			int i;
			while ((line = in.readLine()) != null) {
				line = line.trim();
				if (line.length() < 2 || line.charAt(0) == '#')
					continue;
				int index = line.indexOf('=');
				if (index == -1)
					continue;
				name = line.substring(0, index).trim();
				value = line.substring(index + 1).trim();
				switch (name) {
				case "WIDTH":
					i = Integer.parseInt(value);
					if (i > 0)
						width = i;
					break;
				case "HEIGHT":
					i = Integer.parseInt(value);
					if (i > 0)
						height = i;
					break;
				case "MUSIC":
					i = Integer.parseInt(value);
					if (i >= 0 && i <= 100)
						musicVolume = i / 100f;
					break;
				case "SOUND":
					i = Integer.parseInt(value);
					if (i >= 0 && i <= 100)
						soundVolume = i / 100f;
					break;
				case "FPS":
					i = Integer.parseInt(value);
					if (i > 0 && i < 240)
						fps = i;
					break;
				case "FONT":
					File newFont = new File(value);
					if (newFont.isFile())
						fontFile = newFont;
					break;
				case "AI_PLAYER":
					playerAI = AIType.valueOf(value);
					break;
				case "AI_OPPONENT":
					opponentAI = AIType.valueOf(value);
					break;
				default:
					try {
						Rule rule = Rule.valueOf(name);
						rule.setState(Boolean.parseBoolean(value));
					} catch (IllegalArgumentException e) {
						Log.warn(String.format("Failed to read line: %s", line));
						continue;
					}
				}
			}
		} catch (IOException e) {
			Log.error(String.format("Failed to read file '%s'.", OPTIONS_FILE.getAbsolutePath()), e);
		} catch (IllegalArgumentException e) {
			Log.warn("Format error in options file.", e);
			return;
		}
	}

	/**
	 * (Over)writes user options to a file.
	 */
	public static void saveOptions() {
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(OPTIONS_FILE), "utf-8"))) {
			// header
			SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
			String date = dateFormat.format(new Date());
			writer.write("# Triple Triad configuration");
			writer.newLine();
			writer.write("# last updated on ");
			writer.write(date);
			writer.newLine();
			writer.newLine();

			// game settings
			writer.write("# Game Settings");
			writer.newLine();
			writer.write(String.format("WIDTH = %d", width));
			writer.newLine();
			writer.write(String.format("HEIGHT = %d", height));
			writer.newLine();
			writer.write(String.format("MUSIC = %d", (int) (musicVolume * 100)));
			writer.newLine();
			writer.write(String.format("SOUND = %d", (int) (soundVolume * 100)));
			writer.newLine();
			writer.write(String.format("FPS = %d", fps));
			writer.newLine();
			writer.write(String.format("FONT = %s", fontFile.getName()));
			writer.newLine();
			writer.newLine();

			// AI
			writer.write("# AI Type (RANDOM, OFFENSIVE, DEFENSIVE, BALANCED)");
			writer.newLine();
			writer.write(String.format("AI_PLAYER = %s", playerAI.toString()));
			writer.newLine();
			writer.write(String.format("AI_OPPONENT = %s", opponentAI.toString()));
			writer.newLine();
			writer.newLine();

			// rules
			writer.write("# Rules");
			writer.newLine();
			for (Rule rule : Rule.values()) {
				writer.write(String.format("%s = %b", rule.toString(), rule.isActive()));
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			Log.error(String.format("Failed to write to file '%s'.", OPTIONS_FILE.getAbsolutePath()), e);
		}
	}
}