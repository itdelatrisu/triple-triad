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
import itdelatrisu.tripletriad.Element;

import java.util.ArrayList;

/**
 * Random AI.
 * Always chooses random moves.
 */
public class RandomAI extends AI {
	/**
	 * Random-moves AI constructor.
	 * @param hand the hand of cards
	 * @param board the board
	 * @param elements the element board
	 * @see itdelatrisu.tripletriad.ai.AI#AI(ArrayList, Card[], Element[])
	 */
	public RandomAI(ArrayList<Card> hand, Card[] board, Element[] elements) {
		super(hand, board, elements);
	}

	@Override
	public void update() {
		ArrayList<Integer> spaces = emptySpaces();
		nextIndex = (int) (Math.random() * hand.size());
		nextPosition = spaces.get((int) (Math.random() * spaces.size()));
		return;
	}
}