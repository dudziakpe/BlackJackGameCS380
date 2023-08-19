package com.cs380.blackjackgame.gamelogic;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cs380.blackjackgame.R;
import com.cs380.blackjackgame.deck.Card;
import com.cs380.blackjackgame.deck.Deck;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

public class Game {
	public static final int defaultDeckAmount = 2;

	@NonNull
	private Context context;
	@NonNull
	private RelativeLayout playerLayout;
	@NonNull
	private RelativeLayout dealerLayout;

	// Dealer player holds holds the dealer player AI. This is initialzed in the
	// constructor of Game.
	@NonNull
	public AIPlayer dealerPlayer;

	// Deck holds the current Deck object for this Game object and is initialized
	// and shuffled in the constructor of Game.
	@NonNull
	public Deck deck;

	// Player holds all of the players (not including the dealer but including all
	// players who have busted as well) currently in this Game.
	@NonNull
	public Set<AbstractPlayer> players = new LinkedHashSet<>();

	// Game.playersLeftQueue holds the players we still need to process. This can be
	// thought of as the order of players who still havn't gone yet. The player at
	// the first index of this queue is the next player after Game.currentPlayer.
	@Nullable
	private Queue<AbstractPlayer> playersLeftQueue;

	// Game.currentPlayer holds the current player which is going. Since we need to
	// wait for button presses for a HumanPlayer, we check which player called the
	// Game.doPlayerAction() method. We then check if that player was the current
	// player and process their input.
	@Nullable
	public AbstractPlayer currentPlayer;

	// isDone tracks whether the game has finished or not.
	private boolean isDone = false;

	public Game(@NonNull Context context, @NonNull RelativeLayout playerLayout, @NonNull RelativeLayout dealerLayout) {
		this(context, playerLayout, dealerLayout, defaultDeckAmount);
	}

	public Game(@NonNull Context context, @NonNull RelativeLayout playerLayout, @NonNull RelativeLayout dealerLayout,
				int decks) {
		// Initialze and shuffle the deck object with the amount of decks being equal to
		// the parameter passed to this constructor.
		this.deck = new Deck(decks);

		this.context = context;
		this.playerLayout = playerLayout;
		this.dealerLayout = dealerLayout;

		// Initialize the dealerPlayer field to a new AIPlayer with the Dealer
		// difficulty / AI.
		dealerPlayer = new AIPlayer("Dealer", this, AIDiff.DEALER);
	}

	// Game.doRound() is called once when the game is started. From the player's in
	// the Game.players set, we construct a queue which holds the players we
	// still need to process.
	public void doRound() {
		dealerPlayer.showCards(context, dealerLayout);

		// Add all players in the game to the Game.playersLeftQueue. Since we are
		// processing the dealer after the game has "finished" (after all player
		// player's have made their move), we do not need to add them to this queue.
		playersLeftQueue = new LinkedList<>(players);

		// Ensure that the Game.playersLeftQueue contains player's in it. If it does,
		// set the current player to the first player in the queue, prompt them for
		// their actions if they are a HumanPlayer, which will allow them to press
		// buttons corresponding to the actions that HumanPlayer can do.

		// If the player is a AIPlayer, all we need to do is call the
		// AIPlayer.takeTurn() method, passing which card the dealer is currently
		// showing so their AI can calculate a move to make.
		if (playersLeftQueue.isEmpty() == false) {
			nextPlayer();
		} else {
			// If we have gotten to this point, there were no players in the
			// Game.playersLeftQueue, which means there is no players left to process. Log
			// that this error occurred.
			Log.e(this.toString(), "Players left queue was empty on initialization. Players: ");

			for (AbstractPlayer abstractPlayer : players) {
				Log.e(this.toString(), abstractPlayer.name);
			}
		}
	}

