package com.hwajung.ksa.calligraphyhelper.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.hwajung.ksa.calligraphyhelper.R;

import java.io.File;

/**
 * Created by Jaemin on 2015-06-06.
 */
public class CameraActivity extends Activity {

    static final int CAMERA = 1, CROP = 2, GALLERY = 3;
    private static final int BLACK = Color.BLACK;
    private static final int WHITE = Color.WHITE;

    ImageView imageView;
    SeekBar seekBar;

    Bitmap bitmap, primaryBitmap;

    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imageView = (ImageView) findViewById(R.id.imageView_camera);
        seekBar = (SeekBar) findViewById(R.id.seekBar_sensitivity);

        Intent i = getIntent();
        int type = i.getIntExtra("TYPE", GALLERY);

        if (type == CAMERA) {
            File file = new File(getExternalFilesDir(null), "image.jpg");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            startActivityForResult(intent, CAMERA);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, GALLERY);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                process();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA:
                    file = new File(getExternalFilesDir(null), "image.jpg");
                    crop(Uri.fromFile(file));
                    break;
                case GALLERY:
                    Uri uri = data.getData();
                    crop(uri);
                    break;
                case CROP:
                    Bundle extras = data.getExtras();
                    bitmap = extras.getParcelable("data");
                    imageView.setImageBitmap(bitmap);
                    primaryBitmap = bitmap;

                    process();
                    break;
            }
        }
    }

    private void process() {
        if (primaryBitmap != null) {

            int bitmapWidth = primaryBitmap.getWidth();
            int bitmapHeight = primaryBitmap.getHeight();

            int[] colors = new int[bitmapHeight * bitmapWidth];
            primaryBitmap.getPixels(colors, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);

            for (int i = 0; i < colors.length; i++)
                if (Color.red(colors[i]) + Color.green(colors[i]) + Color.blue(colors[i]) < seekBar.getProgress() * 3)
                    colors[i] = BLACK;
                else
                    colors[i] = WHITE;

            bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(colors, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);
            imageView.setImageBitmap(bitmap);
        }
    }

    private void crop(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP);
    }
}
