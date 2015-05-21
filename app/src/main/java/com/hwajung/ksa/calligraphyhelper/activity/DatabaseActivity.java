package com.hwajung.ksa.calligraphyhelper.activity;

import android.app.Activity;
import android.app.ProgressDialog;
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

    Thread databaseThread = new Thread() {
        @Override
        public void run() {

            boolean success = true;

            TypedArray typedArray = getResources().obtainTypedArray(R.array.letters_drawable_id_array);
            int[] lettersCategory = getResources().getIntArray(R.array.letters_category);
            byte[] data = new byte[typedArray.length() * 3];
            for (int i = 0; i < typedArray.length(); i++) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), typedArray.getResourceId(i, -1));
                data[i * 3] = (byte) (i / 128);
                data[i * 3 + 1] = (byte) (i % 128);
                data[i * 3 + 2] = (byte) lettersCategory[i];
                try {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100,
                            getApplicationContext().openFileOutput(getString(R.string.fileName_letterResource) + i, MODE_PRIVATE));

                    float ratio = 1.f * getResources().getInteger(R.integer.preview_bitmap_size) / Math.max(bitmap.getWidth(), bitmap.getHeight());
                    Bitmap bitmapPreview = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * ratio), (int) (bitmap.getHeight() * ratio), false);
                    bitmapPreview.compress(Bitmap.CompressFormat.PNG, 100,
                            getApplicationContext().openFileOutput(getString(R.string.fileName_previewResource) + i, MODE_PRIVATE));
                } catch (IOException ioException) {
                    success = false;
                }

            }
            try {
                FileOutputStream fos = openFileOutput(getString(R.string.fileName_letterResource), MODE_PRIVATE);
                fos.write(data);
                fos.flush();
                fos.close();
            } catch (IOException ioException) {
                success = false;
            }

            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();

            if (success)
                setResult(RESULT_OK);
            else
                setResult(RESULT_CANCELED);

            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        progressDialog = ProgressDialog.show(DatabaseActivity.this, "", "Making database. Please wait.", false);

        databaseThread.start();
    }
}