	public void doPlayerAction(@NonNull AbstractPlayer abstractPlayer, @NonNull PlayerAction playerAction) {
		// Validate that we have a current player and that the AbstractPlayer which
		// called this method was the current player.
		if (currentPlayer != null && currentPlayer.equals(abstractPlayer)) {
			// Stood should be set to true if the player stood when they were allowed to
			// stand.
			boolean stood = false;

			if (isActionValid(abstractPlayer, playerAction)) {
				Log.i(this.toString(), "Current Player: " + currentPlayer + " Action: " + playerAction);

				// At this point, whatever playerAction was passed to this function should be
				// valid, so we can process it without further validity checks.

				// TODO: Switch to switch statement once all actions are implemented.
				if (playerAction == PlayerAction.HIT) {
					// Add a card to the players hand.
					currentPlayer.hand.add(deck.getCard());

					//show players cards in the correct location
					if (currentPlayer.isDealer()){currentPlayer.showCards(context, dealerLayout);}
					else {currentPlayer.showCards(context, playerLayout);}

					if (currentPlayer.isBust()) {
						currentPlayer.endPlay();
					}
				} else if (playerAction == PlayerAction.STAND) {
					// Set the stood boolean to true and do nothing else.
					stood = true;
				} else if (playerAction == PlayerAction.DOUBLE_DOWN
						&& isActionValid(abstractPlayer, playerAction) == true) {
					abstractPlayer.doubleDown();
					//as a note: double down doubles the player's bet and adds exactly one more card to the player's hand
					// Add a card to the players hand.
					Card temp = deck.getCard();
					temp.setFaceDown(true);
					currentPlayer.hand.add(temp);

					//show players cards in the correct location
					if (currentPlayer.isDealer()){currentPlayer.showCards(context, dealerLayout);}
					else {currentPlayer.showCards(context, playerLayout);}

					if (currentPlayer.isBust()) {
						currentPlayer.endPlay();
					} else {
						//after the player doubles down, they immediately hit then stand. So the player should automatically stand here.
						currentPlayer.doAction(PlayerAction.STAND);
					}
				} else if (playerAction == PlayerAction.SPLIT) {
					// TODO: Implement
				} else if (playerAction == PlayerAction.SURRENDER) {
					abstractPlayer.surrender();
				}
			} else {
				StringBuilder logStringBuilder = new StringBuilder();
				logStringBuilder.append("Player attempted to do invalid action: ");
				logStringBuilder.append(abstractPlayer.name);
				logStringBuilder.append(". Action: ");
				logStringBuilder.append(playerAction);
				logStringBuilder.append(". Hand: ");

				for (Card card : abstractPlayer.hand) {
					logStringBuilder.append(card.getNumName()).append(" ").append(card.getSuitName());
				}

				Log.e(this.toString(), logStringBuilder.toString());

				stood = true;
			}

			// If the player stood or they busted / surrendered...
			if (stood || !currentPlayer.isPlaying()) {
				// ...log whether they busted or not.
				if (currentPlayer.isBust()) {
					Log.i(this.toString(), "Player " + abstractPlayer.name + " busted.");
					currentPlayer.endPlay();
				} else if (currentPlayer.getSurrender()) {
					Log.i(this.toString(), "Player " + abstractPlayer.name + " surrendered.");
					currentPlayer.endPlay();
				}

				// If the current player is not the dealer, we can go to the next player (if
				// there is any).
				if (!currentPlayer.isDealer()) {
					nextPlayer();
				}
			}

			promptHumanPlayerActions(currentPlayer);
		} else {
			// Player tried to call an action when they wern't supposed to. Log the
			// incident. This might have occured due to the buttons for HumanPlayer actions
			// still being on screen, etc.
			if (currentPlayer == null) {
				Log.e(this.toString(), "Player attempted to do action when current player wasn't set. Player: "
						+ abstractPlayer.name + ". Action: " + playerAction);
			} else {
				Log.e(this.toString(), "Player attempted to do action out of turn: " + abstractPlayer.name
						+ ". Action: " + playerAction);
			}
		}
	}

	public void nextPlayer() {
		// If their are players still left in the queue, set the current player to that
		// player and begin prompting them for actions.
		if (playersLeftQueue != null && !playersLeftQueue.isEmpty()) {
			setCurrentPlayer(playersLeftQueue.poll());
			currentPlayer.startPlay();

			if (!currentPlayer.isDealer()){currentPlayer.showCards(context, playerLayout);}

			if (currentPlayer instanceof HumanPlayer) {
				promptHumanPlayerActions(currentPlayer);
			} else {
				// the dealer / AIPlayer will return false if they haven't finished playing, returning true if it has.
				while(!((AIPlayer) currentPlayer).takeTurn(getDealerShowingCard())){}
			}
		} else {
			// If there are no more players to process, than all but the dealer has played
			// for this round. End the game.
			endGame();
		}
	}

