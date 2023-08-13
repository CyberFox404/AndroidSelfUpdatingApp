package ru.argara.selfupdatingapp;

import static androidx.core.app.ActivityCompat.requestPermissions;
import static androidx.core.content.PermissionChecker.checkCallingOrSelfPermission;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;

import cz.msebera.android.httpclient.Header;
import cz.msebera.httpclient.android.BuildConfig;

public class MyRequestPermissions extends AppCompatActivity {

	private static MyRequestPermissions.Listener listener;

	private int PERMISSION_REQUEST_CODE = 101;

	String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
	Context ctx;


	public MyRequestPermissions(Context context) {
		ctx = context;
	}

	public interface Listener {
		public void permissionOn(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
		public void permissionOff(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
	}


	public void setListener(MyRequestPermissions.Listener listener){
		MyRequestPermissions.listener = listener;
	}

	private void setRequestCode(int code){
		PERMISSION_REQUEST_CODE = code;
	}


	boolean hasPermissions(){
		int res = 0;

		for (String perms : permissions){
			res = checkCallingOrSelfPermission(perms);
			if (!(res == PackageManager.PERMISSION_GRANTED)){
				return false;
			}
		}
		return true;
	}

	private void requestPerms(){
		//String[] permissions = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
			requestPermissions(permissions,PERMISSION_REQUEST_CODE);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		boolean allowed = true;

		if(requestCode == PERMISSION_REQUEST_CODE) {
			for (int res : grantResults) {
				// if user granted all permissions.
				allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
			}
		} else {
			allowed = false;
		}

		if (allowed) {
			listener.permissionOn(requestCode, permissions, grantResults);
		} else {
			listener.permissionOff(requestCode, permissions, grantResults);
		}

	}

	private boolean isPermissionGranted(String permission) {
		// проверяем разрешение - есть ли оно у нашего приложения
		int permissionCheck = ActivityCompat.checkSelfPermission(this, permission);
		// true - если есть, false - если нет
		return permissionCheck == PackageManager.PERMISSION_GRANTED;
	}

	private void requestPermission(String permission, int requestCode) {
		// запрашиваем разрешение
		ActivityCompat.requestPermissions(this,
				new String[]{permission}, requestCode);
	}

	public void requestMultiplePermissions() {
		ActivityCompat.requestPermissions(this,
				permissions,
				PERMISSION_REQUEST_CODE
		);
	}

	void ff(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			if(!Environment.isExternalStorageManager()){
				Snackbar.make(findViewById(android.R.id.content), "Необходимо разрешение!", Snackbar.LENGTH_INDEFINITE)
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
