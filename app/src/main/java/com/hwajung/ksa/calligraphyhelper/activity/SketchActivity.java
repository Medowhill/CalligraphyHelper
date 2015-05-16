package com.hwajung.ksa.calligraphyhelper.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ScrollView;

import com.hwajung.ksa.calligraphyhelper.R;
import com.hwajung.ksa.calligraphyhelper.adapter.LetterAdapter;
import com.hwajung.ksa.calligraphyhelper.view.MultiButton;
import com.hwajung.ksa.calligraphyhelper.view.SketchView;


public class SketchActivity extends Activity {

    final int STATE_MENU_VISIBLE = 0, STATE_MENU_INVISIBLE = 1, STATE_MENU_APPEARING = 2, STATE_MENU_DISAPPEARING = 3;

    SketchView sketchView;
    Button imageButton_menu, imageButton_undo, imageButton_redo;
    MultiButton multiButton_menu;
    GridView gridView_letter;

    ScrollView scrollView_menu;

    Animation animation_menu_appear, animation_menu_disappear;

    int menuAnimation = STATE_MENU_INVISIBLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sketch);

        Log.i("TEST", "go");

        sketchView = (SketchView) findViewById(R.id.sketchView);
        imageButton_menu = (Button) findViewById(R.id.button_menu);
        imageButton_redo = (Button) findViewById(R.id.button_redo);
        imageButton_undo = (Button) findViewById(R.id.button_undo);
        multiButton_menu = (MultiButton) findViewById(R.id.multiButton_menu);
        gridView_letter = (GridView) findViewById(R.id.gridView_letter);

        scrollView_menu = (ScrollView) findViewById(R.id.scrollView_menu);

        sketchView.setSketchActivity(this);

        LetterAdapter letterAdapter = new LetterAdapter(this);
        gridView_letter.setAdapter(letterAdapter);

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

        imageButton_menu.setOnClickListener(new View.OnClickListener() {
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

        imageButton_redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sketchView.redo();
            }
        });

        imageButton_undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sketchView.undo();
            }
        });

        multiButton_menu.setDrawableID(new int[]{R.drawable.sketch_menu_load,
                R.drawable.sketch_menu_save,
                R.drawable.sketch_menu_newletter});

        multiButton_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gridView_letter.setVisibility(View.VISIBLE);
            }
        }, 2);

    }

    public void setUndoButton(boolean enabled) {
        imageButton_undo.setEnabled(enabled);
    }

    public void setRedoButton(boolean enabled) {
        imageButton_redo.setEnabled(enabled);
    }

    public void closeMenu() {
        if (menuAnimation == STATE_MENU_VISIBLE)
            scrollView_menu.startAnimation(animation_menu_disappear);
        gridView_letter.setVisibility(View.INVISIBLE);
    }

}
