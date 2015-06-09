package com.hwajung.ksa.calligraphyhelper.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hwajung.ksa.calligraphyhelper.R;
import com.hwajung.ksa.calligraphyhelper.adapter.LetterAdapter;
import com.hwajung.ksa.calligraphyhelper.view.FontButton;
import com.hwajung.ksa.calligraphyhelper.view.FontTextView;
import com.hwajung.ksa.calligraphyhelper.view.MultiButton;
import com.hwajung.ksa.calligraphyhelper.view.PenPreView;
import com.hwajung.ksa.calligraphyhelper.view.SketchView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * (C) 2015. Jaemin Hong all rights reserved.
 */

public class SketchActivity extends Activity {

    final int STATE_MENU_VISIBLE = 0, STATE_MENU_INVISIBLE = 1, STATE_MENU_APPEARING = 2, STATE_MENU_DISAPPEARING = 3;
    final int REQUEST_CAMERA = 1, REQUEST_DRAW = 2, REQUEST_GALLERY = 3;
    final int ACTION_SAVE = 0, ACTION_SAVEAS = 1, ACTION_LOAD = 2, ACTION_NEW = 3, ACTION_FINISH = 4;

    ProgressDialog progressDialog;

    SketchView sketchView;
    ImageButton button_menu, button_undo, button_redo, button_add, button_cancel, button_circle, button_rectangle;
    MultiButton multiButton_menu;
    GridView gridView_letter;
    ScrollView scrollView_menu;
    TextView textView_fileName;
    Spinner spinner;
    LinearLayout linearLayout_letter, linearLayout_draw, linearLayout_pen;
    SeekBar seekBar_thickness, seekBar_degree;
    PenPreView penPreView;

    Animation animation_menu_appear, animation_menu_disappear, animation_newLetter_appear, animation_newLetter_disappear,
            animation_pen_appear, animation_pen_disappear;

    LetterAdapter letterAdapter;

    SharedPreferences sharedPreferences;

    Typeface typeface;

    int menuAnimation = STATE_MENU_INVISIBLE;
    int newLetterAnimation = STATE_MENU_INVISIBLE;
    int penAnimation = STATE_MENU_INVISIBLE;

    String currentFileName, loadingFileName;
    boolean isNewFile = true, modified = false;
    int action = -1;

