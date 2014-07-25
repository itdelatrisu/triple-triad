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

package itdelatrisu.tripletriad.ai;

import itdelatrisu.tripletriad.Card;
import itdelatrisu.tripletriad.CardResult;
import itdelatrisu.tripletriad.Element;

import java.util.ArrayList;

/**
 * Balanced AI.
 * Weighs capture count against best card placement for each move.
 */
public class BalancedAI extends AI {
	/**
	 * Balanced AI constructor.
	 * @param hand the hand of cards
	 * @param board the board
	 * @param elements the element board
	 * @see itdelatrisu.tripletriad.ai.AI#AI(ArrayList, Card[], Element[])
	 */
	public BalancedAI(ArrayList<Card> hand, Card[] board, Element[] elements) {
		super(hand, board, elements);
	}

	@Override
	public void update() {
		int handSize = hand.size();
		ArrayList<Integer> spaces = emptySpaces();

		// use lowest level card possible, except if starting second and on last turn
		boolean useLowestLevel = ((spaces.size() % 2 > 0) || handSize != 2);

		// find move with max number of captured cards
		int maxCapture = -1;
		int nextRankDiff = 41;
		int nextLevel = -1;
		for (int space : spaces) {
			for (int index = 0; index < handSize; index++) {
				Card c = hand.get(index);
				CardResult result = new CardResult(c, space, board, elements);
				int captureCount = result.getCapturedCount();
				int rankDiff = getRankDiff(c, space);

				// determine whether or not to use this result...
				boolean isValid = false;
				if (maxCapture == -1)
					isValid = true;
				else if (captureCount > maxCapture) {
					if ((captureCount > 2) || nextRankDiff - rankDiff > -5)
						isValid = true;
				} else if (captureCount == maxCapture) {
					if (rankDiff < nextRankDiff ||
						(rankDiff == nextRankDiff && (
							(useLowestLevel && c.getLevel() < nextLevel) ||
							(!useLowestLevel && c.getLevel() > nextLevel)
						)
					))
						isValid = true;
				} else if (captureCount == maxCapture - 1) {
					if (nextRankDiff - rankDiff > 5)
						isValid = true;
				}

				if (isValid) {
					maxCapture = captureCount;
					nextRankDiff = rankDiff;
					nextLevel = c.getLevel();
					nextIndex = index;
					nextPosition = space;
				}
			}
		}

		// no capture possible: find lowest total rank difference
		if (maxCapture == 0 && spaces.size() != 9)
			useMinRankDiff(spaces);
	}
}
