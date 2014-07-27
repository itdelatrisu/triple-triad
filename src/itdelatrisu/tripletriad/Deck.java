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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import org.newdawn.slick.util.Log;
import org.newdawn.slick.util.ResourceLoader;

/**
 * Deck data type.
 */
public class Deck {
	/** List of cards. */
	private ArrayList<Card> deck;

	/**
	 * Creates a deck by parsing all cards.
	 */
	public Deck() {
		deck = new ArrayList<Card>();

		/*
		 * Data file format (all elements separated by tabs):
		 * - ID
		 * - Name
		 * - Ranks ({top}{left}{right}{bottom})
		 * - Element (all caps)
		 * - Level
		 */
		try (BufferedReader in = new BufferedReader(new InputStreamReader(
				ResourceLoader.getResourceAsStream(Options.DATA_FILE)))) {
			String line;
			while ((line = in.readLine()) != null) {
				// create an object only for valid input
				String[] tokens = line.split("\\t");
				if (tokens.length != 5) {
					Log.warn(String.format("Failed to parse line: %s", line));
					continue;
				}

				// ID
				int id = 0;
				try {
					id = Integer.parseInt(tokens[0]);
				} catch (NumberFormatException e) {
					Log.warn(String.format("Failed to parse ID in line: %s", line), e);
					continue;
				}
				if (id < 0) {
					Log.warn(String.format("Failed to parse ID in line: %s", line));
					continue;
				}

				// name (no checks)
				String name = tokens[1];

				// ranks
				String ranks = tokens[2];
				if (!ranks.matches("[1-9A]{4}")) {
					Log.warn(String.format("Failed to parse ranks for card %d: %s", id, tokens[2]));
					continue;
				}

				// element
				Element element;
				try {
					element = Element.valueOf(tokens[3]);
				} catch (IllegalArgumentException e) {
					Log.warn(String.format("Failed to parse element for card %d: %s", id, tokens[3]));
					continue;
				}

				// level
				int level = 0;
				try {
					level = Integer.parseInt(tokens[0]);
				} catch (NumberFormatException e) {
					Log.warn(String.format("Failed to parse level in line: %s", line), e);
					continue;
				}
				if (level < 0) {
					Log.warn(String.format("Failed to parse level in line: %s", line));
					continue;
				}

				deck.add(new Card(id, name, ranks, element, level));
			}
		} catch (IOException e) {
			Log.error("Failed to read card data.", e);
		}
	}

	/**
	 * Builds two hands of cards, without repeats.
	 * @param playerCards the player hand
	 * @param opponentCards the opponent hand
	 */
	public void buildHands(Card[] playerCards, Card[] opponentCards) {
		// deck size too small
		if (deck.size() < playerCards.length + opponentCards.length) {
			Log.error("Not enough cards loaded (10 minimum).");
			return;
		}

		Collections.shuffle(deck);
		for (int i = 0; i < playerCards.length; i++) {
			playerCards[i] = new Card(deck.get(i));
			playerCards[i].setOwner(TripleTriad.PLAYER);
		}
		for (int i = 0; i < opponentCards.length; i++) {
			opponentCards[i] = new Card(deck.get(i + playerCards.length));
			opponentCards[i].setOwner(TripleTriad.OPPONENT);
		}
	}
}