	public void endGame() {
		// Add the dealer as a player still left to play, then call nextPlayer() which will make
		// them play until they are done.
		playersLeftQueue.add(dealerPlayer);
		nextPlayer();

		dealerPlayer.reveal();
		dealerPlayer.showCards(context, dealerLayout);

		boolean dealerBusted = dealerPlayer.isBust();
		int dealerHandValue = dealerPlayer.getHandValue();
		boolean dealerHasBlackjack = dealerPlayer.hasBlackjack();

		clearCurrentPlayerText();

		// Blank the log to make score total more readable.
		for (int i = 0; i < 10; i++) {
			Log.i(this.toString(), "");
		}

		// TODO: Log blackjacks, even though it is apparent due to on-screen cards.
		Log.i(this.toString(), "Dealer Hand Value: " + (dealerBusted ? "Busted " : "") + dealerHandValue);
		Log.i(this.toString(), "");
		Log.i(this.toString(), "Player Hand Values: ");

		// For every player, check if they are still in the game. If they are, check if
		// their hand was better than the dealer and output.
		for (AbstractPlayer abstractPlayer : players) {
			int handValue = abstractPlayer.getHandValue();

			if (abstractPlayer.isPlaying()) {
				boolean tied = handValue == dealerHandValue;
				boolean playerWon = false;
				boolean playerHasBlackjack = abstractPlayer.hasBlackjack();

				if (dealerBusted) {
					playerWon = true;
				} else if (handValue > dealerHandValue) {
					playerWon = true;
				} else if (tied) {
					// Taken from: https://en.wikipedia.org/wiki/Blackjack#:~:text=A%20player%20total,value%20of%2021.
					boolean blackjackTrueTie = dealerHasBlackjack && playerHasBlackjack;
					boolean noBlackjacks = !dealerHasBlackjack && !playerHasBlackjack;

					if(blackjackTrueTie) {
						// Both the dealer and the player had a Blackjack, meaning they tied.
						// TODO: Return the player's bet
						Log.i(this.toString(),
								"Player " + abstractPlayer.name + " tied the dealer with a hand value of " + handValue + ", both with Blackjacks");
					} else if(noBlackjacks) {
						// Neither the dealer nor the player had a Blackjack, meaning the tie was just a tie.
						// TODO: Return the player's bet
						Log.i(this.toString(),
								"Player " + abstractPlayer.name + " tied the dealer with a hand value of " + handValue);
					} else {
						// Either the dealer or the player had a Blackjack, meaning that whoever did wins the tie.
						if(dealerHasBlackjack) {
							Log.i(this.toString(),
									"Player " + abstractPlayer.name + " lost to the dealer with a hand value of " + handValue + " without a Blackjack");
						} else {
							Log.i(this.toString(),
									"Player " + abstractPlayer.name + " beat the dealer with a hand value of " + handValue + " with a Blackjack");
						}
					}
				} else {
					Log.i(this.toString(), "Player " + abstractPlayer.name
							+ " lost against the dealer with a hand value of " + handValue);
				}

				if (playerWon) {
					Log.i(this.toString(),
							"Player " + abstractPlayer.name + " beat the dealer with a hand value of " + handValue);
				}
			} else {
				if(abstractPlayer.isBust()){
					Log.i(this.toString(), abstractPlayer.name + " busted with a hand value of " + handValue);
				}
				else if(abstractPlayer.getSurrender()){
					Log.i(this.toString(), abstractPlayer.name + " surrendered with a hand value of " + handValue);
				}
			}
		}

		isDone = true;
	}

	// This should check if the action the player is making is valid.
	public boolean isActionValid(AbstractPlayer abstractPlayer, PlayerAction playerAction) {
		if (abstractPlayer.isBust()) {
			return false;
		} else if (!abstractPlayer.isPlaying()) {
			return false;
		} else if (abstractPlayer.getHandValue() == 21 && playerAction != PlayerAction.STAND) {
			return false;
		} else if (abstractPlayer.hasDoubled() && playerAction == PlayerAction.DOUBLE_DOWN) {
			return false;
		} else if (abstractPlayer.getSurrender() && playerAction == PlayerAction.SURRENDER) {
			return false;
		} else if (abstractPlayer.hand.size() > 2){
			switch(playerAction){
				case SURRENDER:		//surrender/double/split down are only allowed on a hand of 2 cards
				case DOUBLE_DOWN:	//player.hand.size > 2 works for checking surrender/double/split down validity since the initial hand will always be 2
				case SPLIT:			//also works for split in the future since you CAN double down/surrender on each new hand after a split
					return false;
				default:
					break;
			}
		}	else if (abstractPlayer.hand.get(0).getNum() != abstractPlayer.hand.get(1).getNum() && playerAction == PlayerAction.SPLIT) {
			return false; //note, it should only reach this if the player is splitting and their hand size is 2 because of the previous if.
		}

		return true; //if nothing wrong is found, returns as valid
	}

	// This method adds the player passed to the function to the list of players in
	// this Game object.
	public boolean addPlayer(@NonNull AbstractPlayer player) {
		return players.add(player);
	}

	// This method will deal two cards to each player, including the dealer.
	public void dealInitialHand() {
		for (AbstractPlayer abstractPlayer : players) {
			abstractPlayer.hand.add(deck.getCard());
			abstractPlayer.hand.add(deck.getCard());
		}

		Card temp = deck.getCard();
		temp.setFaceDown(true);
		dealerPlayer.hand.add(temp);
		dealerPlayer.hand.add(deck.getCard());

	}

