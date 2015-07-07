package com.blockhead.bhmusic.activities;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.blockhead.bhmusic.R;

public class ColorChooserActivity extends Activity {

    private RelativeLayout previewAB;
    private ImageView previewFab;
    private Drawable fabDrawable;

    @Override @TargetApi(21)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_chooser);

        Drawable mActionBarDrawable = getResources().getDrawable(R.drawable.ab_background);
        ActionBar mActionBar = getActionBar();

        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (mActionBar != null && mActionBarDrawable != null) {
            mActionBarDrawable.setColorFilter(MainActivity.primaryColor, PorterDuff.Mode.SRC_ATOP);
            mActionBar.setBackgroundDrawable(mActionBarDrawable);

            if(MainActivity.isLollipop())
            {
                //Create and set color for statusbar
                float[] hsv = new float[3];
                int color = MainActivity.primaryColor;
                Color.colorToHSV(color, hsv);
                hsv[2] *= 0.8f; // value component
                color = Color.HSVToColor(hsv);
                getWindow().setStatusBarColor(color);
            }

        }

        previewAB = (RelativeLayout)findViewById(R.id.previewAB);
        previewFab = (ImageView)findViewById(R.id.previewFab);
        if(previewFab != null)
            fabDrawable = previewFab.getBackground();

        updatePreviewColors();


        //Primary Color Change Listener
        View.OnClickListener primaryListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tag = (int)v.getTag();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.remove("primary_color_key");
                editor.putString("primary_color_key", tag+"");
                editor.apply();
                MainActivity.primaryColor = getResources().getColor(MainActivity.getColor(tag));
                updatePreviewColors();
                MainActivity.updateColors(getResources());
            }
        };
        //Secondary Color Change Listener
        View.OnClickListener secondaryListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tag = (int)v.getTag();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.remove("accent_color_key");
                editor.putString("accent_color_key", tag+"");
                editor.apply();
                MainActivity.accentColor = getResources().getColor(MainActivity.getColor(tag));
                updatePreviewColors();
                MainActivity.updateColors(getResources());
            }
        };

        //Set Primary Swatches
        LinearLayout primarySwatches = (LinearLayout)findViewById(R.id.primarySwatches);
        for(int i=0; i < 19; i++) {
            ImageView iv = new ImageView(getApplicationContext());
            Drawable d = getResources().getDrawable(R.drawable.swatch);
            d.setColorFilter(getResources().getColor(MainActivity.getColor(i)), PorterDuff.Mode.SRC_ATOP);
            iv.setBackground(d);
            iv.setTag(i);
            iv.setOnClickListener(primaryListener);
            primarySwatches.addView(iv);
        }
        //Set Secondary Swatches
        LinearLayout secondarySwatches = (LinearLayout)findViewById(R.id.secondarySwatches);
        for(int i=0; i < 19; i++) {
            ImageView iv = new ImageView(getApplicationContext());
            Drawable d = getResources().getDrawable(R.drawable.swatch);
            d.setColorFilter(getResources().getColor(MainActivity.getColor(i)), PorterDuff.Mode.SRC_ATOP);
            iv.setBackground(d);
            iv.setTag(i);
            iv.setOnClickListener(secondaryListener);
            secondarySwatches.addView(iv);
        }

    }

    @Override
    protected void onDestroy() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
        super.onDestroy();
    }

    private boolean updatePreviewColors()
    {
        if((previewAB != null) && (fabDrawable != null))
        {
            previewAB.setBackgroundColor(MainActivity.primaryColor);
            fabDrawable.setColorFilter(MainActivity.accentColor, PorterDuff.Mode.SRC_ATOP);
            return  true;
        }
        else
            return false;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_color_chooser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
