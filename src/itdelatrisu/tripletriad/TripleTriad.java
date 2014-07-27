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

import itdelatrisu.tripletriad.ai.AI;
import itdelatrisu.tripletriad.ai.BalancedAI;
import itdelatrisu.tripletriad.ai.DefensiveAI;
import itdelatrisu.tripletriad.ai.OffensiveAI;
import itdelatrisu.tripletriad.ai.RandomAI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.DefaultLogSystem;
import org.newdawn.slick.util.FileSystemLocation;
import org.newdawn.slick.util.Log;
import org.newdawn.slick.util.ResourceLoader;

/**
 * Main class.
 */
public class TripleTriad extends BasicGame {
	/** Player/opponent constants. */
	public static final boolean PLAYER = true, OPPONENT = false;

	/** Wait time unit, in milliseconds, between actions. */
	private static final int WAIT_TIME = 1000;

	/** Delay timer. */
	private int timer;

	/** The deck of cards. */
	private Deck deck;

	/** Current board. */
	private Card[] board;

	/** Elements on board. */
	private Element[] elements;

	/** Original hands. */
	private Card[] playerCards, opponentCards;

	/** Current hands. */
	private ArrayList<Card> playerHand, opponentHand;

	/** The AIs. */
	private AI playerAI, opponentAI;

	/** Current card result. */
	private CardResult result;

	/** Whether the currently processing result is a "Combo". */
	private boolean isCombo;

	/** Score. */
	private int playerScore, opponentScore;

	/** Turn (PLAYER or OPPONENT). */
	private boolean turn;

	/** Selected card index. */
	private int selectedCard;

	/** Selected board position. */
	private int selectedPosition;

	/** Whether the game has loaded. */
	private boolean init;

	/** Card loading: current count. */
	private int loadCardCount;

	/** Card loading: current offset. */
	private float loadCardOffset;

	/** Alpha level for special text images. */
	private float textAlpha;

	/** Spinner. */
	private Spinner spinner;

	/** Game container. */
	private GameContainer container;

	public TripleTriad() {
		super("Triple Triad");
	}

	public static void main(String[] args) {
		// log all errors to a file
		Log.setVerbose(false);
		try {
			DefaultLogSystem.out = new PrintStream(new FileOutputStream(Options.LOG_FILE, true));
		} catch (FileNotFoundException e) {
			Log.error(e);
		}
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				Log.error("** Uncaught Exception! **", e);
			}
		});

		// parse configuration file
		Options.parseOptions();

		// set path for lwjgl natives - NOT NEEDED if using JarSplice
