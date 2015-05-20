package com.hwajung.ksa.calligraphyhelper.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.hwajung.ksa.calligraphyhelper.R;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Jaemin on 2015-05-20.
 */
public class DatabaseActivity extends Activity {

    ProgressDialog progressDialog;
    Intent intent;

    Thread databaseThread = new Thread() {
        @Override
        public void run() {
            TypedArray typedArray = getResources().obtainTypedArray(R.array.letters_drawable_id_array);
            int[] lettersCategory = getResources().getIntArray(R.array.letters_category);
            String data = "";
            for (int i = 0; i < typedArray.length(); i++) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), typedArray.getResourceId(i, -1));
                data += i + "\t" + lettersCategory[i] + "\n";
                try {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, getApplicationContext().openFileOutput("letterBitmapResource" + i, MODE_PRIVATE));
                } catch (IOException ioException) {
                }
            }
            try {
                FileOutputStream fos = openFileOutput("letterBitmapResource", MODE_PRIVATE);
                fos.write(data.getBytes());
                fos.flush();
                fos.close();
            } catch (IOException ioException) {
            }

            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        intent = getIntent();

        progressDialog = ProgressDialog.show(DatabaseActivity.this, "", "Making database. Please wait.", false);

        databaseThread.start();

//        try {
//            bitmap = BitmapFactory.decodeStream(openFileInput("data"));
//        } catch (IOException ioException) {
//        }
    }
}
