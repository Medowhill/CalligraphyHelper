package com.hwajung.ksa.calligraphyhelper.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    SketchView sketchView;
    Button button_menu, button_undo, button_redo, button_cancel, button_add;
    MultiButton multiButton_menu;
    GridView gridView_letter;
    ImageView imageView_letterPreview;
    ScrollView scrollView_menu;
    LinearLayout linearLayout_letter;
    TextView textView_fileName;

    Animation animation_menu_appear, animation_menu_disappear;

    int menuAnimation = STATE_MENU_INVISIBLE;
    int selectedLetter = -1;

    String fileName = "NoName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sketch);

        Log.i("TEST", "go");

        sketchView = (SketchView) findViewById(R.id.sketchView);
        button_menu = (Button) findViewById(R.id.button_menu);
        button_redo = (Button) findViewById(R.id.button_redo);
        button_undo = (Button) findViewById(R.id.button_undo);
        button_cancel = (Button) findViewById(R.id.button_cancel);
        button_add = (Button) findViewById(R.id.button_add);
        multiButton_menu = (MultiButton) findViewById(R.id.multiButton_menu);
        gridView_letter = (GridView) findViewById(R.id.gridView_letter);
        imageView_letterPreview = (ImageView) findViewById(R.id.imageView_letterPreview);
        scrollView_menu = (ScrollView) findViewById(R.id.scrollView_menu);
        linearLayout_letter = (LinearLayout) findViewById(R.id.linearLayout_letter);
        textView_fileName = (TextView) findViewById(R.id.textView_fileName);

        sketchView.setSketchActivity(this);

        final LetterAdapter letterAdapter = new LetterAdapter(this);
        gridView_letter.setAdapter(letterAdapter);

        gridView_letter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                imageView_letterPreview.setVisibility(View.VISIBLE);
                imageView_letterPreview.setImageResource((int) letterAdapter.getItem(i));
                selectedLetter = i;
                button_add.setEnabled(true);
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
                gridView_letter.setVisibility(View.INVISIBLE);
                imageView_letterPreview.setVisibility(View.INVISIBLE);
                linearLayout_letter.setVisibility(View.INVISIBLE);
            }
        });

        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedLetter != -1)
                    sketchView.addLetter(selectedLetter);
                linearLayout_letter.setVisibility(View.INVISIBLE);
                gridView_letter.setVisibility(View.INVISIBLE);
                imageView_letterPreview.setVisibility(View.INVISIBLE);
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
                        Toast.makeText(getApplicationContext(), "File does not exist", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Dialog를 생성한다.
                    AlertDialog.Builder adb = new AlertDialog.Builder(SketchActivity.this);
                    adb.setTitle("Open File");

                    adb.setItems(split, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String openFileName = split[whichButton];

                            try {
                                FileInputStream fis = getApplicationContext().openFileInput(openFileName);
                                byte[] data = new byte[fis.available()];
                                fis.read(data);
                                fis.close();

                                sketchView.setDataByByteArray(data);
                            } catch (FileNotFoundException fnf) {
                                Toast.makeText(getApplicationContext(), "File does not exist", Toast.LENGTH_SHORT).show();
                            } catch (IOException ioe) {

                            }

                        }
                    });

                    adb.setNegativeButton("cancel", null);
                    adb.show();

                } catch (FileNotFoundException fnfException) {
                    try {
                        FileOutputStream fos = getApplicationContext().openFileOutput("saveFiles", MODE_PRIVATE);
                        fos.close();
                    } catch (IOException ioe) {
                    }
                } catch (IOException ioException) {
                    Toast.makeText(getApplicationContext(), "Fail to load files", Toast.LENGTH_SHORT).show();
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
                gridView_letter.setVisibility(View.VISIBLE);
                linearLayout_letter.setVisibility(View.VISIBLE);
                button_add.setEnabled(false);
            }
        }, R.drawable.sketch_newletter);

        imageView_letterPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

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
        gridView_letter.setVisibility(View.INVISIBLE);
        linearLayout_letter.setVisibility(View.INVISIBLE);
    }

}
