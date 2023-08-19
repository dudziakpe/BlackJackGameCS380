package com.cs380.blackjackgame;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.cs380.blackjackgame.gamelogic.Game;
import com.cs380.blackjackgame.gamelogic.HumanPlayer;
import com.cs380.blackjackgame.gamelogic.PlayerAction;

public class Play_Blackjack extends AppCompatActivity {
	public Button quitGame;
	public Button Hit;
	public Button Stand;
	public Button Doubledwn;
	public Button Split;
	public Game game;
	public Button Replay;

	// All HumanPlayer's and AIPLayer's (other than the dealer, which is handled
	// automatically) should be added to the Game.players set.
	// public AbstractPlayer player;
	// public HumanPlayer Human;

	// The dealer is automatically created by constructing Game (new Game()) and is
	// located at Game.dealer
//	public AIPlayer Dealer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_blackjack);

		game = new Game(this, findViewById(R.id.userscr), findViewById(R.id.dealerscr));

		game.addPlayer(new HumanPlayer("Player 1", game));
		game.addPlayer(new HumanPlayer("Player 2", game));

		//initializes the game state with fresh player hands and a new round
		game.initialize();

		// We need to assign variables to the Hit, Stand, and Doubledwn fields of
		// Play_Blackjack, else we will NullPointerException.
		Hit = findViewById(R.id.Hit);
		Stand = findViewById(R.id.Stand);
		Doubledwn = findViewById(R.id.DoubleDwn);

		Hit.setVisibility(View.VISIBLE);
		Stand.setVisibility(View.VISIBLE);
		Doubledwn.setVisibility(View.VISIBLE);

		//runs the runnable on the handler that causes the app to check if the game is done every 1/100 second
		//if it's done, display the restart/quit buttons
		handler.postDelayed(endGameCheck, 10);

		((Button) findViewById(R.id.Hit)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				game.currentPlayer.doAction(PlayerAction.HIT);
			}
		});

		((Button) findViewById(R.id.Stand)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				game.currentPlayer.doAction(PlayerAction.STAND);
			}
		});

		// split button
		Split = findViewById(R.id.Split);

		// quit game button
		quitGame = findViewById(R.id.quit);
		quitGame.setOnClickListener((View v) -> Return_to_Menu());

		Replay = findViewById(R.id.Replay);
		Replay.setOnClickListener((View v) -> replay());
	}

	private Handler handler = new Handler();
	private Runnable endGameCheck = new Runnable(){
		@Override
		public void run(){
			if (game.isDone()) {
				quitGame.setVisibility(View.VISIBLE);
				Replay.setVisibility(View.VISIBLE);
				Hit.setVisibility(View.INVISIBLE);
				Stand.setVisibility(View.INVISIBLE);
			}
			handler.postDelayed(this, 10);
		}
	};



	public void Return_to_Menu() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	public void replay(){
		//re-initializes the game state
		game.initialize();
		//removes the end game buttons from the screen
		quitGame.setVisibility(View.INVISIBLE);
		Replay.setVisibility(View.INVISIBLE);
		Hit.setVisibility(View.VISIBLE);
		Stand.setVisibility(View.VISIBLE);
	}
}