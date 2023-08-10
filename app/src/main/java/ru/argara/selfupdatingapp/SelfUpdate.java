package ru.argara.selfupdatingapp;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class SelfUpdate extends AppCompatActivity {

	String _PACKAGE_NAME;
	//String _DIR_SAVE;
	String APP_DB_PATH_SHORT;
	String APP_DB_PATH;


	AsyncHttp asyncHttp;


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selfupdateapp);

		init();


		Log.d("FAB " + "SelfUpdate", APP_DB_PATH);


	}

	//public SelfUpdate() {
	//	init(true);
	//}



	//public SelfUpdate(String dirSave) {
	//	this.APP_DB_PATH_SHORT = dirSave;
	//	init(false);
	//}

	public void init(){
		_PACKAGE_NAME = getApplicationContext().getPackageName();
		APP_DB_PATH_SHORT = Environment.DIRECTORY_DOWNLOADS + "/"+_PACKAGE_NAME+"/downloads/app/";
		APP_DB_PATH = Environment.getExternalStoragePublicDirectory(APP_DB_PATH_SHORT).toString();
		asyncHttp = new AsyncHttp();
	}

}
