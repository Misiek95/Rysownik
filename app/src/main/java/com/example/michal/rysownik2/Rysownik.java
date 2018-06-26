package com.example.michal.rysownik2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

/**
 * Created by Michal on 12.05.2018.
 */

public class Rysownik  extends SurfaceView implements SurfaceHolder.Callback,
            Runnable {
    // pola klasy
    //bitmapa główna
    private Bitmap mBitmapa;
    //bitmapa tymczasowa
    private Bitmap mBitmapa2;
    //kanwa
    private Canvas mKanwa;
    // pozwala kontrolować i monitorować powierzchnię
    private SurfaceHolder mPojemnik;
    // wątek, który odświeża kanwę
    private Thread mWatekRysujacy;
    // flaga logiczna do kontrolowania pracy watku
    private boolean mWatekPracuje = false;
    // obiekt do tworzenia sekcji krytycznych
    private Object mBlokada=new Object();
    //Farba określająca właściwości rysowania
    private Paint mFarba= new Paint();
    //współrzędne do rysowania
    private float X_move = -1;
    private float Y_move = -1;
    private float Xstart = -1;
    private float Ystart = -1;
    private float XmoveOld = -1;
    private float YmoveOld = -1;
    private float r;

    public Rysownik(Context context, AttributeSet attrs) {
        super(context, attrs);
// Pojemnik powierzchni - pozwala kontrolować i monitorować powierzchnię
        mPojemnik = getHolder();
        mPojemnik.addCallback(this);
//inicjalizacja innych elementów...
    }

    public void wznowRysowanie() {
// uruchomienie wątku rysującego
        mWatekPracuje = true;
        mWatekRysujacy.start();
    }

    public void pauzujRysowanie() {
            mWatekPracuje = false;
        }

//obsługa dotknięcia ekranu
    @Override
    public boolean onTouchEvent(MotionEvent event) {
            performClick();
//sekcja krytyczna – modyfikacja rysunku na wyłączność
            synchronized (mBlokada) {
//modyfikacja rysunku...
                switch (event.getAction()) {
                //Dotknięcie ekranu
                    case MotionEvent.ACTION_DOWN:

                        Xstart = event.getX();
                        Ystart = event.getY();
                        mFarba.setColor(MainActivity.color); //Ustawienie koloru
                        mFarba.setStrokeWidth(MainActivity.rozmiar); //Ustawienie szerokości linii
                        mFarba.setStyle(Paint.Style.FILL);
                        mKanwa.drawCircle(Xstart, Ystart, MainActivity.rozmiar+5, mFarba); //Rysowanie kółka na końcu i początku linii
                        XmoveOld = Xstart;
                        YmoveOld = Ystart;
                        return true;
                    //puszczenie ekranu
                    case MotionEvent.ACTION_UP:
                        if(MainActivity.circle==true) {
                            if(MainActivity.wypelnienie==false)
                                mFarba.setStyle(Paint.Style.STROKE);
                            else
                                mFarba.setStyle(Paint.Style.FILL);
                            r= (float) Math.sqrt(Math.pow(Math.abs(X_move-Xstart),2.0)+Math.pow(Math.abs(Y_move-Ystart),2.0))/2;
                            mKanwa.drawCircle((Xstart+X_move)/2, (Ystart+Y_move)/2, r, mFarba);
                        }
                        mFarba.setStyle(Paint.Style.FILL);
                        mKanwa.drawCircle(event.getX(),event.getY(), MainActivity.rozmiar+5, mFarba);
                        return true;
                    //przesówanie po ekranie
                    case MotionEvent.ACTION_MOVE:
                        X_move = event.getX();
                        Y_move = event.getY();
                        //jeśli wybrano rysowanie linii
                        if(MainActivity.line==true)
                        mKanwa.drawLine(XmoveOld, YmoveOld, X_move, Y_move, mFarba);
                        XmoveOld = X_move;
                        YmoveOld = Y_move;
                        return true;
                    default:
                        break;
                }
            }
            return true;
        }

    //żeby lint nie wyświetlał ostrzeżeń - onTouchEvent i performClick trzeba
//implementować razem
    public boolean performClick()
    {
        return super.performClick();
    }

    @Override
    public void run() {

        while (mWatekPracuje) {
            Canvas kanwa = null;
            try {
// sekcja krytyczna - żaden inny wątek nie może używać pojemnika
                synchronized (mPojemnik) {
// czy powierzchnia jest prawidłowa
                    if (!mPojemnik.getSurface().isValid()) continue;
// zwraca kanwę, na której można rysować, każdy piksel
// kanwy w prostokącie przekazanym jako parametr musi być
// narysowany od nowa inaczej: rozpoczęcie edycji
// zawartości kanwy
                    kanwa = mPojemnik.lockCanvas(null);
//sekcja krytyczna – dostęp do rysunku na wyłączność
                    synchronized (mBlokada) {
                        if (mWatekPracuje) {
//rysowanie na lokalnej kanwie...
                            if(MainActivity.clear == true) //Instrukcja wykona się gdy przycisk o ID clear jest wciśnięty
                            {
                                mKanwa.drawARGB(255, 255, 255, 255); //Wyczyszczenie ekranu (zamalowanie kanwy na biało)
                                MainActivity.clear = false;
                            }
                            kanwa.drawBitmap(mBitmapa, 0, 0, null);
                        }
                    }
                }
            } finally {
// w bloku finally - gdyby wystąpił wyjątek w powyższym
// powierzchnia zostanie zostawiona w spójnym stanie
                if (kanwa != null) {
// koniec edycji kanwy i wyświetlenie rysunku na ekranie
                    mPojemnik.unlockCanvasAndPost(kanwa);
                }
            }
            try {
                Thread.sleep(1000 / 50); // 25
                }
                catch (InterruptedException e) {
                }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
// inicjalizacja...
        // tworzenie bitmapy i związanej z nią kanwy
        Rect surfaceSize = holder.getSurfaceFrame();

        mBitmapa = Bitmap.createBitmap(surfaceSize.width(), surfaceSize.height(), Bitmap.Config.RGB_565);
        mBitmapa.eraseColor(Color.WHITE);

        mKanwa = new Canvas(mBitmapa);
        mKanwa.setBitmap(mBitmapa);

        //sprawdzenie czy pobrano bitmapę z pamięci Cache
        if (mBitmapa2 != null) {
            mKanwa.drawBitmap(mBitmapa2, 0, 0, null);
            mBitmapa2.recycle();
        }

        mFarba = new Paint();
        mFarba.setColor(Color.BLACK);
        mFarba.setStyle(Paint.Style.STROKE);
        mFarba.setStrokeWidth(MainActivity.rozmiar);

        mWatekRysujacy = new Thread(this);
        wznowRysowanie();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) { }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
// zatrzymanie rysowania
        pauzujRysowanie();
        mBitmapa2=null;
        }

    public void setBitmap(Bitmap bitmap) {
        mBitmapa2 = bitmap;
    }

    public Bitmap getBitmap() {
        return mBitmapa;
    }

}
