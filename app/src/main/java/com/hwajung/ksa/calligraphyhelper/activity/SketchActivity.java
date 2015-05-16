package com.hwajung.ksa.calligraphyhelper.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.hwajung.ksa.calligraphyhelper.R;
import com.hwajung.ksa.calligraphyhelper.adapter.LetterAdapter;
import com.hwajung.ksa.calligraphyhelper.view.MultiButton;
import com.hwajung.ksa.calligraphyhelper.view.SketchView;


public class SketchActivity extends Activity {

    final int STATE_MENU_VISIBLE = 0, STATE_MENU_INVISIBLE = 1, STATE_MENU_APPEARING = 2, STATE_MENU_DISAPPEARING = 3;

    SketchView sketchView;
    Button button_menu, button_undo, button_redo, button_cancel, button_add;
    MultiButton multiButton_menu;
    GridView gridView_letter;
    ImageView imageView_letterPreview;
    ScrollView scrollView_menu;
    LinearLayout linearLayout_letter;

    Animation animation_menu_appear, animation_menu_disappear;

    int menuAnimation = STATE_MENU_INVISIBLE;
    int selectedLetter = -1;

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

        multiButton_menu.setDrawableID(new int[]{R.drawable.sketch_menu_load,
                R.drawable.sketch_menu_save,
                R.drawable.sketch_menu_newletter});

        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollView_menu.startAnimation(animation_menu_disappear);
                gridView_letter.setVisibility(View.VISIBLE);
                linearLayout_letter.setVisibility(View.VISIBLE);
                button_add.setEnabled(false);
            }
        }, 2);

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
