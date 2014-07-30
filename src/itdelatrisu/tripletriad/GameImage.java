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

import org.newdawn.slick.Image;
import org.newdawn.slick.util.Log;

/**
 * Game images.
 */
public enum GameImage {
	// backgrounds
	BOARD_MAT ("board-mat.jpg"),
//	BOARD_BG ("board-bg.jpg"),

	// cursor
	CURSOR ("cursor.png"),

	// card-related
	CARD_BACK ("card-back.png"),
	CARD_BLUE ("card-blue.png"),
	CARD_RED ("card-red.png"),
	CARD_GRAY ("card-gray.png"),

	// rank symbols
	RANK_0 ("rank-0.png"),
	RANK_1 ("rank-1.png"),
	RANK_2 ("rank-2.png"),
	RANK_3 ("rank-3.png"),
	RANK_4 ("rank-4.png"),
	RANK_5 ("rank-5.png"),
	RANK_6 ("rank-6.png"),
	RANK_7 ("rank-7.png"),
	RANK_8 ("rank-8.png"),
	RANK_9 ("rank-9.png"),
	RANK_A ("rank-A.png"),

	// score symbols
	SCORE_1 ("score-1.png"),
	SCORE_2 ("score-2.png"),
	SCORE_3 ("score-3.png"),
	SCORE_4 ("score-4.png"),
	SCORE_5 ("score-5.png"),
	SCORE_6 ("score-6.png"),
	SCORE_7 ("score-7.png"),
	SCORE_8 ("score-8.png"),
	SCORE_9 ("score-9.png"),

	// bonuses
	BONUS_PLUS ("bonus-plus.png"),
	BONUS_MINUS ("bonus-minus.png"),

	// special
	SPECIAL_SAME ("special-same.png"),
	SPECIAL_PLUS ("special-plus.png"),
	SPECIAL_COMBO ("special-combo.png"),

	// results
	RESULT_WIN ("result-win.png"),
	RESULT_LOSE ("result-lose.png"),
	RESULT_DRAW ("result-draw.png"),

	// info
	INFO_BOX ("info-box.png"),
	INFO_TEXT ("info-text.png");

	/** The associated file name. */
	private String filename;

	/** The associated image. */
	private Image img;

	/**
	 * Initializes all game images.
	 */
	public static void init() {
		for (GameImage o : GameImage.values()) {
			try {
				o.img = new Image(o.filename);
			} catch (Exception e) {
				Log.error(String.format("Failed to load image '%s'.", o.filename), e);
			}
		}
		scaleImages();
	}

	/**
	 * Scales all game images.
	 */
	private static void scaleImages() {
		int cardLength = Options.getCardLength();
		float baseScale = cardLength / 256f;

		float bgScale = (float) Options.getWidth() / BOARD_MAT.img.getWidth();
		BOARD_MAT.setScale(bgScale);
//		BOARD_BG.setScale(bgScale);

		CARD_BACK.setScale(cardLength, cardLength);
		CARD_BLUE.setScale(cardLength, cardLength);
		CARD_RED.setScale(cardLength, cardLength);
		CARD_GRAY.setScale(cardLength, cardLength);

		CURSOR.setScale(baseScale / 2.25f);

		int scoreLength = (int) (cardLength * 0.4f);
		SCORE_1.setScale(scoreLength, scoreLength);
		SCORE_2.setScale(scoreLength, scoreLength);
		SCORE_3.setScale(scoreLength, scoreLength);
		SCORE_4.setScale(scoreLength, scoreLength);
		SCORE_5.setScale(scoreLength, scoreLength);
		SCORE_6.setScale(scoreLength, scoreLength);
		SCORE_7.setScale(scoreLength, scoreLength);
		SCORE_8.setScale(scoreLength, scoreLength);
		SCORE_9.setScale(scoreLength, scoreLength);

		RESULT_WIN.setScale(baseScale);
		RESULT_LOSE.setScale(baseScale);
		RESULT_DRAW.setScale(baseScale);

		SPECIAL_SAME.setScale(baseScale);
		SPECIAL_PLUS.setScale(baseScale);
		SPECIAL_COMBO.setScale(baseScale);

		BONUS_PLUS.setScale(baseScale);
		BONUS_MINUS.setScale(baseScale);

		float infoScale = cardLength * 2.75f / 1024f;
		INFO_BOX.setScale(infoScale);
		INFO_TEXT.setScale(infoScale);
	}

	/**
	 * Returns the rank GameImage corresponding to the given integer rank.
	 * @param rank the rank [1, 10]
	 * @return the GameImage, or RANK_0 if invalid input
	 */
	public static GameImage getRank(int rank) {
		if (rank > 0 && rank < 10)
			return GameImage.valueOf(String.format("RANK_%d", rank));
		else if (rank == 10)
			return RANK_A;
		else
			return RANK_0;
	}

	/**
	 * Returns the score GameImage corresponding to the given integer score.
	 * @param score the score [1, 9]
	 * @return the GameImage, or null if invalid input
	 */
	public static GameImage getScore(int score) {
		if (score > 0 && score < 10)
			return GameImage.valueOf(String.format("SCORE_%d", score));
		else
			return null;
	}

	/**
	 * Constructor.
	 * @param filename the file name
	 */
	GameImage(String filename) {
		this.filename = filename;
	}

	/**
	 * Returns the image.
	 * @return the image
	 */
	public Image getImage() { return img; }

	/**
	 * Sets an image scale.
	 * @param scale the scale [0, 1]
	 */
	private void setScale(float scale) { img = img.getScaledCopy(scale); }

	/**
	 * Sets an image scale.
	 * @param width the scaled width
	 * @param height the scaled height
	 */
	private void setScale(int width, int height) { img = img.getScaledCopy(width, height); }
}
