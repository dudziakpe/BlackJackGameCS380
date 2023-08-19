package com.cs380.blackjackgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
	public Button Play_BJ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Play_BJ = findViewById(R.id.Play_BJ);
		Play_BJ.setOnClickListener((View v) -> openPlay_Blackjack());
	}

	public void openPlay_Blackjack() {
		Intent intent = new Intent(this, Play_Blackjack.class);
		startActivity(intent);
	}
}