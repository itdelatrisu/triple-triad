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
 * Generic game AI.
 */
public abstract class AI {
	/** The hand. */
	protected ArrayList<Card> hand;

	/** The game board. */
	protected Card[] board;

	/** The element board. */
	protected Element[] elements;

	/** Hand index of the next card to be played. */
	protected int nextIndex;

	/** Board position of the next card to be played.*/
	protected int nextPosition;

	/**
	 * Constructor.
	 * @param hand the hand of cards
	 * @param board the board
	 * @param elements the element board
	 */
	protected AI(ArrayList<Card> hand, Card[] board, Element[] elements) {
		this.hand = hand;
		this.board = board;
		this.elements = elements;
	}

	/**
	 * Calculates the next card index and position.
	 */
	public abstract void update();

	/**
	 * Returns the hand index of the next card to be played.
	 * @return the hand index
	 */
	public int nextIndex() { return nextIndex; }

	/**
	 * Returns the board position of the next card to be played.
	 * @return the board position
	 */
	public int nextPosition() { return nextPosition; }

	/**
	 * Returns a list containing all empty board positions [0, 8].
	 * @return an ArrayList of empty positions
	 */
	protected ArrayList<Integer> emptySpaces() {
		ArrayList<Integer> spaces = new ArrayList<Integer>();
		for (int i = 0; i < board.length; i++) {
			if (board[i] == null)
				spaces.add(i);
		}
		return spaces;
	}

	/**
	 * Returns the "rank difference" value of a card at a position.
	 * This takes elements into account, and is calculated using the formula:<ul>
	 * <li>rank_diff = (10 * (# open sides)) - sum(ranks of open sides)</ul>
	 * @param c the card
	 * @param position the board position
	 * @return the rank difference [0, 40]
	 */
	protected int getRankDiff(Card c, int position) {
		int totalRank = 0;
		int sides = 0;
		if (position % 3 != 0 && board[position - 1] == null) {
			totalRank += c.getRank(Card.Rank.LEFT);
			sides++;
		}
		if (position % 3 != 2 && board[position + 1] == null) {
			totalRank += c.getRank(Card.Rank.RIGHT);
			sides++;
		}
		if (position > 2 && board[position - 3] == null) {
			totalRank += c.getRank(Card.Rank.TOP);
			sides++;
		}
		if (position < 6 && board[position + 3] == null) {
			totalRank += c.getRank(Card.Rank.BOTTOM);
			sides++;
		}

		// element bonuses
		if (elements != null && elements[position] != Element.NEUTRAL)
			totalRank += ((c.getElement() == elements[position]) ? 1 : -1) * sides;

		return Math.max((sides * 10) - totalRank, 0);
	}

	/**
	 * Returns the sum "rank difference" of all of the owner's cards on the board.
	 * @return the board's rank difference
	 */
	private int getBoardRankDiff() {
		boolean owner = hand.get(0).getOwner();
		int totalRank = 0;
		for (int i = 0; i < board.length; i++) {
			if (board[i] != null && board[i].getOwner() == owner)
				totalRank += getRankDiff(board[i], i);
		}
		return totalRank;
	}

	/**
	 * Returns the "side rank difference" at a position.
	 * This takes elements into account, and is calculated using the formula:<ul>
	 * <li>side_rank_diff = (10 * (# adjacent cards)) - sum(facing ranks of adjacent cards)</ul>
	 * @param position the board position
	 * @return the side rank difference [-4, 40]
	 */
	private int sideRankDiff(int position) {
		boolean owner = hand.get(0).getOwner();
		int totalRank = 0;
		int sides = 0;
		if (position % 3 != 0 && board[position - 1] != null && board[position - 1].getOwner() == owner) {
			totalRank += board[position - 1].getRank(Card.Rank.RIGHT);
			if (elements != null && elements[position - 1] != Element.NEUTRAL)
				totalRank += (board[position - 1].getElement() == elements[position - 1]) ? 1 : -1;
			sides++;
		}
		if (position % 3 != 2 && board[position + 1] != null && board[position + 1].getOwner() == owner) {
			totalRank += board[position + 1].getRank(Card.Rank.LEFT);
			if (elements != null && elements[position + 1] != Element.NEUTRAL)
				totalRank += (board[position + 1].getElement() == elements[position + 1]) ? 1 : -1;
			sides++;
		}
		if (position > 2 && board[position - 3] != null && board[position - 3].getOwner() == owner) {
			totalRank += board[position - 3].getRank(Card.Rank.BOTTOM);
			if (elements != null && elements[position - 3] != Element.NEUTRAL)
				totalRank += (board[position - 3].getElement() == elements[position - 3]) ? 1 : -1;
			sides++;
		}
		if (position < 6 && board[position + 3] != null && board[position + 3].getOwner() == owner) {
			totalRank += board[position + 3].getRank(Card.Rank.TOP);
			if (elements != null && elements[position + 3] != Element.NEUTRAL)
				totalRank += (board[position + 3].getElement() == elements[position + 3]) ? 1 : -1;
			sides++;
		}

		return (sides * 10) - totalRank;
	}

	/**
	 * Uses the card and position creating the lowest "rank difference" of
	 * all the owner's cards on the board.
	 * @param spaces the list of empty spaces
	 */
	protected void useMinRankDiff(ArrayList<Integer> spaces) {
		int handSize = hand.size();
		int boardRankDiff = getBoardRankDiff();
		int minTotalRankDiff = Integer.MAX_VALUE;
		int nextLevel = -1;

		// use lowest level card possible, except if starting second and on last turn
		boolean useLowestLevel = ((spaces.size() % 2 > 0) || handSize != 2);

		for (int space : spaces) {
			int sideRankDiff = sideRankDiff(space);
			for (int index = 0; index < handSize; index++) {
				Card c = hand.get(index);
				int totalRankDiff = boardRankDiff + getRankDiff(c, space) - sideRankDiff;
				if (totalRankDiff < minTotalRankDiff ||
					(totalRankDiff == minTotalRankDiff && (
						(useLowestLevel && c.getLevel() < nextLevel) ||
						(!useLowestLevel && c.getLevel() > nextLevel)
					)
				)) {
					minTotalRankDiff = totalRankDiff;
					nextLevel = c.getLevel();
					nextIndex = index;
					nextPosition = space;
				}
			}
		}
	}
}