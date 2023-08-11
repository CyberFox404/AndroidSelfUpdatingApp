package ru.argara.selfupdatingapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import cz.msebera.android.httpclient.Header;

public class SelfUpdate extends AppCompatActivity {


	//String SIO_URL_APP_UPD = "http://192.168.0.20/checkUPD/";
	String SIO_URL_APP_UPD = "https://gorg404.ru/android_upd/";

	String _PACKAGE_NAME;
	//String _DIR_SAVE;
	String APP_DB_PATH_SHORT;
	String APP_DB_PATH;


	AsyncHttp asyncHttp;


	String version;
	int verCode;

	ProgressBar progressBarCircle;
	ProgressBar progressBarLine;
	TextView tv_selfupdate_title;
	TextView tv_selfupdate_text;

	int checkInet;
	String TAG = "FAB TAG";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selfupdateapp);

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


					try {
						PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
						version = pInfo.versionName;
						verCode = pInfo.versionCode;

						checkUpdApp();
					} catch (PackageManager.NameNotFoundException e) {
						e.printStackTrace();
					}
				}


			}

			@Override
			public void ponFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e, int ID) {
				//inetcheck.setText("onFailure " + statusCode);
				if (ID == checkInet) {
					//inetcheck.setText("onFailure " + statusCode);

					startIntent();

				}
			}
		});




	}




	public void checkUpdApp() {


		RequestParams params = new RequestParams();
		params.put("p", _PACKAGE_NAME); // VERSION_CODE
		params.put("vc", verCode); // VERSION_CODE
		//params.put("username", "username");
		//params.put("password", "password");


		asyncHttp.get(SIO_URL_APP_UPD, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// If the response is JSONObject instead of expected JSONArray


				Log.d(TAG, "onSuccess JSONObject");
				Log.d(TAG, String.valueOf(response));
				try {
					// Log.d("rrrr", String.valueOf(response.getString("error")));
					//Log.d("rrrr", String.valueOf(response.getBoolean("check")));

					if(response.getBoolean("check")) {

						tv_selfupdate_text.setText("Доступно обновление");
						String url = response.getString("url");
						//ffff(url);

						download(url, APP_DB_PATH);
					} else {
						startIntent();
					}



				} catch (JSONException e) {
					//throw new RuntimeException(e);
					Log.d(TAG, "Error " + String.valueOf(e));
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

	public void init(){
		_PACKAGE_NAME = getApplicationContext().getPackageName();
		APP_DB_PATH_SHORT = Environment.DIRECTORY_DOWNLOADS + "/"+_PACKAGE_NAME+"/downloads/app/";
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



	private void download(String _urlfile, String _location) {

		Thread downloadThread = new Thread() {

			long percents = 0;
			String msg = null;

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





			@Override
			public void run() {





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

					}
				});


				//func.deleteDir(destinationDir);
				//new File(destinationDir).mkdirs();
				File saveTo = new File(destinationDir, fileName);
				File saveToDir = new File(destinationDir);
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
				Log.d(TAG, String.valueOf(saveTo));


				//totalDownloaded += count;
				//percents = (totalDownloaded * 100 / fileLength);
				Log.d(TAG + " totalDownloaded", String.valueOf(totalDownloaded));
				Log.d(TAG + " fileLength", String.valueOf(fileLength));

				//totalDownloaded = fileLength;

				if(totalDownloaded == fileLength) {

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
					}, 3000);
				}


			}
		};
		//downloadTask.execute(file_url, destinationDir);
		downloadThread.start();

	}


	public void installApp(String fullPathApp) {
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
		}
	}

}