//		System.setProperty("org.lwjgl.librarypath", new File("native").getAbsolutePath());

		// set the resource paths
		ResourceLoader.addResourceLocation(new FileSystemLocation(new File("./res/")));
		ResourceLoader.addResourceLocation(new FileSystemLocation(new File("./cards/")));

		// start the game
		try {
			AppGameContainer app = new AppGameContainer(new TripleTriad());

			// game settings
			Options.setDisplayMode(app);
			String[] icons = { "icon16.png", "icon32.png" };
			app.setIcons(icons);

			app.start();
		} catch (SlickException e) {
			// JARs will not run properly inside directories containing '!'
			// http://bugs.java.com/view_bug.do?bug_id=4523159
			if (new File("").getAbsolutePath().contains("!"))
				Log.error("Cannot run JAR from path containing '!'.");
			else
				Log.error("Error while creating game container.", e);
		}
	}

	@Override
	public void init(GameContainer container) throws SlickException {
		this.container = container;

		// initialize everything
		Options.init(container);
		AudioController.init();
		GameImage.init();
		Element.init();
		Spinner.init();

		// build deck
		this.deck = new Deck();

		restart(true);
	}

	@Override
	public void render(GameContainer container, Graphics g)
			throws SlickException {
		int width = container.getWidth();
		int height = container.getHeight();
		int cardLength = Options.getCardLength();

		// board
		GameImage.BOARD_MAT.getImage().drawCentered(width / 2, height / 2);

		// card loading
		if (!init) {
			for (int i = 0, N = Math.min(loadCardCount, 5); i < N; i++)
				opponentHand.get(i).drawInHand(i, false);
			if (loadCardCount >= 5) {
				for (int i = 0, N = loadCardCount - 5; i < N; i++)
					playerHand.get(i).drawInHand(i, false);
			}

			if (loadCardCount < 5)
				opponentHand.get(loadCardCount).drawInHand(loadCardOffset, false);
			else if (loadCardCount < 10)
				playerHand.get(loadCardCount % 5).drawInHand(loadCardOffset, false);
			else if (timer < 1000)
				spinner.drawCentered(width / 2, height / 2);
			else
				spinner.getFrame((turn == PLAYER) ? 1 : 3).drawCentered(width / 2, height / 2);
			return;
		}

		// cards (hand)
		boolean isPlayerTurn = (turn == PLAYER);
		boolean noSelect = (result != null || isGameOver());
		for (int i = 0, N = playerHand.size(); i < N; i++)
			playerHand.get(i).drawInHand(i + (5 - N), (isPlayerTurn && selectedCard == i && !noSelect));
		for (int i = 0, N = opponentHand.size(); i < N; i++)
			opponentHand.get(i).drawInHand(i + (5 - N), (!isPlayerTurn && selectedCard == i && !noSelect));

		// cards (board)
		for (int i = 0; i < board.length; i++) {
			if (board[i] != null)
				board[i].drawOnBoard();
		}

		// spinner
		if (!isGameOver()) {
			spinner.drawCentered(
				(width / 2) + ((cardLength * 1.95f) * ((turn == PLAYER) ? 1 : -1)),
				(height / 2) - (cardLength * 1.5f)
			);
		}

		// score
		float scoreHeight = (height / 2) + (cardLength * 1.4f);
		GameImage.getScore(playerScore).getImage().drawCentered(
			(width / 2) + (cardLength * 2.1f), scoreHeight
		);
		GameImage.getScore(opponentScore).getImage().drawCentered(
			(width / 2) - (cardLength * 2.1f), scoreHeight
		);

		// elements
		if (elements != null) {
			for (int i = 0; i < elements.length; i++)
				elements[i].drawOnBoard(i, board[i]);
		}

		// card result
		if (result != null) {
			Image img = null;
			if (isCombo)
				img = GameImage.SPECIAL_COMBO.getImage();
			else if (result.isSame())
				img = GameImage.SPECIAL_SAME.getImage();
			else
				img = GameImage.SPECIAL_PLUS.getImage();
			if (img != null) {
				img.setAlpha(textAlpha);
				img.drawCentered(width / 2, height / 2);
			}
			return;
		}

		// game over
		if (isGameOver()) {
			GameImage result =
				(playerScore > opponentScore) ? GameImage.RESULT_WIN :
				(playerScore < opponentScore) ? GameImage.RESULT_LOSE :
				                                GameImage.RESULT_DRAW;
			result.getImage().setAlpha(textAlpha);
			result.getImage().drawCentered(width / 2, height / 2);
			return;
		}

		// player turn...
		if (isPlayerTurn && !playerHand.isEmpty()) {
			// cursor
			Image cursor = GameImage.CURSOR.getImage();
			cursor.setAlpha(1f);
			if (selectedPosition != -1) {
				cursor.drawCentered(
					(width / 2) - ((1 - (selectedPosition % 3)) * cardLength) - cursor.getWidth(),
					(height / 2) - ((1 - (selectedPosition / 3)) * cardLength)
				);
				cursor.setAlpha(0.5f);
			}
			int pos = selectedCard + (5 - playerHand.size());
			cursor.draw(
				(width / 2) + (cardLength * 1.5f) - (cursor.getWidth() / 1.25f),
				(height / 2) - cardLength + (pos * cardLength / 2f)
			);

			// card name
			String name;
			if (selectedPosition != -1) {
				if (board[selectedPosition] != null)
					name = board[selectedPosition].getName();
				else
					name = "";
			} else
				name = playerHand.get(selectedCard).getName();
			if (!name.isEmpty()) {
				Image infoBox = GameImage.INFO_BOX.getImage();
				float infoX = (width - infoBox.getWidth()) / 2;
				float infoY = (height - infoBox.getHeight()) / 2 + (cardLength * 1.4f);
				infoBox.draw(infoX, infoY);
				GameImage.INFO_TEXT.getImage().draw(
					infoX + (infoBox.getWidth() * 0.015f),
					infoY + (infoBox.getHeight() * 0.015f)
				);
				Options.getFont().drawString(
					(width - Options.getFont().getWidth(name)) / 2,
					infoY + ((infoBox.getHeight() - Options.getFont().getLineHeight()) / 2),
					name, Color.white
				);
			}
		}
	}

	@Override
	public void update(GameContainer container, int delta)
			throws SlickException {
		// card loading
		if (!init) {
			// sound effect timer
			if (timer > 0) {
				if (timer < 1500)  // "start" sound effect length
					timer += delta;
				else {  // start game
					timer = 0;
					spinner.setSpeed(1f);
					init = true;
				}
				return;
			}

			int targetOffset = loadCardCount % 5;
			if (loadCardOffset > targetOffset)
				loadCardOffset -= (delta / 25f);

			// next card
			if (loadCardOffset <= targetOffset) {
				if (++loadCardCount > 9) {  // finished animating: play sound effect
					AudioController.Effect.START.play();
					timer = 1;
				} else {
					loadCardOffset = 3 + (float) container.getHeight() / Options.getCardLength();
					AudioController.Effect.CARD.play();
				}
			}
			return;
		}

		// card result
		if (result != null) {
			// card playing
			if (Card.isCardPlaying()) {
				Card.update(delta);
				if (!Card.isCardPlaying()) {
					// change card owners and adjust score
					if (result.isSame()) {
						AudioController.Effect.SPECIAL.play();
						cardResult(result.getSameList());
					} else if (result.isPlus()) {
						AudioController.Effect.SPECIAL.play();
						cardResult(result.getPlusList());
					}
					if (result.hasCapture())
						cardResult(result.getCapturedList());
				}
				return;
			}

			Card.update(delta);
			if (!result.isSame() && !result.isPlus()) {
				if (!Card.isColorChange()) {  // finish color change animation
					result = null;
					turn = !turn;
				}
				return;
			}

			if (textAlpha < 1f)  // main text ("same" or "plus")
				textAlpha += (delta / 500f);
			else if (result.hasCombo()) {  // combo action and text
				if (timer < WAIT_TIME / 2)  // delay
					timer += delta;
				else {
					timer = 0;
					cardResult(result.nextCombo());
					if (!isCombo) {
						textAlpha = 0f;
						isCombo = true;
					}
				}
			} else if (timer < WAIT_TIME / 2)  // delay
				timer += delta;
			else if (!Card.isColorChange()) {  // reset
				textAlpha = 0f;
				timer = 0;
				result = null;
				isCombo = false;
				turn = !turn;
			}
			return;
		}

		// game over
		if (isGameOver()) {
			// fade in result
			if (textAlpha < 1f)
				textAlpha += (delta / 750f);

			// sudden death
			else if (Rule.SUDDEN_DEATH.isActive() && playerScore == opponentScore) {
				if (timer < WAIT_TIME / 2)
					timer += delta;
				else
					restart(false);
			}
			return;
		}

		// opponent turn
		if (turn == OPPONENT) {
			if (timer == 0) {  // calculate next move
				opponentAI.update();
				timer += delta;
			} else if (timer < WAIT_TIME) {  // delay, move card
				int nextIndex = opponentAI.nextIndex();
				if (selectedCard < nextIndex &&
					timer >= (selectedCard + 1) * WAIT_TIME / (nextIndex + 1))
					selectedCard++;
				timer += delta;
			} else {  // play card
				playCard(opponentHand, opponentAI.nextIndex(), opponentAI.nextPosition());
				timer = 0;
			}
			return;
		}
	}

	@Override
	public boolean closeRequested() {
		Options.saveOptions();
		return true;
	}

	@Override
	public void keyPressed(int key, char c) {
		// exit
		if (key == Input.KEY_ESCAPE) {
			Options.saveOptions();
			container.exit();
			return;
		}

		// restart game
		if (key == Input.KEY_F5 || (
			isGameOver() && (playerScore != opponentScore) &&
			textAlpha >= 1f && result == null &&
			(key == Input.KEY_Z || key == Input.KEY_ENTER)
		)) {
			restart(true);
			return;
		}

		// not player turn
		if (turn != PLAYER || !init || result != null || isGameOver())
			return;

		switch (key) {
		case Input.KEY_DOWN:
			if (selectedPosition == -1) {
				selectedCard = (selectedCard + 1) % playerHand.size();
				AudioController.Effect.SELECT.play();
			} else {
				if (selectedPosition < 6) {
					selectedPosition += 3;
					AudioController.Effect.SELECT.play();
				}
			}
			break;
		case Input.KEY_UP:
			if (selectedPosition == -1) {
				int size = playerHand.size();
				selectedCard = (selectedCard + (size - 1)) % size;
				AudioController.Effect.SELECT.play();
			} else {
				if (selectedPosition > 2) {
					selectedPosition -= 3;
					AudioController.Effect.SELECT.play();
				}
			}
			break;
		case Input.KEY_LEFT:
			if (selectedPosition != -1 && selectedPosition % 3 != 0) {
				selectedPosition--;
				AudioController.Effect.SELECT.play();
			}
			break;
		case Input.KEY_RIGHT:
			if (selectedPosition != -1 && selectedPosition % 3 != 2) {
				selectedPosition++;
				AudioController.Effect.SELECT.play();
			}
			break;
		case Input.KEY_Z:
		case Input.KEY_ENTER:
			if (selectedPosition == -1) {
				selectedPosition = 4;
				AudioController.Effect.SELECT.play();
			} else {
				if (playCard(playerHand, selectedCard, selectedPosition))
					AudioController.Effect.SELECT.play();
				else
					AudioController.Effect.INVALID.play();
			}
			break;
		case Input.KEY_X:
		case Input.KEY_BACK:
			if (selectedPosition != -1) {
				selectedPosition = -1;
				AudioController.Effect.BACK.play();
			}
			break;
		case Input.KEY_F1:
			playerAI.update();
			selectedCard = playerAI.nextIndex();
			selectedPosition = playerAI.nextPosition();
			playCard(playerHand, selectedCard, selectedPosition);
			AudioController.Effect.SELECT.play();
			break;
		}
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		if (button != Input.MOUSE_LEFT_BUTTON)
			return;

		// restart game
		if (isGameOver() && (playerScore != opponentScore) &&
			textAlpha >= 1f && result == null) {
			restart(true);
			return;
		}

		// not player turn
		if (turn != PLAYER || !init || result != null || isGameOver())
			return;

		int cardLength = Options.getCardLength();
		int centerX = container.getWidth() / 2;
		int centerY = container.getHeight() / 2;

		// player hand
		for (int i = 0, handSize = playerHand.size(); i < handSize; i++) {
			int index = handSize - i - 1;
			int posX = centerX + (int) (cardLength * ((selectedCard == index) ? 1.45f : 1.6f));
			int posY = centerY - ((i - 1) * cardLength / 2);
			if (x >= posX && x < posX + cardLength &&
				y >= posY && y < posY + cardLength) {
				if (selectedCard == index) {
					if (selectedPosition == -1) {
						selectedPosition = 4;
						AudioController.Effect.SELECT.play();
					} else {
						selectedPosition = -1;
						AudioController.Effect.BACK.play();
					}
				} else {
					selectedCard = index;
					selectedPosition = -1;
					AudioController.Effect.SELECT.play();
				}
				return;
			}
		}

		// board
		int centerOffset = cardLength * 3 / 2;
		if (x >= centerX - centerOffset && x < centerX + centerOffset &&
			y >= centerY - centerOffset && y < centerY + centerOffset) {
			int boardPosition =
					(x - (centerX - centerOffset)) / cardLength +
					(y - (centerY - centerOffset)) / cardLength * 3;
			if (selectedPosition != boardPosition) {
				selectedPosition = boardPosition;
				AudioController.Effect.SELECT.play();
			} else if (playCard(playerHand, selectedCard, boardPosition))
				AudioController.Effect.SELECT.play();
			else
				AudioController.Effect.INVALID.play();
			return;
		}
	}

	/**
	 * Re-initializes the game.
	 * @param newHand whether or not to generate new hands (e.g. false for Sudden Death)
	 */
	private void restart(boolean newHand) {
		if (newHand) {
			playerCards = new Card[5];
			opponentCards = new Card[5];
			deck.buildHands(playerCards, opponentCards);
			playerHand = new ArrayList<Card>(Arrays.asList(playerCards));
			opponentHand = new ArrayList<Card>(Arrays.asList(opponentCards));
			spinner = Spinner.getRandomSpinner();
			spinner.setSpeed(5f);
			init = false;
		} else {
			playerHand.clear();
			opponentHand.clear();

			// build new hands from owned cards
			for (int i = 0; i < 5; i++) {
				// determine new owners
				if (playerCards[i].getOwner() == PLAYER)
					playerHand.add(playerCards[i]);
				else
					opponentHand.add(playerCards[i]);
				if (opponentCards[i].getOwner() == PLAYER)
					playerHand.add(opponentCards[i]);
				else
					opponentHand.add(opponentCards[i]);

				// reset the card positions
				playerCards[i].resetPosition();
				opponentCards[i].resetPosition();
			}
		}

		// reset game data
		board = new Card[9];
		elements = (Rule.ELEMENTAL.isActive()) ? Element.getRandomBoard() : null;
		switch (Options.getOpponentAI()) {
			case RANDOM: opponentAI = new RandomAI(opponentHand, board, elements); break;
			case OFFENSIVE: opponentAI = new OffensiveAI(opponentHand, board, elements); break;
			case DEFENSIVE: opponentAI = new DefensiveAI(opponentHand, board, elements); break;
			case BALANCED: opponentAI = new BalancedAI(opponentHand, board, elements); break;
		}
		switch (Options.getPlayerAI()) {
			case RANDOM: playerAI = new RandomAI(playerHand, board, elements); break;
			case OFFENSIVE: playerAI = new OffensiveAI(playerHand, board, elements); break;
			case DEFENSIVE: playerAI = new DefensiveAI(playerHand, board, elements); break;
			case BALANCED: playerAI = new BalancedAI(playerHand, board, elements); break;
		}
		result = null;
		isCombo = false;
		playerScore = opponentScore = 5;
		turn = new Random().nextBoolean();
		selectedCard = 0;
		selectedPosition = -1;
		timer = 0;
		loadCardCount = 0;
		loadCardOffset = 3 + (float) container.getHeight() / Options.getCardLength();
		textAlpha = 0f;
	}

	/**
	 * Returns whether or not the game is over.
	 * @return true if over
	 */
	private boolean isGameOver() { return (playerHand.isEmpty() || opponentHand.isEmpty()); }

	/**
	 * Plays a card, unless the board position is occupied by another card.
	 * @param hand the hand of cards
	 * @param index the index in the hand [0, 4]
	 * @param position the position on the board [0, 8]
	 * @return true if a card was played, false if position already taken
	 */
	private boolean playCard(ArrayList<Card> hand, int index, int position) {
		if (board[position] != null)
			return false;

		// set card
		Card card = hand.get(index);
		card.playAtPosition(position, index);
		board[position] = card;
		AudioController.Effect.CARD.play();
		hand.remove(index);
		selectedCard = 0;
		selectedPosition = -1;

		// calculate the results
		result = new CardResult(card, position, board, elements);

		return true;
	}

	/**
	 * Processes a card result by changing card owners and adjusting score.
	 * @param resultList the list of affected cards
	 */
	private void cardResult(ArrayList<Card> resultList) {
		boolean owner = result.getSourceCard().getOwner();
		for (Card c : resultList) {
			if (c.getOwner() != owner) {
				c.changeOwner();
				if (c.getOwner() == PLAYER) {
					playerScore++;
					opponentScore--;
				} else {
					playerScore--;
					opponentScore++;
				}
			}
		}
		AudioController.Effect.TURN.play();
	}
}
