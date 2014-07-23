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

/**
 * Game rules.
 */
public enum Rule {
	OPEN (false),
//	RANDOM (true),
	SAME (true),
	SAME_WALL (true),
	PLUS (true),
	COMBO (true),
	ELEMENTAL (true),
	SUDDEN_DEATH (true);

	/**
	 * Whether or not the rule is active.
	 */
	private boolean state;

	/**
	 * Constructor.
	 * @param state the default state
	 */
	Rule(boolean state) {
		this.state = state;
	}

	/**
	 * Returns whether or not the rule is active.
	 * @return true if active
	 */
	public boolean isActive() { return state; }

	/**
	 * Sets the active state.
	 * @param state true if active
	 */
	public void setState(boolean state) { this.state = state; }

	/**
	 * Toggles the active state.
	 */
	public void toggle() { state = !state; }
}
