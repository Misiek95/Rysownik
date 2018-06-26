package com.example.michal.rysownik2;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    public static int rozmiar = 1;
    public static int color = Color.RED; //Ustawienie początkowego koloru na czerwony
    public static boolean clear = false; //Flaga wskazująca na to czy został wciśnięty przycisk o ID clear
    public static boolean line = true;
    public static boolean circle = false;
    public static boolean wypelnienie = false;

    private static final String TAG = "MainActivity";
    private static final String EXTRA_BITMAP_URI = "bitmap_uri";
    private static final String CACHED_BITMAP = "cached_bitmap.png";

    private Rysownik mPaintCanvas;

    //zmeinne do animacji
    public Animation migniecie;
    public Animation odjazd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPaintCanvas = findViewById(R.id.powierzchnia_rysunku);
        if (savedInstanceState != null) {
            wczytajZCache(savedInstanceState);
        }

        odjazd = AnimationUtils.loadAnimation(this, R.anim.odjazd_animacja);
        migniecie = AnimationUtils.loadAnimation(this, R.anim.migniecie_animacja);
        //domyślne ustawienei rysowania linii
        Button button = (Button) findViewById(R.id.line_button);
        button.setEnabled(false);
//inicjalizacja paska przesówanego
        SeekBar seekBar = (SeekBar) findViewById(R.id.rozmiar_linii);

//Obsługa zmiany wartości paska przesówanego
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                rozmiar = progress;
            }
        });
    }

    //ustawianie kolorów rysowania
    public void redbutton(View view) {
        view.startAnimation(migniecie);
        color = Color.RED;
    }

    public void yellowbutton(View view) {
        view.startAnimation(migniecie);
        color = Color.YELLOW;
    }

    public void bluebutton(View view) {
        view.startAnimation(migniecie);
        color = Color.BLUE;
    }

    public void greenbutton(View view) {
        view.startAnimation(migniecie);
        color = Color.GREEN;
    }

    public void whitebutton(View view) {
        view.startAnimation(migniecie);
        color = Color.WHITE;
    }

    public void clearbutton(View view) {
        view.startAnimation(odjazd);
        clear = true;
    }

    //Włączenie rysowania okręgów/kółek
    public void circlebutton(View view) {
        view.startAnimation(odjazd);
        circle = true;
        line = false;
        Button button = (Button) findViewById(R.id.circle_button);
        button.setEnabled(false);
        Button button2 = (Button) findViewById(R.id.line_button);
        button2.setEnabled(true);
    }

    //włączenie rysowania linii
    public void linebutton(View view) {
        view.startAnimation(odjazd);
        circle = false;
        line = true;
        Button button = (Button) findViewById(R.id.circle_button);
        button.setEnabled(true);
        Button button2 = (Button) findViewById(R.id.line_button);
        button2.setEnabled(false);
    }


    //Zmiana sposobu rysowania figur wypełnienie/brak wypełnienia
    public void wypelnienieCheck(View view) {
        wypelnienie = ((CheckBox) view).isChecked();
    }


    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        zapiszDoCache(outState);
    }

//funkcja wczytująca bitmape z pamieci Cache
    private void wczytajZCache(Bundle savedInstanceState) {
        String savedUri = savedInstanceState.getString(EXTRA_BITMAP_URI);
        if (savedUri != null) {
            try {
                URI uri = new URI(savedUri);
                Bitmap bitmap = BitmapUtil.loadBitmap(uri);
                mPaintCanvas.setBitmap(bitmap);
            } catch (URISyntaxException e) {
                Log.d(TAG, "wczytajZCache: Bitmap URI not valid " + e);
            } catch (IOException e) {
                Log.d(TAG, "wczytajZCache: IOException white loading bitmap " + e);
            }
        }
    }
//Funkcja zapisująca bitmape do pamięci Cache
    private void zapiszDoCache(Bundle outState) {
        Bitmap bitmap = mPaintCanvas.getBitmap();
        try {
            URI uri = BitmapUtil.saveBitmap(this, bitmap, CACHED_BITMAP, Bitmap.CompressFormat.PNG);
            outState.putString(EXTRA_BITMAP_URI, uri.toString());
        } catch (IOException e) {
            Log.d(TAG, "onSaveInstanceState: failed to save bitmap cache - " + e);
        }
    }
}