    int selected = 0;
    private Thread databaseThread = new Thread() {

        @Override
        public void run() {

            final TypedArray typedArray = getResources().obtainTypedArray(R.array.letters_drawable_id_array);
            final int[] lettersCategory = getResources().getIntArray(R.array.letters_category);
            final byte[] data = new byte[typedArray.length()];

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.setMax(typedArray.length());
                    progressDialog.setProgress(0);
                }
            });

            for (int i = 0; i < typedArray.length(); i++) {

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), typedArray.getResourceId(i, -1));

                data[i] = (byte) lettersCategory[i];
                try {
                    float ratio1 = 1.f * getResources().getInteger(R.integer.save_bitmap_size) / Math.max(bitmap.getWidth(), bitmap.getHeight());
                    Bitmap bitmapSave = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * ratio1), (int) (bitmap.getHeight() * ratio1), false);
                    bitmapSave.compress(Bitmap.CompressFormat.PNG, 100,
                            getApplicationContext().openFileOutput(getString(R.string.fileName_letterResource) + i, MODE_PRIVATE));

                    float ratio2 = 1.f * getResources().getInteger(R.integer.preview_bitmap_size) / Math.max(bitmap.getWidth(), bitmap.getHeight());
                    Bitmap bitmapPreview = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * ratio2), (int) (bitmap.getHeight() * ratio2), false);
                    bitmapPreview.compress(Bitmap.CompressFormat.PNG, 100,
                            getApplicationContext().openFileOutput(getString(R.string.fileName_previewResource) + i, MODE_PRIVATE));
                } catch (IOException ioException) {
                    finish();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.incrementProgressBy(1);
                    }
                });

            }
            try {
                FileOutputStream fos = openFileOutput(getString(R.string.fileName_letterResource), MODE_PRIVATE);
                fos.write(data);
                fos.flush();
                fos.close();
            } catch (IOException ioException) {
                finish();
            }

            letterAdapter.load(data);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(getResources().getString(R.string.sharedPreferences_version), getResources().getInteger(R.integer.version));
            editor.apply();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    gridView_letter.setAdapter(letterAdapter);
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                }
            });

            Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make Typeface
        typeface = Typeface.createFromAsset(getAssets(), getString(R.string.fontName));
        FontTextView.setClassTypeface(typeface);
        FontButton.setClassTypeface(typeface);

        // Set view
        setContentView(R.layout.activity_sketch);

        // Find views
        sketchView = (SketchView) findViewById(R.id.sketchView);
        button_menu = (ImageButton) findViewById(R.id.button_menu);
        button_redo = (ImageButton) findViewById(R.id.button_redo);
        button_undo = (ImageButton) findViewById(R.id.button_undo);
        button_add = (ImageButton) findViewById(R.id.button_draw_add);
        button_cancel = (ImageButton) findViewById(R.id.button_draw_cancel);
        button_circle = (ImageButton) findViewById(R.id.button_circle);
        button_rectangle = (ImageButton) findViewById(R.id.button_rectangle);
        multiButton_menu = (MultiButton) findViewById(R.id.multiButton_menu);
        gridView_letter = (GridView) findViewById(R.id.gridView_letter);
        scrollView_menu = (ScrollView) findViewById(R.id.scrollView_menu);
        textView_fileName = (TextView) findViewById(R.id.textView_fileName);
        spinner = (Spinner) findViewById(R.id.spinner_category);
        linearLayout_letter = (LinearLayout) findViewById(R.id.linearLayout_letter);
        linearLayout_draw = (LinearLayout) findViewById(R.id.linearLayout_draw);
        linearLayout_pen = (LinearLayout) findViewById(R.id.linearLayout_pen);
        seekBar_degree = (SeekBar) findViewById(R.id.seekBar_rectdegree);
        seekBar_thickness = (SeekBar) findViewById(R.id.seekBar_thickness);
        penPreView = (PenPreView) findViewById(R.id.penPreView);

        // Set File Name
        currentFileName = getString(R.string.defaultFileName);

        // Make adpater
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.letters_category_name, R.layout.spinner_category);
        letterAdapter = new LetterAdapter(SketchActivity.this);

        // Set sketchView
        sketchView.setSketchActivity(this);
        if (savedInstanceState != null) {
            byte[] data1 = savedInstanceState.getByteArray("DATA1");
            float[] data2 = savedInstanceState.getFloatArray("DATA2");
            sketchView.setDataByByteArray(data1);
            sketchView.setSettings(data2);
        }

        // Set gridView
        gridView_letter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != -1)
                    sketchView.addLetter((int) letterAdapter.getItemId(i));
                linearLayout_letter.startAnimation(animation_newLetter_disappear);
            }
        });

        // Set spinner
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (letterAdapter != null && letterAdapter.isLoaded()) {
                    letterAdapter.setCategory(i);
                    gridView_letter.setAdapter(letterAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Set animation
        animation_menu_appear = AnimationUtils.loadAnimation(this, R.anim.sketch_menu_appear);
        animation_menu_appear.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                scrollView_menu.setVisibility(View.VISIBLE);
                menuAnimation = STATE_MENU_APPEARING;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                menuAnimation = STATE_MENU_VISIBLE;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        animation_menu_disappear = AnimationUtils.loadAnimation(this, R.anim.sketch_menu_disappear);
        animation_menu_disappear.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                menuAnimation = STATE_MENU_DISAPPEARING;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                scrollView_menu.setVisibility(View.INVISIBLE);
                menuAnimation = STATE_MENU_INVISIBLE;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animation_newLetter_appear = AnimationUtils.loadAnimation(this, R.anim.sketch_newletter_appear);
        animation_newLetter_appear.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                linearLayout_letter.setVisibility(View.VISIBLE);
                newLetterAnimation = STATE_MENU_APPEARING;
                textView_fileName.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                newLetterAnimation = STATE_MENU_VISIBLE;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        animation_newLetter_disappear = AnimationUtils.loadAnimation(this, R.anim.sketch_newletter_disappear);
        animation_newLetter_disappear.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                newLetterAnimation = STATE_MENU_DISAPPEARING;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                newLetterAnimation = STATE_MENU_INVISIBLE;
                linearLayout_letter.setVisibility(View.INVISIBLE);
                textView_fileName.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        animation_pen_appear = AnimationUtils.loadAnimation(this, R.anim.sketch_pen_appear);
        animation_pen_appear.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                linearLayout_pen.setVisibility(View.VISIBLE);
                penAnimation = STATE_MENU_APPEARING;
                textView_fileName.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                penAnimation = STATE_MENU_VISIBLE;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        animation_pen_disappear = AnimationUtils.loadAnimation(this, R.anim.sketch_pen_disappear);
        animation_pen_disappear.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                penAnimation = STATE_MENU_DISAPPEARING;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                penAnimation = STATE_MENU_INVISIBLE;
                linearLayout_pen.setVisibility(View.INVISIBLE);
                textView_fileName.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        // Set menu button listener
        button_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (menuAnimation) {
                    case STATE_MENU_INVISIBLE:
                        scrollView_menu.startAnimation(animation_menu_appear);
                        break;
                    case STATE_MENU_VISIBLE:
                        scrollView_menu.startAnimation(animation_menu_disappear);
                        break;
                    case STATE_MENU_APPEARING:
                        break;
                    case STATE_MENU_DISAPPEARING:
                        break;
                }
            }
        });

        // Set undo, redo buttons
        button_redo.setEnabled(false);
        button_redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sketchView.redo();
            }
        });
        button_undo.setEnabled(false);
        button_undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sketchView.undo();
            }
        });

        // Set drawing mode buttons
        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap screenShot = sketchView.getBitmapImage(true);

                try {
                    File file = new File(getExternalFilesDir(null), "image.jpg");
                    screenShot.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
                    Uri uri = Uri.fromFile(file);
                    crop(uri);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), R.string.failLoadLetter, Toast.LENGTH_SHORT).show();
                }

                endDrawing();
            }
        });

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endDrawing();
            }
        });

        button_circle.setEnabled(false);
        button_circle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_circle.setEnabled(false);
                button_rectangle.setEnabled(true);
                sketchView.setIsCircle(true);
                seekBar_degree.setEnabled(false);
                penPreView.setCircle(true);
            }
        });

        button_rectangle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_circle.setEnabled(true);
                button_rectangle.setEnabled(false);
                sketchView.setIsCircle(false);
                seekBar_degree.setEnabled(true);
                penPreView.setCircle(false);
            }
        });

        // Set drawing mode seekBars
        seekBar_thickness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                penPreView.setThickness(seekBar.getProgress() * 0.5f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sketchView.setThickness(seekBar.getProgress() * 0.5f);
            }
        });

        seekBar_degree.setEnabled(false);
        seekBar_degree.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                penPreView.setDegree((float) Math.toRadians(seekBar.getProgress() - 90));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sketchView.setDegree((float) Math.toRadians(seekBar.getProgress() - 90));
            }
        });

        // Set multiButton
        multiButton_menu.setDrawableID(new int[]{R.drawable.sketch_new,
                R.drawable.sketch_load,
                R.drawable.sketch_save,
                R.drawable.sketch_saveas,
                R.drawable.sketch_share,
                R.drawable.sketch_fitting,
                R.drawable.sketch_newletter,
                R.drawable.sketch_draw,
                R.drawable.sketch_camera,
                R.drawable.sketch_gallery,
                R.drawable.sketch_help});

        // Save button clicked
        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMenu();
                action = ACTION_SAVE;
                save();
            }
        }, R.drawable.sketch_save);

        // Load button clicked
        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMenu();
                action = ACTION_LOAD;
                load();
            }
        }, R.drawable.sketch_load);

        // New button clicked
        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMenu();
                action = ACTION_NEW;
                newFile();
            }
        }, R.drawable.sketch_new);

        // New letter button clicked
        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                letterAdapter.load();
                gridView_letter.setAdapter(letterAdapter);

                scrollView_menu.startAnimation(animation_menu_disappear);
                if (newLetterAnimation == STATE_MENU_INVISIBLE) {
                    linearLayout_letter.startAnimation(animation_newLetter_appear);
                }
            }
        }, R.drawable.sketch_newletter);

        // Drawing mode button clicked
        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMenu();
                sketchView.setDrawingMode(true);
                button_menu.setVisibility(View.INVISIBLE);
                linearLayout_draw.setVisibility(View.VISIBLE);
                button_add.setEnabled(false);
                linearLayout_pen.startAnimation(animation_pen_appear);
            }
        }, R.drawable.sketch_draw);

        // Camera button clicked
        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMenu();
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                intent.putExtra("TYPE", REQUEST_CAMERA);
                startActivityForResult(intent, REQUEST_CAMERA);
            }
        }, R.drawable.sketch_camera);

        // Gallery button clicked
        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMenu();
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                intent.putExtra("TYPE", REQUEST_GALLERY);
                startActivityForResult(intent, REQUEST_GALLERY);
            }
        }, R.drawable.sketch_gallery);

        // Share button clicked
        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMenu();
                Bitmap bitmap = sketchView.getBitmapImage(false);
                try {
                    File file = new File(getExternalFilesDir(null), "image.jpg");
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));

                    Uri uri = Uri.fromFile(file);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/jpg");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(intent, getString(R.string.share)));
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), R.string.failLoadImageFile, Toast.LENGTH_SHORT).show();
                }
            }
        }, R.drawable.sketch_share);

        // Fitting button clicked
        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMenu();
                sketchView.regulateFitting();
            }
        }, R.drawable.sketch_fitting);

        // SaveAs button clicked
        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMenu();
                action = ACTION_SAVEAS;
                saveAs();
            }
        }, R.drawable.sketch_saveas);

        // Help button clicked
        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMenu();
                Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
                startActivity(intent);
            }
        }, R.drawable.sketch_help);

        // Make database
        sharedPreferences = getSharedPreferences(getResources().getString(R.string.sharedPreferences_name), MODE_PRIVATE);
        int version = sharedPreferences.getInt(getResources().getString(R.string.sharedPreferences_version), 0);
        if (version != getResources().getInteger(R.integer.version)) {
            try {
                FileOutputStream fos = getApplicationContext().openFileOutput(getString(R.string.fileName_saveFileNames), MODE_APPEND);
                fos.close();
            } catch (IOException ioe) {
                Toast.makeText(getApplicationContext(), R.string.failFindSaving, Toast.LENGTH_SHORT).show();
                finish();
            }

            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage(getString(R.string.makingDatabase));
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();

            databaseThread.start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CAMERA:
            case REQUEST_GALLERY:
                if (resultCode == RESULT_OK) {
                    int id = data.getIntExtra("ID", -1);
                    if (id != -1) {
                        sketchView.addLetter(id);
                    }
                }
                break;
            case REQUEST_DRAW:
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    final Bitmap bitmap = extras.getParcelable("data");

                    AlertDialog.Builder adb = new AlertDialog.Builder(SketchActivity.this);

                    final View dialogView = getLayoutInflater().inflate(R.layout.dialog_lettersave, null);
                    adb.setView(dialogView);

                    final Spinner spinner = (Spinner) dialogView.findViewById(R.id.spinner_newLetterCategory);
                    ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(SketchActivity.this,
                            R.array.letters_category_name, R.layout.spinner_category_holo);
                    spinner.setAdapter(spinnerAdapter);

                    adb.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                String selected = spinner.getSelectedItem().toString();

                                String[] categories = getResources().getStringArray(R.array.letters_category_name);
                                byte index = -1;
                                for (byte j = 0; j < categories.length; j++) {
                                    if (selected.equals(categories[j])) {
                                        index = j;
                                        break;
                                    }
                                }

                                FileInputStream fis = getApplicationContext().openFileInput(getString(R.string.fileName_letterResource));
                                int length = fis.available();

                                float ratio1 = 1.f * getResources().getInteger(R.integer.save_bitmap_size) / Math.max(bitmap.getWidth(), bitmap.getHeight());
                                Bitmap bitmapSave = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * ratio1), (int) (bitmap.getHeight() * ratio1), false);
                                bitmapSave.compress(Bitmap.CompressFormat.PNG, 100,
                                        getApplicationContext().openFileOutput(getString(R.string.fileName_letterResource) + length, MODE_PRIVATE));

                                float ratio2 = 1.f * getResources().getInteger(R.integer.preview_bitmap_size) / Math.max(bitmap.getWidth(), bitmap.getHeight());
                                Bitmap bitmapPreview = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * ratio2), (int) (bitmap.getHeight() * ratio2), false);
                                bitmapPreview.compress(Bitmap.CompressFormat.PNG, 100,
                                        getApplicationContext().openFileOutput(getString(R.string.fileName_previewResource) + length, MODE_PRIVATE));

                                FileOutputStream fos = openFileOutput(getString(R.string.fileName_letterResource), MODE_APPEND);
                                fos.write(new byte[]{index});
                                fos.flush();
                                fos.close();

                                sketchView.addLetter(length);
                                letterAdapter.load();
                                gridView_letter.setAdapter(letterAdapter);
                            } catch (IOException ioe) {
                                Toast.makeText(getApplicationContext(), R.string.failSavingLetter, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                    adb.setNegativeButton(R.string.cancel, null);

                    adb.setCancelable(false);
                    AlertDialog ad = adb.show();

                    TextView textView_b1 = (TextView) ad.findViewById(android.R.id.button1);
                    TextView textView_b2 = (TextView) ad.findViewById(android.R.id.button2);
                    textView_b1.setTypeface(typeface);
                    textView_b2.setTypeface(typeface);
                }
                break;
        }

    }

    @Override
    public void onBackPressed() {
        if (penAnimation == STATE_MENU_VISIBLE)
            endDrawing();
        else if (menuAnimation == STATE_MENU_VISIBLE)
            scrollView_menu.startAnimation(animation_menu_disappear);
        else if (newLetterAnimation == STATE_MENU_VISIBLE)
            linearLayout_letter.startAnimation(animation_newLetter_disappear);
        else {
            action = ACTION_FINISH;
            if (modified) {
                showWantToSaveDialog();
            } else {
                finish();
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putByteArray("DATA1", sketchView.getData());
        outState.putFloatArray("DATA2", sketchView.getSettings());
    }

    private void save() {
        if (isNewFile) {
            showSaveDialog();
        } else {
            saveFile(currentFileName, false);
            action();
        }
    }

    private void saveAs() {
        showSaveDialog();
    }

    private void load() {
        showLoadDialog();
    }

    private void newFile() {
        if (modified) {
            showWantToSaveDialog();
        } else {
            action();
        }
    }

    private void showSaveDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(SketchActivity.this);
        adb.setTitle(R.string.saveFile);
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_save, null);
        adb.setView(dialogView);

        final EditText editText = (EditText) dialogView.findViewById(R.id.editText_save);
        editText.setTypeface(typeface);

        adb.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String name = editText.getText().toString();

                if (name.length() == 0) {
                    Toast.makeText(getApplicationContext(), R.string.fileNameEmpty, Toast.LENGTH_SHORT).show();
                    showSaveDialog();
                    return;
                }

                final String[] names = getFileNames();
                boolean b = false;
                for (String name_ : names) {
                    if (name.equals(name_)) {
                        b = true;
                        break;
                    }
                }

                if (b) {
                    showOverrideDialog(name);
                } else {
                    saveFile(name, true);
                    action();
                }
            }
        });
        adb.setNegativeButton(R.string.cancel, null);

        adb.setCancelable(false);
        AlertDialog ad = adb.show();

        TextView textView_b1 = (TextView) ad.findViewById(android.R.id.button1);
        TextView textView_b2 = (TextView) ad.findViewById(android.R.id.button2);
        textView_b1.setTypeface(typeface);
        textView_b2.setTypeface(typeface);
    }

    private void showOverrideDialog(String fileName) {
        AlertDialog.Builder adb = new AlertDialog.Builder(SketchActivity.this);
        adb.setMessage(R.string.fileOverwrite);

        final String name = fileName;
        adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                saveFile(name, false);
            }
        });
        adb.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showSaveDialog();
            }
        });

        adb.setCancelable(false);
        AlertDialog ad = adb.show();

        TextView textView_b1 = (TextView) ad.findViewById(android.R.id.button1);
        TextView textView_b2 = (TextView) ad.findViewById(android.R.id.button2);
        TextView textView_b3 = (TextView) ad.findViewById(android.R.id.message);
        textView_b1.setTypeface(typeface);
        textView_b2.setTypeface(typeface);
        textView_b3.setTypeface(typeface);
    }

    private void showLoadDialog() {
        final String[] names = getFileNames();

        AlertDialog.Builder adb = new AlertDialog.Builder(SketchActivity.this);
        adb.setTitle(R.string.openFile);
        selected = 0;
        adb.setSingleChoiceItems(names, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                selected = i;
            }
        });

        adb.setPositiveButton(R.string.open, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String name = names[selected];
                loadingFileName = name;
                if (!isNewFile && currentFileName.equals(name))
                    Toast.makeText(getApplicationContext(), R.string.fileAlreadyOpen, Toast.LENGTH_SHORT).show();
                else if (modified) {
                    showWantToSaveDialog();
                } else {
                    loadFile();
                }
            }
        });
        adb.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String name = names[selected];
                if (!isNewFile && currentFileName.equals(name))
                    Toast.makeText(getApplicationContext(), R.string.fileAlreadyOpen, Toast.LENGTH_SHORT).show();
                else {
                    deleteFile(name, names);
                }
            }
        });
        adb.setNegativeButton(R.string.cancel, null);

        adb.setCancelable(false);

        AlertDialog ad = adb.show();

        TextView textView_b1 = (TextView) ad.findViewById(android.R.id.button1);
        TextView textView_b2 = (TextView) ad.findViewById(android.R.id.button2);
        TextView textView_b3 = (TextView) ad.findViewById(android.R.id.button3);
        textView_b1.setTypeface(typeface);
        textView_b2.setTypeface(typeface);
        textView_b3.setTypeface(typeface);
    }

    private void showWantToSaveDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(SketchActivity.this);
        adb.setMessage(R.string.wantToSave);

        adb.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                save();
            }
        });
        adb.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                action();
            }
        });
        adb.setNeutralButton(R.string.cancel, null);

        adb.setCancelable(false);
        AlertDialog ad = adb.show();

        TextView textView_b1 = (TextView) ad.findViewById(android.R.id.button1);
        TextView textView_b2 = (TextView) ad.findViewById(android.R.id.button2);
        TextView textView_b3 = (TextView) ad.findViewById(android.R.id.button3);
        TextView textView_b4 = (TextView) ad.findViewById(android.R.id.message);
        textView_b1.setTypeface(typeface);
        textView_b2.setTypeface(typeface);
        textView_b3.setTypeface(typeface);
        textView_b4.setTypeface(typeface);
    }

    private void action() {
        switch (action) {
            case ACTION_LOAD:
                loadFile();
                break;
            case ACTION_NEW:
                sketchView.clear();
                sketchView.invalidate();
                textView_fileName.setText(R.string.defaultFileName);
                currentFileName = getString(R.string.defaultFileName);
                isNewFile = true;
                modified = false;
                break;
            case ACTION_FINISH:
                finish();
                break;
        }
    }

    private boolean saveFile(String fileName, boolean needAddition) {
        // Save file
        try {
            FileOutputStream fos = getApplicationContext().openFileOutput(fileName, MODE_PRIVATE);
            byte[] data = sketchView.getData();
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (IOException ioe) {
            Toast.makeText(getApplicationContext(), R.string.fileSavingFail, Toast.LENGTH_SHORT).show();
            return false;
        }

        // Add file name on the file name list and save
        if (needAddition) {
            try {
                FileOutputStream fos = getApplicationContext().openFileOutput(getString(R.string.fileName_saveFileNames), MODE_APPEND);
                byte[] data = (fileName + "\t").getBytes();
                fos.write(data);
                fos.flush();
                fos.close();
            } catch (IOException ioe) {
                Toast.makeText(getApplicationContext(), R.string.fileSavingFail, Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        currentFileName = fileName;
        textView_fileName.setText(fileName);
        isNewFile = false;
        modified = false;
        Toast.makeText(getApplicationContext(), R.string.fileSavingSuccess, Toast.LENGTH_SHORT).show();

        return true;
    }

    private boolean loadFile() {
        try {
            FileInputStream fis = getApplicationContext().openFileInput(loadingFileName);
            byte[] data = new byte[fis.available()];
            fis.read(data);
            fis.close();

            sketchView.setDataByByteArray(data);
        } catch (IOException ioe) {
            Toast.makeText(getApplicationContext(), R.string.fileLoadingFail, Toast.LENGTH_SHORT).show();
            return false;
        }

        textView_fileName.setText(loadingFileName);
        currentFileName = loadingFileName;
        isNewFile = false;
        modified = false;
        Toast.makeText(getApplicationContext(), R.string.fileLoadingSuccess, Toast.LENGTH_SHORT).show();

        return true;
    }

    private boolean deleteFile(String fileName, String[] fileNames) {
        try {
            FileOutputStream fos = getApplicationContext().openFileOutput(getString(R.string.fileName_saveFileNames), MODE_PRIVATE);
            String names = "";
            for (int j = 0; j < fileNames.length; j++)
                if (selected != j)
                    names += fileNames[j] + "\t";
            byte[] data = names.getBytes();
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (IOException ioe) {
            Toast.makeText(getApplicationContext(), R.string.fileDeleteFail, Toast.LENGTH_SHORT).show();
            return false;
        }

        Toast.makeText(getApplicationContext(), R.string.fileDeleteSuccess, Toast.LENGTH_SHORT).show();
        return true;
    }

    private String[] getFileNames() {
        // Load existing file name list
        String fileNames = "";
        try {
            FileInputStream fis = getApplicationContext().openFileInput(getString(R.string.fileName_saveFileNames));
            byte[] data = new byte[fis.available()];
            fis.read(data);
            fis.close();
            fileNames = new String(data);
        } catch (IOException ioException) {
            Toast.makeText(getApplicationContext(), getString(R.string.fileLoadingFail), Toast.LENGTH_SHORT).show();
            return new String[]{};
        }

        if (fileNames.equals(""))
            return new String[]{};
        return fileNames.split("\t");
    }

    private void crop(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("return-data", true);
        startActivityForResult(intent, REQUEST_DRAW);
    }

    public void setAddButton(boolean enabled) {
        button_add.setEnabled(enabled);
    }

    public void setUndoButton(boolean enabled) {
        button_undo.setEnabled(enabled);
    }

    public void setRedoButton(boolean enabled) {
        button_redo.setEnabled(enabled);
    }

    public void closeMenu() {
        if (menuAnimation == STATE_MENU_VISIBLE)
            scrollView_menu.startAnimation(animation_menu_disappear);
        if (newLetterAnimation == STATE_MENU_VISIBLE)
            linearLayout_letter.startAnimation(animation_newLetter_disappear);
    }

    private void endDrawing() {
        button_menu.setVisibility(View.VISIBLE);
        linearLayout_draw.setVisibility(View.INVISIBLE);
        sketchView.setDrawingMode(false);
        sketchView.invalidate();
        linearLayout_pen.startAnimation(animation_pen_disappear);
    }

    public void showLetterEditDialog(int initialColor, float initialSize, float initialDegree) {
        AlertDialog.Builder adb = new AlertDialog.Builder(SketchActivity.this);
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit, null);
        adb.setView(dialogView);

        TextView[] textViews = new TextView[7];
        textViews[0] = (TextView) dialogView.findViewById(R.id.textView_colorPreview);
        textViews[1] = (TextView) dialogView.findViewById(R.id.textView_alpha);
        textViews[2] = (TextView) dialogView.findViewById(R.id.textView_red);
        textViews[3] = (TextView) dialogView.findViewById(R.id.textView_green);
        textViews[4] = (TextView) dialogView.findViewById(R.id.textView_blue);
        textViews[5] = (TextView) dialogView.findViewById(R.id.textView_size);
        textViews[6] = (TextView) dialogView.findViewById(R.id.textView_degree);

        for (TextView tv : textViews)
            tv.setTypeface(typeface);

        final ImageView imageView = (ImageView) dialogView.findViewById(R.id.imageView_colorPreview);

        final SeekBar[] seekBars = new SeekBar[5];
        final EditText[] editTexts = new EditText[5];

        seekBars[0] = (SeekBar) dialogView.findViewById(R.id.seekBar_alpha);
        seekBars[1] = (SeekBar) dialogView.findViewById(R.id.seekBar_red);
        seekBars[2] = (SeekBar) dialogView.findViewById(R.id.seekBar_green);
        seekBars[3] = (SeekBar) dialogView.findViewById(R.id.seekBar_blue);
        seekBars[4] = (SeekBar) dialogView.findViewById(R.id.seekBar_degree);

        editTexts[0] = (EditText) dialogView.findViewById(R.id.editText_alpha);
        editTexts[1] = (EditText) dialogView.findViewById(R.id.editText_red);
        editTexts[2] = (EditText) dialogView.findViewById(R.id.editText_green);
        editTexts[3] = (EditText) dialogView.findViewById(R.id.editText_blue);
        editTexts[4] = (EditText) dialogView.findViewById(R.id.editText_degree);

        final SeekBar seekBar = (SeekBar) dialogView.findViewById(R.id.seekBar_size);
        final EditText editText = (EditText) dialogView.findViewById(R.id.editText_size);

        int alpha = Color.alpha(initialColor);
        int red = Color.red(initialColor);
        int green = Color.green(initialColor);
        int blue = Color.blue(initialColor);
        int degree = (int) initialDegree;

        final int[] maxValues = {getResources().getInteger(R.integer.color_max), getResources().getInteger(R.integer.color_max),
                getResources().getInteger(R.integer.color_max), getResources().getInteger(R.integer.color_max), getResources().getInteger(R.integer.degree_max)};
        final int[] values = {alpha, red, green, blue, degree};
        imageView.setBackgroundColor(Color.argb(values[0], values[1], values[2], values[3]));

        for (int i = 0; i < values.length; i++) {
            final int j = i;
            seekBars[j].setProgress(values[j]);
            editTexts[j].setText(String.valueOf(values[j]));
            seekBars[j].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    editTexts[j].setText(String.valueOf(i));
                    values[j] = i;
                    if (j < 4)
                        imageView.setBackgroundColor(Color.argb(values[0], values[1], values[2], values[3]));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            editTexts[j].setTypeface(typeface);
            editTexts[j].setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    String text = editTexts[j].getText().toString();
                    int value;
                    if (text.length() == 0) {
                        editTexts[j].setText("0");
                        value = 0;
                    } else
                        value = Integer.parseInt(text);

                    if (value > maxValues[j]) {
                        value = maxValues[j];
                        editTexts[j].setText(String.valueOf(value));
                    }

                    seekBars[j].setProgress(value);
                }
            });
        }

        final float[] size = {(int) (initialSize * 100) / 10.f};

        seekBar.setProgress((int) (size[0] * 10) - 1);
        editText.setText(String.valueOf(size[0]));
        editText.setTypeface(typeface);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                size[0] = (i + 1) / 10.f;
                editText.setText(String.valueOf(size[0]));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                String text = editText.getText().toString();
                float value;
                if (text.length() == 0) {
                    editText.setText("0");
                    value = 0;
                } else
                    value = Float.parseFloat(text);

                int maxValue = getResources().getInteger(R.integer.size_max);

                if (value > maxValue)
                    value = maxValue;

                size[0] = (int) (value * 10) / 10.f;

                editText.setText(String.valueOf(size[0]));
                seekBar.setProgress((int) (size[0] * 10) - 1);
            }
        });

        adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (int j = 0; j < values.length; j++) {
                    String text = editTexts[j].getText().toString();
                    int value;
                    if (text.length() == 0)
                        value = 0;
                    else
                        value = Integer.parseInt(text);

                    if (value > maxValues[j])
                        value = maxValues[j];

                    values[j] = value;
                }

                String text = editText.getText().toString();
                float value;
                if (text.length() == 0)
                    value = 0;
                else
                    value = Float.parseFloat(text);

                int maxValue = getResources().getInteger(R.integer.size_max);

                if (value > maxValue)
                    value = maxValue;

                float size = value / 10;

                sketchView.modifyLetter(Color.argb(values[0], values[1], values[2], values[3]), size, values[4]);
            }
        });
        adb.setNegativeButton(R.string.cancel, null);
        adb.show();
    }

    public void modify() {
        modified = true;
    }


}

