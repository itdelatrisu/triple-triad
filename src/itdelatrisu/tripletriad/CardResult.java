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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Data type determining the results of placing a card.
 */
public class CardResult {
	/** Card result lists. */
	private ArrayList<Card> captured, same, plus;

	/** Combo lists. */
	private LinkedList<ArrayList<Card>> combo;

	/** Total number of cards captured in this result (all rules and combos). */
	private int capturedCount = 0;

	/** Source card. */
	private Card card;

	/** Source card position. */
	private int position;

	/** Current board. */
	private Card[] board;

	/** Current element board. */
	private Element[] elements;

	/** Card owners on the board. */
	private boolean[] owners;

	/** Card sums list. */
	private HashMap<Integer, ArrayList<Card>> sums;

	/** Whether or not "Same Wall" has been triggered. */
	private boolean sameWall = false;

	/**
	 * Constructor.
	 * @param card source card
	 * @param position source card position
	 * @param board current board
	 * @param elements current element board
	 */
	public CardResult(Card card, int position, Card[] board, Element[] elements) {
		this.card = card;
		this.position = position;
		this.board = board;
		this.elements = elements;

		// store owners (for "combo")
		if (Rule.COMBO.isActive()) {
			this.owners = new boolean[9];
			for (int i = 0; i < 9; i++) {
				if (board[i] != null)
					owners[i] = board[i].getOwner();
			}
		}

		this.captured = new ArrayList<Card>();
		if (Rule.SAME.isActive())
			this.same = new ArrayList<Card>();
		if (Rule.PLUS.isActive())
			this.sums = new HashMap<Integer, ArrayList<Card>>();

		// process card results on all sides (if valid)
		// set "Same Wall" status on borders
		if (position % 3 != 0)
			calcResult(card, position, Card.Rank.LEFT, position - 1, Card.Rank.RIGHT);
		else if (card.getRank(Card.Rank.LEFT) == 10)
			sameWall = true;
		if (position % 3 != 2)
			calcResult(card, position, Card.Rank.RIGHT, position + 1, Card.Rank.LEFT);
		else if (card.getRank(Card.Rank.RIGHT) == 10)
			sameWall = true;
		if (position > 2)
			calcResult(card, position, Card.Rank.TOP, position - 3, Card.Rank.BOTTOM);
		else if (card.getRank(Card.Rank.TOP) == 10)
			sameWall = true;
		if (position < 6)
			calcResult(card, position, Card.Rank.BOTTOM, position + 3, Card.Rank.TOP);
		else if (card.getRank(Card.Rank.BOTTOM) == 10)
			sameWall = true;

		// check captured
		if (captured.size() < 1)
			captured = null;

		// check "same"
		if (same != null) {
			boolean isValid = false;
			int minSize = (sameWall && Rule.SAME_WALL.isActive()) ? 1 : 2;
			if (same.size() >= minSize) {
				for (Card c : same) {
					if (c.getOwner() != card.getOwner()) {
						isValid = true;
						capturedCount++;
					}
				}
			}
			if (isValid) {
				sums = null;
				filterCaptured(same);
				calcCombo(same);
			} else
				same = null;
		}

		// check "plus"
		if (sums != null) {
			for (ArrayList<Card> sumList : sums.values()) {
				if (sumList.size() >= 2) {
					boolean isValid = false;
					for (Card c : sumList) {
						if (c.getOwner() != card.getOwner()) {
							isValid = true;
							capturedCount++;
						}
					}
					if (isValid) {
						plus = sumList;
						filterCaptured(plus);
						calcCombo(plus);
						break;
					}
				}
			}
			sums = null;
		}

		if (captured != null)
			capturedCount += captured.size();
	}

	/**
	 * Processes card results for a source and target card.
	 * @param source the source card
	 * @param sourcePosition the source board position
	 * @param sourceLocation the source rank location
	 * @param targetPosition the target board position
	 * @param targetLocation the target rank location
	 */
	private void calcResult(Card source, int sourcePosition, Card.Rank sourceLocation,
			int targetPosition, Card.Rank targetLocation) {
		Card target = board[targetPosition];
		if (target == null)  // target position is empty
			return;

		int sourceRank = source.getRank(sourceLocation);
		int targetRank = target.getRank(targetLocation);

		// add to "same" list
		if (same != null) {
			if (sourceRank == targetRank)
				same.add(target);
		}

		// add to "plus" list
		if (sums != null) {
			int sum = sourceRank + targetRank;
			if (sums.containsKey(sum))
				sums.get(sum).add(target);
			else {
				ArrayList<Card> sumList = new ArrayList<Card>();
				sumList.add(target);
				sums.put(sum, sumList);
			}
		}

		// add to "captured" list
		if (captures(source, sourcePosition, sourceLocation, source.getOwner(),
				targetPosition, targetLocation, target.getOwner()))
			captured.add(target);
	}

