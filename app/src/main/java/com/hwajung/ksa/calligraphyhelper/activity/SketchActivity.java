package com.hwajung.ksa.calligraphyhelper.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.hwajung.ksa.calligraphyhelper.R;
import com.hwajung.ksa.calligraphyhelper.adapter.LetterAdapter;
import com.hwajung.ksa.calligraphyhelper.view.MultiButton;
import com.hwajung.ksa.calligraphyhelper.view.SketchView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    SharedPreferences sharedPreferences;

    int menuAnimation = STATE_MENU_INVISIBLE;
    int newLetterAnimation = STATE_MENU_INVISIBLE;

    String fileName = "NoName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        sharedPreferences = getSharedPreferences(getResources().getString(R.string.sharedPreferences_name), MODE_PRIVATE);
        int version = sharedPreferences.getInt(getResources().getString(R.string.sharedPreferences_version), 0);
        if (version != getResources().getInteger(R.integer.version)) {
            Intent intent = new Intent(getApplicationContext(), DatabaseActivity.class);
            startActivityForResult(intent, REQUEST_DATABASE);
        }

        sketchView.setSketchActivity(this);

        final LetterAdapter letterAdapter = new LetterAdapter(this);
        gridView_letter.setAdapter(letterAdapter);

        gridView_letter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != -1)
                    sketchView.addLetter(i);
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
            // 불러오기 버튼을 눌렀을 때

            @Override
            public void onClick(View view) {

                closeMenu();

                try {
                    // 기존에 저장한 파일 이름 목록을 불러온다.

                    // Stream을 연다.
                    FileInputStream fis = getApplicationContext().openFileInput("saveFiles");

                    // 파일을 읽어 byte 배열에 기록한다.
                    byte[] data = new byte[fis.available()];
                    fis.read(data);

                    // Stream을 닫는다.
                    fis.close();

                    // String 배열로 변환한다.
                    String files = new String(data);
                    final String[] split = files.split("\t");

                    // 파일이 존재하지 않을 시 dialog를 표시하지 않는다.
                    if (split.length == 0) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.fileNotExist), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Dialog를 생성한다.
                    AlertDialog.Builder adb = new AlertDialog.Builder(SketchActivity.this);
                    adb.setTitle(getResources().getString(R.string.openFile));

                    adb.setItems(split, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String openFileName = split[whichButton];

                            try {
                                FileInputStream fis = getApplicationContext().openFileInput(openFileName);
                                byte[] data = new byte[fis.available()];
                                fis.read(data);
                                fis.close();

                                sketchView.setDataByByteArray(data);
                            } catch (IOException ioe) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.fileLoadingError), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                    adb.setNegativeButton(getResources().getString(R.string.cancel), null);
                    adb.show();

                } catch (FileNotFoundException fnfException) {
                    try {
                        FileOutputStream fos = getApplicationContext().openFileOutput("saveFiles", MODE_PRIVATE);
                        fos.close();
                    } catch (IOException ioe) {
                    }
                } catch (IOException ioException) {
                    Toast.makeText(getApplicationContext(), getString(R.string.fileLoadingError), Toast.LENGTH_SHORT).show();
                }
            }
        }, R.drawable.sketch_load);

        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            // 저장 버튼을 눌렀을 때

            @Override
            public void onClick(View view) {

                closeMenu();

                // 기존에 저장한 파일 이름 목록을 불러온다.
                String[] split = new String[0];
                String files_ = "";
                try {
                    // Stream을 연다.
                    FileInputStream fis = getApplicationContext().openFileInput("saveFiles");

                    // 파일을 읽어 byte 배열에 기록한다.
                    byte[] data = new byte[fis.available()];
                    fis.read(data);

                    // Stream을 닫는다.
                    fis.close();

                    // 파일 이름 목록을 만든다.
                    files_ = new String(data);
                    split = files_.split("\t");

                } catch (FileNotFoundException fnfException) {
                } catch (IOException ioException) {
                    Toast.makeText(getApplicationContext(), "Fail to load files", Toast.LENGTH_SHORT).show();
                    return;
                }
                final String[] fileNames = split;
                final String files = files_;

                // Dialog를 생성한다.
                AlertDialog.Builder adb = new AlertDialog.Builder(SketchActivity.this);
                adb.setTitle("Save File");
                final View dialogView = getLayoutInflater().inflate(R.layout.dialog_save, null);
                adb.setView(dialogView);

                adb.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // EditText를 가져온다.
                        final EditText editText = (EditText) dialogView.findViewById(R.id.editText_save);

                        // 파일 이름을 가져온다.
                        String fileName = editText.getText().toString();

                        // 파일 이름이 없을 시 다시 입력받는다.
                        if (fileName.length() == 0) {
                            Toast.makeText(getApplicationContext(), "No file name.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 이미 존재하는 파일 이름일 시 다시 입력받는다.
                        for (int j = 0; j < fileNames.length; j++) {
                            if (fileNames[j].equals(fileName)) {
                                Toast.makeText(getApplicationContext(), "File already exists.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        // 파일을 저장한다.
                        try {
                            FileOutputStream fos = getApplicationContext().openFileOutput(fileName, MODE_PRIVATE);
                            byte[] data = sketchView.getData();
                            fos.write(data);
                            fos.close();
                        } catch (IOException ioe) {
                            Toast.makeText(getApplicationContext(), "Fail to save files", Toast.LENGTH_SHORT).show();
                        }

                        // 파일 이름 목록에 파일 이름을 추가해 저장한다.
                        try {
                            FileOutputStream fos = getApplicationContext().openFileOutput("saveFiles", MODE_PRIVATE);
                            byte[] data = (files + fileName + "\t").getBytes();
                            fos.write(data);
                            fos.close();
                        } catch (IOException ioe) {
                            Toast.makeText(getApplicationContext(), "Fail to save files", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                adb.setNegativeButton("cancel", null);
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
                    editor.commit();
                }
                break;
        }
    }
}
