package ru.argara.selfupdatingapp;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.httpclient.android.BuildConfig;

public class PermissionHelper extends AppCompatActivity {

	private int PERMISSION_REQUEST_CODE = 1;

	private Activity activity;

	private static PermissionHelper.Listener listener;

	public PermissionHelper(Activity activity , int code) {
		this.activity = activity;
		this.PERMISSION_REQUEST_CODE = code;
	}

	public interface Listener {
		//public void permissionOn(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
		void permissionOn();
		//public void permissionOff(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
		void permissionOff();
	}


	public void setListener(PermissionHelper.Listener listener){
		PermissionHelper.listener = listener;
	}


	private void setRequestCode(int code) {
		PERMISSION_REQUEST_CODE = code;
	}

	public boolean checkPermission(String permission) {
		//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			int result = ContextCompat.checkSelfPermission(activity, permission);
			return (result == PackageManager.PERMISSION_GRANTED);
		//}
		//return false;
	}



	boolean checkPermission(String[] permissions){
		int res = 0;
		//boolean res = true;
		boolean allowed = false;

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
			allowed = allowed &&  checkPermission(perms);
		}
		return allowed;

	}



	public void requestPermission(String permission) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			ActivityCompat.requestPermissions(activity, new String[]{permission}, PERMISSION_REQUEST_CODE);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			if(!Environment.isExternalStorageManager()){


				try {
					Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
					Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
					startActivity(intent);
				} catch (Exception ex){
					Intent intent = new Intent();
					intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
					startActivity(intent);
				}

			}
		}

	}

	public void requestPermission(String[] permissions) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			ActivityCompat.requestPermissions(activity, permissions, PERMISSION_REQUEST_CODE);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			if(!Environment.isExternalStorageManager()){
				Intent intent;

				Log.d("FAB requestPermission" , "0");
								try {


									intent = new Intent();
									intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
									//startActivity(intent);
									Log.d("FAB requestPermission" , "2");

								} catch (Exception ex){
									//Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
									Uri uri = Uri.parse("package:ru.argara.selfupdatingapp");
									intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
									//startActivity(intent);
									Log.d("FAB requestPermission" , "1");
								}

								mStartForResult.launch(intent);

		}
	}
	}

	ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
			new ActivityResultCallback<ActivityResult>() {
				@Override
				public void onActivityResult(ActivityResult result) {
					if(result.getResultCode() == Activity.RESULT_OK){
						listener.permissionOn();
					} else {
						listener.permissionOff();
					}
				}
			});

	public boolean verifyPermissions(int[] grantResults) {
		if (grantResults.length < 1) {
			return false;
		}
		for (int result : grantResults) {
			if (result != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}

	public boolean shouldShowRequestPermissionRationale(String permission) {
		return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
	}

	void ff(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			if(!Environment.isExternalStorageManager()){
				Snackbar.make(activity.findViewById(android.R.id.content), "Необходимо разрешение!", Snackbar.LENGTH_INDEFINITE)
						.setAction("Настройки", new View.OnClickListener() {
							@Override
							public void onClick(View v) {

								try {
									Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
									Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
									startActivity(intent);
								} catch (Exception ex){
									Intent intent = new Intent();
									intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
									startActivity(intent);
								}
							}
						})
						.show();
			}
		}
	}
}