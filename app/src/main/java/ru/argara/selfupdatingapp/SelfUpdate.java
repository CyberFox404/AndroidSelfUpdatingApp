package ru.argara.selfupdatingapp;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.snackbar.Snackbar;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.httpclient.android.BuildConfig;

public class SelfUpdate extends AppCompatActivity {


	private static final int PERMISSION_REQUEST_CODE = 101;
	//String SIO_URL_APP_UPD = "http://192.168.0.20/checkUPD/";
	String SIO_URL_APP_UPD = "https://gorg404.ru/android_upd/";

	String _PACKAGE_NAME;
	//String _DIR_SAVE;
	String APP_DB_PATH_SHORT;
	String APP_DB_PATH;


	AsyncHttp asyncHttp;


	String _VERSION_NAME;
	int _VERSION_CODE;

	ProgressBar progressBarCircle;
	ProgressBar progressBarLine;
	TextView tv_selfupdate_title;
	TextView tv_selfupdate_text;

	int checkInet;
	String TAG = "FAB TAG";

	SharedPreferences prefs;
	SharedPreferences.Editor prefEdit;

	int sf_vercode = 0;
	int sf_day = 0;
	boolean sf_upd = false;

	String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

	private PermissionHelper permissionHelper;

	String md5;

	//int PERMISSION_REQUEST_CODE = 101;

	//MyRequestPermissions myRequestPermissions;

	private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
	private static final String WRITE_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
	private static final String READ_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selfupdateapp);
		prefs = getSharedPreferences("MyPrefs",
				Context.MODE_PRIVATE);
		prefEdit = prefs.edit();


		//permissionOn();