	// This will return which card the dealer is "showing". This will be their
	// second card, or going from index 0 as far left and final index as far right,
	// their left-most card.
	public int getDealerShowingCard() {
		return dealerPlayer.hand.get(1).getNum();
	}

	// This checks if a abstract player is the dealer player.
	public boolean isDealer(@Nullable AbstractPlayer player) {
		return dealerPlayer.equals(player);
	}

	public boolean isDone() {
		return isDone;
	}

	//set the bet for the player
	//TODO: Need to add the functionality for prompting the player if they entered an invalid bet
	public void setBet (@NonNull AbstractPlayer abstractPlayer, double bet) {
		if(abstractPlayer.getWinnings() - bet < 0) {
			//prompt user to re-enter a valid bet
		} else {
			abstractPlayer.setBet(bet);
		}
	}

	//add the winnings of the player
	public void addBet(@NonNull AbstractPlayer abstractPlayer) {
		abstractPlayer.addWinnings();
	}

	//subtract the bet from the current amount of money that the player has
	public void subBet (@NonNull AbstractPlayer abstractPlayer) {
		abstractPlayer.subLosses();
	}

	public void updateCurrentPlayerText() {
		if (currentPlayer != null) {
			TextView userTurnTextView = Objects.requireNonNull(getActivity()).findViewById(R.id.userTurntxt);
			userTurnTextView.setText(currentPlayer.name + "'s Turn");
		} else {
			clearCurrentPlayerText();
		}
	}

	public void clearCurrentPlayerText() {
		TextView userTurnTextView = Objects.requireNonNull(getActivity()).findViewById(R.id.userTurntxt);
		userTurnTextView.setText("");
	}

	@Nullable
	public Activity getActivity() {
		if (context instanceof Activity) {
			return (Activity) context;
		}

		return null;
	}

	public void setCurrentPlayer(@Nullable AbstractPlayer newCurrentPlayer) {
		this.currentPlayer = newCurrentPlayer;
		updateCurrentPlayerText();
	}

	public void promptHumanPlayerActions(@NonNull AbstractPlayer abstractPlayer) {
		if(abstractPlayer instanceof HumanPlayer) {
			Log.i(this.toString(), "Player " + abstractPlayer.name + " has " + abstractPlayer.getHandValue());

			Activity activity = getActivity();

			if(abstractPlayer.isPlaying()) {
				boolean initialHand = abstractPlayer.hand.size() == 2;

				activity.findViewById(R.id.Hit).setVisibility(View.VISIBLE);
				activity.findViewById(R.id.Stand).setVisibility(View.VISIBLE);

				if(initialHand) {
					activity.findViewById(R.id.Surrender).setVisibility(View.VISIBLE);
				} else {
					activity.findViewById(R.id.Surrender).setVisibility(View.INVISIBLE);
				}

				// If the player hasn't doubled, then they can still double. Show them the button.
				if(!abstractPlayer.hasDoubled()) {
					activity.findViewById(R.id.DoubleDwn).setVisibility(View.VISIBLE);
				}

				// If it is the player's initial hand, check if they can split. If not, then hide
				// the split button.
				if(initialHand) {
					// ...and their two cards have an equal number, then they can split, so show the
					// button. If not, hide the split button.
					if(abstractPlayer.hand.get(0).getNum() == abstractPlayer.hand.get(1).getNum()) {
						activity.findViewById(R.id.Split).setVisibility(View.VISIBLE);
					} else {
						activity.findViewById(R.id.Split).setVisibility(View.INVISIBLE);
					}
				} else {
					activity.findViewById(R.id.Split).setVisibility(View.INVISIBLE);
				}
			} else {
				clearVisibleButtons();
			}
		} else {
			clearVisibleButtons();
		}
	}

	public void clearVisibleButtons() {
		Activity activity = Objects.requireNonNull(getActivity());

		activity.findViewById(R.id.Hit).setVisibility(View.INVISIBLE);
		activity.findViewById(R.id.Stand).setVisibility(View.INVISIBLE);
		activity.findViewById(R.id.DoubleDwn).setVisibility(View.INVISIBLE);
		activity.findViewById(R.id.Surrender).setVisibility(View.INVISIBLE);
		activity.findViewById(R.id.Split).setVisibility(View.INVISIBLE);
	}

	public void initialize() {

		//clears all the players hands
		for(AbstractPlayer player : players){
			player.emptyHand();
			player.startPlay();
		}

		dealerPlayer.emptyHand();
		dealerPlayer.startPlay();

		//resets the variable for the game being done
		isDone = false;
		// Deals two cards to each player
		dealInitialHand();
		// Start Round
		doRound();
	}
}
