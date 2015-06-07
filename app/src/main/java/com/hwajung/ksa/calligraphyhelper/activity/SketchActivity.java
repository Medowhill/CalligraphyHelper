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
import android.util.Log;
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
import com.hwajung.ksa.calligraphyhelper.view.MultiButton;
import com.hwajung.ksa.calligraphyhelper.view.SketchView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class SketchActivity extends Activity {

    final int STATE_MENU_VISIBLE = 0, STATE_MENU_INVISIBLE = 1, STATE_MENU_APPEARING = 2, STATE_MENU_DISAPPEARING = 3;
    final int REQUEST_CAMERA = 1, REQUEST_DRAW = 2, REQUEST_GALLERY = 3;
    final int ACTION_SAVE = 0, ACTION_SAVEAS = 1, ACTION_LOAD = 2, ACTION_NEW = 3, ACTION_FINISH = 4;

    ProgressDialog progressDialog;

    SketchView sketchView;
    ImageButton button_menu, button_undo, button_redo, button_add, button_cancel;
    MultiButton multiButton_menu;
    GridView gridView_letter;
    ScrollView scrollView_menu;
    TextView textView_fileName;
    Spinner spinner;
    LinearLayout linearLayout_letter;

    Animation animation_menu_appear, animation_menu_disappear, animation_newLetter_appear, animation_newLetter_disappear;

    LetterAdapter letterAdapter;

    SharedPreferences sharedPreferences;

    Typeface typeface;

    int menuAnimation = STATE_MENU_INVISIBLE;
    int newLetterAnimation = STATE_MENU_INVISIBLE;

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

                final int x = i;
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

            letterAdapter = new LetterAdapter(SketchActivity.this);
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

        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sketch);

        Log.i("TEST", "go");

        // Find views
        sketchView = (SketchView) findViewById(R.id.sketchView);
        button_menu = (ImageButton) findViewById(R.id.button_menu);
        button_redo = (ImageButton) findViewById(R.id.button_redo);
        button_undo = (ImageButton) findViewById(R.id.button_undo);
        button_add = (ImageButton) findViewById(R.id.button_draw_add);
        button_cancel = (ImageButton) findViewById(R.id.button_draw_cancel);
        multiButton_menu = (MultiButton) findViewById(R.id.multiButton_menu);
        gridView_letter = (GridView) findViewById(R.id.gridView_letter);
        scrollView_menu = (ScrollView) findViewById(R.id.scrollView_menu);
        textView_fileName = (TextView) findViewById(R.id.textView_fileName);
        spinner = (Spinner) findViewById(R.id.spinner_category);
        linearLayout_letter = (LinearLayout) findViewById(R.id.linearLayout_letter);

        // Make Typeface
        typeface = Typeface.createFromAsset(getAssets(), "NanumGothic.ttf");

        // Set Typeface
        textView_fileName.setTypeface(typeface);

        // Set File Name
        currentFileName = getString(R.string.defaultFileName);

        // Set undo, redo buttons
        button_redo.setEnabled(false);
        button_undo.setEnabled(false);

        // Make adpater
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.letters_category_name, R.layout.spinner_category);

        // Set sketchView
        sketchView.setSketchActivity(this);

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

        // Set redo, undo buttons listener
        button_redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sketchView.redo();
            }
        });
        button_undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sketchView.undo();
            }
        });

        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                R.drawable.sketch_setting,
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

        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMenu();
                scrollView_menu.startAnimation(animation_menu_disappear);
                linearLayout_letter.startAnimation(animation_newLetter_appear);
            }
        }, R.drawable.sketch_newletter);

        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMenu();
                sketchView.setDrawingMode(true);
                button_menu.setVisibility(View.INVISIBLE);
            }
        }, R.drawable.sketch_draw);

        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMenu();
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                intent.putExtra("TYPE", REQUEST_CAMERA);
                startActivityForResult(intent, REQUEST_CAMERA);
            }
        }, R.drawable.sketch_camera);

        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMenu();
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                intent.putExtra("TYPE", REQUEST_GALLERY);
                startActivityForResult(intent, REQUEST_GALLERY);
            }
        }, R.drawable.sketch_gallery);

        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMenu();
                Bitmap bitmap = sketchView.getBitmapImage();
                try {
                    File file = new File(getExternalFilesDir(null), "image.jpg");
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));

                    Uri uri = Uri.fromFile(file);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/jpg");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(intent, getString(R.string.share)));
                } catch (Exception e) {
                }
            }
        }, R.drawable.sketch_share);

        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMenu();
                sketchView.regulateFitting();
            }
        }, R.drawable.sketch_fitting);

        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMenu();
                action = ACTION_SAVEAS;
                saveAs();
            }
        }, R.drawable.sketch_saveas);

        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMenu();
            }
        }, R.drawable.sketch_setting);

        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeMenu();
            }
        }, R.drawable.sketch_help);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Make database
        sharedPreferences = getSharedPreferences(getResources().getString(R.string.sharedPreferences_name), MODE_PRIVATE);
        int version = sharedPreferences.getInt(getResources().getString(R.string.sharedPreferences_version), 0);
        if (version != getResources().getInteger(R.integer.version)) {
            try {
                FileOutputStream fos = getApplicationContext().openFileOutput(getString(R.string.fileName_saveFileNames), MODE_APPEND);
                fos.close();
            } catch (IOException ioe) {
            }

            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage(getString(R.string.makingDatabase));
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();

            databaseThread.start();
        } else {
            letterAdapter = new LetterAdapter(this);
            letterAdapter.load();
            gridView_letter.setAdapter(letterAdapter);
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
        }
    }

    @Override
    public void onBackPressed() {
        action = ACTION_FINISH;
        if (modified) {
            showWantToSaveDialog();
        } else {
            finish();
        }
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
                for (int j = 0; j < names.length; j++) {
                    if (name.equals(names[j])) {
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
        textView_b1.setTypeface(typeface);
        textView_b2.setTypeface(typeface);
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
        textView_b1.setTypeface(typeface);
        textView_b2.setTypeface(typeface);
        textView_b3.setTypeface(typeface);
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

        for (int i = 0; i < textViews.length; i++)
            textViews[i].setTypeface(typeface);

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

