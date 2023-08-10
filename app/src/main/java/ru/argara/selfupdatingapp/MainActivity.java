package ru.argara.selfupdatingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

	SelfUpdate su = new SelfUpdate();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		Log.d("FAB " + "SelfUpdate", su.APP_DB_PATH);
	}
}