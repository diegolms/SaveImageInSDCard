package com.example.saveimagesdcard;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements OnClickListener {

	private Button buttonSave;
	private Button buttonLoad;
	private Bitmap bitmap;
	private HttpResponse response = null;

	private final String FOLDER = Environment.getExternalStorageDirectory()
			+ File.separator + "ExemploSaveImageSDCard" + File.separator;

	private final String urlImageDownload = "http://www.online-image-editor.com//styles/2014/images/example_image.png";
	private final String urlImageLoad = FOLDER + "ImagemGato.png";

	private ProgressDialog pd;
	public static final int OK = 1;
	public static final int FAIL = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		buttonSave = (Button) findViewById(R.id.buttonSave);
		buttonLoad = (Button) findViewById(R.id.buttonLoad);

		buttonSave.setOnClickListener(this);
		buttonLoad.setOnClickListener(this);

		createFolder();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonLoad:
			loadImage();
			break;
		case R.id.buttonSave:
			File image = new File(urlImageLoad);
			if (image.exists()) {
				Toast.makeText(MainActivity.this,
						"Imagem Já Existe!", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			downloadImageFromURL();
			break;
		}

	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if ((pd != null) && (pd.isShowing())) {
				pd.dismiss();
				switch (msg.what) {
				case OK:
					Toast.makeText(getApplicationContext(),
							"File is Saved in  " + FOLDER + "ImagemGato.png",
							1000).show();
					break;
				case FAIL:
					new AlertDialog.Builder(MainActivity.this).setMessage(
							"Problema ao salvar Imagem").show();
					break;
				}
			}
		};
	};

	private class SaveImageAsyncTask extends
			AsyncTask<Void, Void, HttpResponse> {

		@Override
		protected HttpResponse doInBackground(Void... params) {
			try {

				HttpClient httpClient = new DefaultHttpClient();

				HttpGet httpget = new HttpGet(urlImageDownload);
				response = httpClient.execute(httpget);

			} catch (Exception e) {
				Log.i("Error", e.getMessage());
			}
			return response;
		}

		@Override
		protected void onPostExecute(HttpResponse result) {
			super.onPostExecute(result);
			if (result != null) {
				saveImageInSDCard();
				handler.sendEmptyMessage(OK);
			} else {
				handler.sendEmptyMessage(FAIL);

			}
		}
	}

	private void createFolder() {

		try {
			if (!new File(FOLDER).exists()) {
				new File(FOLDER).mkdir();
			}
		} catch (Exception e) {
			Log.d("Exemplo Save Image SDCard", e.getMessage());
		}
	}

	private void saveImageInSDCard() {

		try {

			InputStream is = response.getEntity().getContent();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int buff = 0;
			while ((buff = is.read()) != -1) {
				baos.write(buff);
			}

			byte[] result = baos.toByteArray();

			OutputStream fos = null;

			File image = new File(urlImageLoad);
			if (!image.exists()) {
				image.createNewFile();
				fos = new FileOutputStream(image);
				fos.write(result);
			}
		} catch (Exception e) {
			Log.d("ExemploSaveImageSDCARD", e.getMessage());
		}
	}

	private void downloadImageFromURL() {

		pd = new ProgressDialog(this);
		pd.setMessage("Salvando Imagem...");
		pd.show();

		new SaveImageAsyncTask().execute();

	}

	private void loadImage() {
		File image = new File(urlImageLoad);
		if (!image.exists()) {
			Toast.makeText(MainActivity.this,
					"Imagem Não Encontrada", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		Uri path = Uri.fromFile(image);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(path, "image/*");
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(MainActivity.this,
					"No Application available to view Image", Toast.LENGTH_SHORT)
					.show();
		}
	}
}
