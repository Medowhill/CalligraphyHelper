package com.hwajung.ksa.calligraphyhelper.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ViewFlipper;

import com.hwajung.ksa.calligraphyhelper.R;

/**
 * (C) 2015. Jaemin Hong all rights reserved.
 */
public class HelpActivity extends Activity {

    ViewFlipper viewFlipper;

    int x = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);

        viewFlipper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                x++;
                if (x >= 6)
                    finish();
                viewFlipper.showNext();
            }
        });

    }
}
