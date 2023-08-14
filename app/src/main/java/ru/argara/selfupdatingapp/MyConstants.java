package ru.argara.selfupdatingapp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class MyConstants {

	//public static final String PACKAGE_NAME = ;
	String PACKAGE_NAME;
	Context ctx;

	//String URL_SERVER_UPD = "https://gorg404.ru/android_upd/";
	String URL_SERVER_UPD = "https://update.gorg404.ru/";
	String URL_SERVER_UPD_KEY = "https://update.gorg404.ru/";

	//PACKAGE_NAME;
	String VERSION_NAME;
	int VERSION_CODE;

	String APP_DATA_PATH;
	String APP_DOWNLOAD_PATH_SHORT;
	public String APP_DOWNLOAD_PATH;
	String APP_DOWNLOAD_FILE_NAME;
	String APP_DOWNLOAD_FILE_PATH;

	public MyConstants(Context ctx) {
		this.ctx = ctx;
	}


	void init(){
		PACKAGE_NAME = ctx.getPackageName();

		Log.d("FAB init" , PACKAGE_NAME);

		PackageInfo pInfo = null;
		try {
			pInfo = ctx.getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
			VERSION_NAME = pInfo.versionName;
			VERSION_CODE = pInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}

		APP_DATA_PATH = Environment.DIRECTORY_DOWNLOADS + "/" + PACKAGE_NAME;
		APP_DOWNLOAD_PATH_SHORT = APP_DATA_PATH + "/downloads/app";
		APP_DOWNLOAD_PATH = Environment.getExternalStoragePublicDirectory(APP_DOWNLOAD_PATH_SHORT).toString();

		APP_DOWNLOAD_FILE_NAME = "upd.apk";
		APP_DOWNLOAD_FILE_PATH = APP_DOWNLOAD_PATH + File.separator + APP_DOWNLOAD_FILE_NAME;


	}




}
