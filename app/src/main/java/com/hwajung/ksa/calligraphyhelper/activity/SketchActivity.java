package com.hwajung.ksa.calligraphyhelper.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hwajung.ksa.calligraphyhelper.R;
import com.hwajung.ksa.calligraphyhelper.adapter.LetterAdapter;
import com.hwajung.ksa.calligraphyhelper.view.MultiButton;
import com.hwajung.ksa.calligraphyhelper.view.SketchView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class SketchActivity extends Activity {

    final int STATE_MENU_VISIBLE = 0, STATE_MENU_INVISIBLE = 1, STATE_MENU_APPEARING = 2, STATE_MENU_DISAPPEARING = 3;
    final int REQUEST_DATABASE = 0;

    SketchView sketchView;
    ImageButton button_menu, button_undo, button_redo, button_cancel;
    MultiButton multiButton_menu;
    GridView gridView_letter;
    ScrollView scrollView_menu;
    TextView textView_fileName;

    Animation animation_menu_appear, animation_menu_disappear, animation_newLetter_appear, animation_newLetter_disappear;

    LetterAdapter letterAdapter;

    SharedPreferences sharedPreferences;

    int menuAnimation = STATE_MENU_INVISIBLE;
    int newLetterAnimation = STATE_MENU_INVISIBLE;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sketch);

        Log.i("TEST", "go");

        sketchView = (SketchView) findViewById(R.id.sketchView);
        button_menu = (ImageButton) findViewById(R.id.button_menu);
        button_redo = (ImageButton) findViewById(R.id.button_redo);
        button_undo = (ImageButton) findViewById(R.id.button_undo);
        button_cancel = (ImageButton) findViewById(R.id.button_cancel);
        multiButton_menu = (MultiButton) findViewById(R.id.multiButton_menu);
        gridView_letter = (GridView) findViewById(R.id.gridView_letter);
        scrollView_menu = (ScrollView) findViewById(R.id.scrollView_menu);
        textView_fileName = (TextView) findViewById(R.id.textView_fileName);

        letterAdapter = new LetterAdapter(this);

        sharedPreferences = getSharedPreferences(getResources().getString(R.string.sharedPreferences_name), MODE_PRIVATE);
        int version = sharedPreferences.getInt(getResources().getString(R.string.sharedPreferences_version), 0);
        if (version != getResources().getInteger(R.integer.version)) {
            Intent intent = new Intent(getApplicationContext(), DatabaseActivity.class);
            startActivityForResult(intent, REQUEST_DATABASE);
        } else {
            letterAdapter.load();
        }

        sketchView.setSketchActivity(this);

        gridView_letter.setAdapter(letterAdapter);

        gridView_letter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != -1)
                    sketchView.addLetter((int) letterAdapter.getItemId(i));
                gridView_letter.startAnimation(animation_newLetter_disappear);
                button_cancel.setVisibility(View.INVISIBLE);
            }
        });

        gridView_letter.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                return false;
            }
        });

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
                gridView_letter.setVisibility(View.VISIBLE);
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
                gridView_letter.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

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

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gridView_letter.startAnimation(animation_newLetter_disappear);
                button_cancel.setVisibility(View.INVISIBLE);
            }
        });

        multiButton_menu.setDrawableID(new int[]{R.drawable.sketch_new,
                R.drawable.sketch_load,
                R.drawable.sketch_save,
                R.drawable.sketch_newletter,
                R.drawable.sketch_draw});

        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            // Click load button

            @Override
            public void onClick(View view) {

                closeMenu();

                // Load existing file name list
                final String fileNames = readFileNames();

                // If file does not exist, does not show dialog
                if (fileNames.length() == 0) {
                    Toast.makeText(getApplicationContext(), R.string.fileNotExist, Toast.LENGTH_SHORT).show();
                    return;
                }

                final String[] split = fileNames.split("\t");

                // Make dialog
                AlertDialog.Builder adb = new AlertDialog.Builder(SketchActivity.this);
                adb.setTitle(R.string.openFile);

                adb.setItems(split, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String fileName = split[whichButton];
                        if (loadFile(fileName))
                            Toast.makeText(getApplicationContext(), R.string.fileLoadingSuccess, Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getApplicationContext(), R.string.fileLoadingFail, Toast.LENGTH_SHORT).show();
                    }
                });

                adb.setNegativeButton(R.string.cancel, null);
                adb.show();
            }
        }, R.drawable.sketch_load);

        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            // Click save button

            @Override
            public void onClick(View view) {

                closeMenu();

                // Load existing file name list
                final String fileNames = readFileNames();
                final String[] fileNameList = fileNames.split("\t");

                // Make dialog
                AlertDialog.Builder adb = fileSavingDialog(fileNameList);
                adb.show();
            }
        }, R.drawable.sketch_save);

        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollView_menu.startAnimation(animation_menu_disappear);
                gridView_letter.startAnimation(animation_newLetter_appear);
                //button_cancel.setVisibility(View.VISIBLE);
            }
        }, R.drawable.sketch_newletter);

    }

    private boolean loadFile(String fileName) {
        try {
            FileInputStream fis = getApplicationContext().openFileInput(fileName);
            byte[] data = new byte[fis.available()];
            fis.read(data);
            fis.close();

            sketchView.setDataByByteArray(data);
        } catch (IOException ioe) {
            return false;
        }
        return true;
    }

    private AlertDialog.Builder fileSavingDialog(String[] fileNameList_) {

        AlertDialog.Builder adb = new AlertDialog.Builder(SketchActivity.this);
        adb.setTitle(R.string.saveFile);
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_save, null);
        adb.setView(dialogView);
        final String[] fileNameList = fileNameList_;

        adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // Get EditText
                final EditText editText = (EditText) dialogView.findViewById(R.id.editText_save);

                // Get file name
                final String fileName = editText.getText().toString();

                // If file name is empty, get input again
                if (fileName.length() == 0) {
                    Toast.makeText(getApplicationContext(), R.string.fileNameEmpty, Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder adb1 = fileSavingDialog(fileNameList);
                    adb1.show();
                    return;
                }

                // If file name already exist, ask whether override or not
                for (int j = 0; j < fileNameList.length; j++) {
                    if (fileNameList[j].equals(fileName)) {
                        AlertDialog.Builder adb1 = new AlertDialog.Builder(SketchActivity.this);
                        adb1.setMessage(R.string.fileOverwrite);
                        adb1.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (saveFile(fileName, false))
                                    Toast.makeText(getApplicationContext(), R.string.fileSavingSuccess, Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(getApplicationContext(), R.string.fileSavingFail, Toast.LENGTH_SHORT).show();
                            }
                        });
                        adb1.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                AlertDialog.Builder adb2 = fileSavingDialog(fileNameList);
                                adb2.show();
                            }
                        });
                        adb1.show();
                        return;
                    }
                }

                if (saveFile(fileName, true))
                    Toast.makeText(getApplicationContext(), R.string.fileSavingSuccess, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), R.string.fileSavingFail, Toast.LENGTH_SHORT).show();
            }
        });

        adb.setNegativeButton(R.string.cancel, null);
        return adb;
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
                return false;
            }
        }
        return true;
    }

    private String readFileNames() {
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
        }
        return fileNames;
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
            gridView_letter.startAnimation(animation_newLetter_disappear);
        button_cancel.setVisibility(View.INVISIBLE);
    }

    public void showLetterEditDialog(int initialColor, float initialSize, float initialDegree) {
        AlertDialog.Builder adb = new AlertDialog.Builder(SketchActivity.this);
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit, null);
        adb.setView(dialogView);

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_DATABASE:
                if (resultCode == RESULT_CANCELED) {
                    finish();
                } else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(getResources().getString(R.string.sharedPreferences_version), getResources().getInteger(R.integer.version));
                    editor.apply();
                    letterAdapter.load();
                }
                break;
        }
    }
}