	/**
	 * Returns whether or not the source card rank is greater than
	 * the target card rank, taking elements into account.
	 * @param source the source card
	 * @param sourcePosition the source board position
	 * @param sourceLocation the source rank location
	 * @param sourceOwner the source card owner
	 * @param targetPosition the target board position
	 * @param targetLocation the target rank location
	 * @param targetOwner the target card owner
	 * @return true if source card "captures" target card
	 */
	private boolean captures(Card source, int sourcePosition,
			Card.Rank sourceLocation, boolean sourceOwner,
			int targetPosition, Card.Rank targetLocation, boolean targetOwner) {
		Card target = board[targetPosition];
		if (target == null)  // target position is empty
			return false;

		if (sourceOwner == targetOwner)  // same owner: don't check capture
			return false;

		int sourceRank = source.getRank(sourceLocation);
		int targetRank = target.getRank(targetLocation);
		if (elements != null) {
			// element bonuses
			if (elements[position] != Element.NEUTRAL)
				sourceRank += (source.getElement() == elements[sourcePosition]) ? 1 : -1;
			if (elements[targetPosition] != Element.NEUTRAL)
				targetRank += (target.getElement() == elements[targetPosition]) ? 1 : -1;
		}
		return (sourceRank > targetRank);
	}

	/**
	 * Removes all cards in the captured list contained in the given result list.
	 * If the captured list is empty afterwards, it will be set to null.
	 * @param resultList the list of cards to be used as a filter
	 */
	private void filterCaptured(ArrayList<Card> resultList) {
		if (captured == null)
			return;

		Iterator<Card> iter = captured.iterator();
		while (iter.hasNext()) {
			Card c = iter.next();
			if (resultList.contains(c))
				iter.remove();
			else if (owners != null)
				owners[c.getPosition()] = card.getOwner();
		}
		if (captured.isEmpty())
			captured = null;
	}

	/**
	 * Checks for and processes combos.
	 * @param resultList the list of cards used to initiate the combo
	 */
	private void calcCombo(ArrayList<Card> resultList) {
		if (owners == null)  // rule not active
			return;

		// change owners on copied owners board
		boolean cardOwner = card.getOwner();
		for (Card c : resultList)
			owners[c.getPosition()] = cardOwner;

		// calculate captures
		HashSet<Card> comboSet = new HashSet<Card>(3);
		for (Card c : resultList) {
			int pos = c.getPosition();
			if (pos % 3 != 0 &&
				captures(c, pos, Card.Rank.LEFT, owners[pos], pos - 1, Card.Rank.RIGHT, owners[pos - 1]))
				comboSet.add(board[pos - 1]);
			if (pos % 3 != 2 &&
				captures(c, pos, Card.Rank.RIGHT, owners[pos], pos + 1, Card.Rank.LEFT, owners[pos + 1]))
				comboSet.add(board[pos + 1]);
			if (pos > 2 &&
				captures(c, pos, Card.Rank.TOP, owners[pos], pos - 3, Card.Rank.BOTTOM, owners[pos - 3]))
				comboSet.add(board[pos - 3]);
			if (pos < 6 &&
				captures(c, pos, Card.Rank.BOTTOM, owners[pos], pos + 3, Card.Rank.TOP, owners[pos + 3]))
				comboSet.add(board[pos + 3]);
		}
		if (comboSet.isEmpty())  // no captures
			return;

		// add new "combo" list
		ArrayList<Card> comboList = new ArrayList<Card>(comboSet);
		capturedCount += comboList.size();
		if (combo == null)
			combo = new LinkedList<ArrayList<Card>>();
		combo.add(comboList);

		// chain combos
		calcCombo(comboList);
	}

	/**
	 * Returns whether or not the result contains a normal capture.
	 * @return true if capture
	 */
	public boolean hasCapture() { return captured != null; }

	/**
	 * Returns whether or not the result invoked the "Same" rule.
	 * @return true if "Same"
	 */
	public boolean isSame() { return same != null; }

	/**
	 * Returns whether or not the result invoked the "Plus" rule.
	 * @return true if "Plus"
	 */
	public boolean isPlus() { return plus != null; }

	/**
	 * Returns whether or not there are any (remaining) "Combo" lists.
	 * @return true if lists exist
	 */
	public boolean hasCombo() { return (combo != null && !combo.isEmpty()); }

	/**
	 * Returns the list of captured cards.
	 * @return captured card list, or null if "Same" or "Plus" rule invoked
	 */
	public ArrayList<Card> getCapturedList() { return captured; }

	/**
	 * Returns the list of cards invoking the "Same" rule.
	 * @return "Same" card list, or null if rule not invoked
	 */
	public ArrayList<Card> getSameList() { return same; }

	/**
	 * Returns the list of cards invoking the "Plus" rule.
	 * @return "Plus" card list, or null if rule not invoked
	 */
	public ArrayList<Card> getPlusList() { return plus; }

	/**
	 * Returns the next "Combo" list, if any exist.
	 * @return "Combo" card list, or null if none (remaining)
	 */
	public ArrayList<Card> nextCombo() { return hasCombo() ? combo.remove() : null; }

	/**
	 * Returns the total number of cards captured in this result, including
	 * all rules and combos. 
	 * @return the number of captured cards
	 */
	public int getCapturedCount() { return capturedCount; }

	/**
	 * Returns the source card for this result.
	 * @return the source Card
	 */
	public Card getSourceCard() { return card; }
}