if(!hasAllFilesAccess()) {
	Log.d("FAB hasAllFilesAccess" , "off");
	ff();
} else {
	Log.d("FAB hasAllFilesAccess" , "on");
	permissionOn();
}





	}

	boolean checkPermission(String[] permissions){
		//int res = 0;
		//boolean res = true;
		boolean allowed = true;

		//for (String perms : permissions){
		//	res = checkPermission(perms);
		//	if(res && (res == PackageManager.PERMISSION_GRANTED)){
		//		allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
		//	}
		//	if (!(res == PackageManager.PERMISSION_GRANTED)){
		//		return false;
		//	}
		//}
		//return true;

		for (String perms : permissions){
			allowed = allowed && checkPermission(perms);
		}
		return allowed;

	}

	public boolean checkPermission(String permission) {
		//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
		int result = ContextCompat.checkSelfPermission(this, permission);
		return (result == PackageManager.PERMISSION_GRANTED);
		//}
		//return false;
	}

	private boolean hasAllFilesAccess() {

		AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);

		try {
			ApplicationInfo app  = getPackageManager().getApplicationInfo(getPackageName(), 0);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				return appOpsManager.unsafeCheckOpNoThrow("android:manage_external_storage",app.uid, getPackageName()) == AppOpsManager.MODE_ALLOWED;
			}

		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		return false;
	}


	void ff(){
		Log.d("FAB ff" , "main");
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
	Log.d("FAB ff" , "Build.VERSION.SDK_INT >= Build.VERSION_CODES.R");
		if(!Environment.isExternalStorageManager()){
			Snackbar.make(findViewById(android.R.id.content), "Необходимо разрешение!", Snackbar.LENGTH_INDEFINITE)
					.setAction("Настройки", v -> {
						Intent intent;
						try {
							//Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
							Uri uri = Uri.parse("package:ru.argara.selfupdatingapp");
							intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
							//startActivity(intent);
						} catch (Exception ex){
							intent = new Intent();
							intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
							//startActivity(intent);
						}
						mStartForResult.launch(intent);
					})
					.show();
		}
	}
	}


	ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
			result -> {
				Log.d("FAB mStartForResult" , "1");
				//if(result.getResultCode() == Activity.RESULT_OK){
				//	Log.d("FAB mStartForResult" , "permissionOn");
				//	permissionOn();
				//} else {
				//	Log.d("FAB mStartForResult" , "permissionOff");
				//	permissionOff();
				//}

				if(!hasAllFilesAccess()) {
					Log.d("FAB mStartForResult" , "off");
					//ff();
				} else {
					Log.d("FAB mStartForResult" , "on");
					permissionOn();
				}
			});

	public void permissionOn(){
		Log.d("FAB " , "permissionOn");
		//while (!requestMultiplePermissions()){

		//}

		getData();

		PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			_VERSION_NAME = pInfo.versionName;
			_VERSION_CODE = pInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}





		if(sf_day == getDay()) {
			Log.d("FAB " + "sf_day == getDay", "1");
			//startIntent();
			//return;
		}

		if(sf_upd){
			Log.d("FAB " + "sf_upd", String.valueOf(sf_upd));
			putData();
			//startIntent();
		}



		//if(sf_vercode == _VERSION_CODE) {
		//	putData();
		//	startIntent();
		//}






		init();


		Log.d("FAB " + "SelfUpdate", APP_DB_PATH);

		checkInet = asyncHttp.checkInet();

		asyncHttp.setListener(new AsyncHttp.Listener() {
			@Override
			public void ponSuccess(int statusCode, Header[] headers, byte[] response, int ID) {
				Log.d("FAB", "success");
				Log.d("FAB", String.valueOf(ID));
				Log.d("FAB", String.valueOf(checkInet));
				//final int _checkInet = checkInet;
				if (ID == checkInet) {
					tv_selfupdate_text.setText("Проверка обновления");




					checkUpdApp();

				}


			}

			@Override
			public void ponFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e, int ID) {
				//inetcheck.setText("onFailure " + statusCode);
				if (ID == checkInet) {
					//inetcheck.setText("onFailure " + statusCode);
					Log.d("FAB " + "ponFailure", "ponFailure");
					startIntent();

				}
			}
		});


	}

	public void permissionOff(){
		Log.d("FAB " , "permissionOff");
	}


	public void init(){
		_PACKAGE_NAME = getApplicationContext().getPackageName();
		APP_DB_PATH_SHORT = Environment.DIRECTORY_DOWNLOADS + "/"+_PACKAGE_NAME+"/downloads/app";
		//APP_DB_PATH_SHORT = Environment.DIRECTORY_DOWNLOADS + "/FFFF/downloads/app/";
		APP_DB_PATH = Environment.getExternalStoragePublicDirectory(APP_DB_PATH_SHORT).toString();
		//APP_DB_PATH = Environment.getExternalStorageDirectory()  + "/"+_PACKAGE_NAME+"/downloads/app/";

		File dbDir = new File(APP_DB_PATH);
		dbDir.mkdirs(); // creates needed dirs

		asyncHttp = new AsyncHttp();

		progressBarCircle = findViewById(R.id.progressBarCircle);
		progressBarLine = findViewById(R.id.progressBarLine);
		tv_selfupdate_title = findViewById(R.id.tv_selfupdate_title);
		tv_selfupdate_text = findViewById(R.id.tv_selfupdate_text);
	}

	public void startIntent(){
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
				Intent.FLAG_ACTIVITY_CLEAR_TASK |
				Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	public int getDay(){
		String currentDate = new SimpleDateFormat("dd", Locale.getDefault()).format(new Date());
		return Integer.parseInt(currentDate);
	}

	public void putData() {
		prefEdit.putInt("vercode", _VERSION_CODE);
		prefEdit.putInt("day", getDay());
		// Сохраните изменения.
		prefEdit.apply();
	}


	public void getData(){
		sf_vercode = prefs.getInt("vercode", 0);
		sf_day = prefs.getInt("day", 0);
	}



	public void checkUpdApp() {


		RequestParams params = new RequestParams();
		params.put("p", _PACKAGE_NAME); // PACKAGE_NAME
		params.put("vc", _VERSION_CODE); // VERSION_CODE
		//params.put("username", "username");
		//params.put("password", "password");


		asyncHttp.get(SIO_URL_APP_UPD, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// If the response is JSONObject instead of expected JSONArray

				//putData();


				Log.d(TAG, "onSuccess JSONObject");
				Log.d(TAG, String.valueOf(response));
				try {
					// Log.d("rrrr", String.valueOf(response.getString("error")));
					//Log.d("rrrr", String.valueOf(response.getBoolean("check")));

					if(response.getBoolean("check")) {

						tv_selfupdate_text.setText("Доступно обновление");
						String url = response.getString("url");
						md5 = response.getString("md5");
						//int responseVersion = response.getInt("version");
						//ffff(url);

						download(url, APP_DB_PATH);
					} else {
						Log.d("FAB " + "ponFresponse.getBoolean(\"check\")", "false");
						startIntent();
					}



				} catch (JSONException e) {
					//throw new RuntimeException(e);
					Log.d(TAG, "Error " + String.valueOf(e));
					Log.d("FAB " + "JSONException", "JSONException");
					startIntent();
				}

				//inetcheck.setText("Success " + statusCode);
			}



			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject e) {
				// Pull out the first event on the public timeline
				//JSONObject firstEvent = timeline.get(0);
				//String tweetText = firstEvent.getString("text");

				// Do something with the response
				//System.out.println(tweetText);
				Log.d(TAG, "onFailure");
				Log.e("ERROR", e.toString());
				Log.d(TAG, String.valueOf(e));

				startIntent();
				//return false;
				//inetcheck.setText("onFailure " + statusCode);
			}
		});
	}

	//public SelfUpdate() {
	//	init(true);
	//}



	//public SelfUpdate(String dirSave) {
	//	this.APP_DB_PATH_SHORT = dirSave;
	//	init(false);
	//}

	public static String fileToMD5(String filePath) {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(filePath);
			byte[] buffer = new byte[1024];
			MessageDigest digest = MessageDigest.getInstance("MD5");
			int numRead = 0;
			while (numRead != -1) {
				numRead = inputStream.read(buffer);
				if (numRead > 0)
					digest.update(buffer, 0, numRead);
			}
			byte [] md5Bytes = digest.digest();
			return convertHashToString(md5Bytes);
		} catch (Exception e) {
			return null;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) { }
			}
		}
	}

	private static String convertHashToString(byte[] md5Bytes) {
		String returnVal = "";
		for (int i = 0; i < md5Bytes.length; i++) {
			returnVal += Integer.toString(( md5Bytes[i] & 0xff ) + 0x100, 16).substring(1);
		}
		return returnVal.toUpperCase();
	}



	private void download(String _urlfile, String _location) {

		Thread downloadThread = new Thread() {

			long percents = 0;
			String msg = "normal";

			final byte[] buffer = new byte[512];
			long totalDownloaded = 0;
			int count;

			long fileLength = 0;

			final String sUrl = _urlfile;
			final String destinationDir = _location;
			//File rootDir = Environment.getExternalStorageDirectory();
			//final String fileName = sUrl.substring(sUrl.lastIndexOf('/') + 1);
			final String fileName = "upd.apk";
			//File saveTo = null;

			String filePath = destinationDir + File.separator + fileName;
			//String filePath = destinationDir + fileName;

			//String md5 = "";






			@Override
			public void run() {

				File saveTo = new File(filePath);
				File saveToDir = new File(destinationDir);




				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Log.v("downloads destinationDir", destinationDir);
						Log.v("downloads fileName", fileName);
						// progressBar.setIndeterminate(false);
						// ProgressTitle.setText("Загрузка БД");
						//progressLabel.setText("");
						//pd = new ProgressDialog(MainActivity.this);
						//pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						//pd.setTitle("Обновление БД");
						//pd.setMessage("Загрузка ...");
						///pd.show();
						tv_selfupdate_title.setText("Загрузка обновления");

						//if (saveToDir.isDirectory()){
							//saveToDir.delete();
						//	try {
						//		FileUtils.deleteDirectory(saveToDir);
						//	} catch (IOException e) {
						//		throw new RuntimeException(e);
						///	}
						//}


						saveToDir.delete();

						if(saveToDir.exists()){
							try {
								saveToDir.getCanonicalFile().delete();
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
							if(saveToDir.exists()){
								getApplicationContext().deleteFile(saveToDir.getName());
							}
						}

					}
				});


				//func.deleteDir(destinationDir);
				//new File(destinationDir).mkdirs();


				//if(saveTo.exists()) {
				//	File file2 = new File(saveTo.getAbsolutePath());
				//	boolean deleted = file2.delete();
				//	Log.d( "FAB deleted", String.valueOf(deleted));
					//Toast.makeText(this, "File deleted.", Toast.LENGTH_SHORT).show();
					//finish();
				//}







				if(saveTo.exists()) Log.d( "FAB deleted", "exists");
					//boolean deleted = saveTo.delete();
				boolean gf = saveToDir.mkdirs();



				Log.v("downloads mkdirs", String.valueOf(gf));

				//ProgressTitle.setText("Загрузка БД");

				//ProgressTitle.setText("Подготовка БД");
				//progressLabel.setText("");


				//String fileName = _urlfile.substring(_urlfile.lastIndexOf('/') + 1);
				//String destinationDir = Environment.getExternalStorageDirectory() + "/dsimpleblogru/downloads";

				Log.d(TAG + " sUrl", sUrl);

				//if (saveTo.exists()) saveTo.delete();
				try {
					URL url = new URL(sUrl);
					URLConnection conn = url.openConnection();
					conn.connect();
					fileLength = conn.getContentLength();
					InputStream is = new BufferedInputStream(url.openStream());
					OutputStream os = new FileOutputStream(saveTo);

					///long percents = 0;

					while ((count = is.read(buffer)) != -1) {
						totalDownloaded += count;
						percents = (totalDownloaded * 100 / fileLength);
						//publishProgress(percents, totalDownloaded, fileLength);


						//publishProgress((int) (totalDownloaded * 100 / fileLength));
						os.write(buffer, 0, count);
						//if (isCancelled())
						//   break;



						new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
							@Override
							public void run() {
								progressBarLine.setProgress((int) percents);
								tv_selfupdate_text.setText(String.valueOf(percents) + "%");
							}
						}, 100);
/*
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Log.v("Decompress All", "Start");
                                //DescriptionProgressLabel(percents, fileLength, totalDownloaded, 1);

                                try {
                                    //progressBar.setProgress((int) percents);
                                    //textView.setText(String.valueOf(percents));
                                } catch (Exception e) {
                                    Log.d(TAG + " 00000", String.valueOf(e));
                                }
                                //Log.d(TAG, String.valueOf(e));

                                //pd.setMessage(totalDownloaded +" / "+ fileLength);
                            }
                        });


 */
					}
					os.flush();
					os.close();
					is.close();

					Log.d(TAG, "FINISSH DOWNLOADS");
					// return true;
				} catch (MalformedURLException e) {
					msg = "Invalid URL";
				} catch (IOException e) {
					msg = "No internet connection " + e;
				}
				//return false;


				//Log.d(TAG, msg);
				Log.d("FAB msg", msg);
				Log.d(TAG, String.valueOf(saveTo));


				//totalDownloaded += count;
				//percents = (totalDownloaded * 100 / fileLength);
				Log.d(TAG + " totalDownloaded", String.valueOf(totalDownloaded));
				Log.d(TAG + " fileLength", String.valueOf(fileLength));

				//totalDownloaded = fileLength;

				if(totalDownloaded == fileLength) {
					String ms5DwnFile = fileToMD5(filePath);
					Log.d("FAB md5", md5);
					Log.d("FAB ms5DwnFile", ms5DwnFile);
					if(Objects.equals(md5, fileToMD5(filePath))) {
						Log.d("FAB fileToMD5", "ok");
					} else {
						Log.d("FAB fileToMD5", "err");
					}

					putData();
					new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
						@Override
						public void run() {
							installApp(saveTo.toString());
						}
					}, 0);


				} else {
					//Toast.makeText(getApplicationContext(), "Не верный размер файла. Обновите вручную", Toast.LENGTH_LONG).show();
					Log.d(TAG, String.valueOf("Не верный размер файла. Обновите вручную"));
					new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
						@Override
						public void run() {
							startIntent();
						}
					}, 1000);
				}


			}
		};
		//downloadTask.execute(file_url, destinationDir);
		downloadThread.start();

	}


	public void installApp(String fullPathApp) {

		sf_upd = true;
		//Intent intent = new Intent(Intent.ACTION_VIEW);
		//intent.setDataAndType(Uri.fromFile(new File(fullPathApp)), "application/vnd.android.package-archive");
		//intent.setDataAndType(Uri.parse(fullPathApp), "application/vnd.android.package-archive");
		//intent.setDataAndType(Uri.parse(fullPathApp), "application/android.com.app");
		//intent.setData(Uri.parse(fullPathApp));
		//intent.setType("application/android.com.app");
		//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//startActivity(intent);
		//StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		//StrictMode.setVmPolicy(builder.build());
		File newFile = new File(fullPathApp);

		MimeTypeMap mime = MimeTypeMap.getSingleton();
		String ext = newFile.getName().substring(newFile.getName().lastIndexOf(".") + 1);
		String type = mime.getMimeTypeFromExtension(ext);
		try {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			Uri contentUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileProvider", newFile);
			intent.setDataAndType(contentUri, type);


			//Uri photoURI = Uri.fromFile(createImageFile());
			//to

			//Uri photoURI = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", new File(fullPathApp));
			// Edit: If you're using an intent to make the system open your file, you may need to add the following line of code:



			//final Uri uri = Uri.parse("file://" + fullPathApp);
			//Intent install = new Intent(Intent.ACTION_VIEW);
			//install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			// install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//install.setDataAndType(photoURI,
			//        "application/android.com.app");
			startActivity(intent);

		} catch (ActivityNotFoundException anfe) {
			Toast.makeText(this, "No activity found to open this attachment.", Toast.LENGTH_LONG).show();
			startIntent();
		}
	}

}
