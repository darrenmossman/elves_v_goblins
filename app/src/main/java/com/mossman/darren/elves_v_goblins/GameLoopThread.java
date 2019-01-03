package com.mossman.darren.elves_v_goblins;

import android.annotation.SuppressLint;
import android.graphics.Canvas;

public class GameLoopThread extends Thread {
    static final long FPS = 10;
    private GameView view;
    private boolean running = false;
    private boolean paused = false;

    public GameLoopThread(GameView view) {
        this.view = view;
    }

    public void setRunning(boolean run) {
        running = run;
    }
    public void setPaused(boolean pause) {
        paused = pause;
    }
    public boolean getPaused() {
        return paused;
    }

    @SuppressLint("WrongCall")
    private void drawView(Canvas c) {
        view.onDraw(c);
    }

    @Override
    public void run() {
        long ticksPS = 1000 / FPS;
        long startTime;
        long sleepTime = 0;
        while (running) {
            while (paused) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                }
            }
            Canvas c = null;
            startTime = System.currentTimeMillis();
            try {
                c = view.getHolder().lockCanvas();
                synchronized (view.getHolder()) {
                    drawView(c);
                }
            }
            finally {
                if (c != null) {
                    view.getHolder().unlockCanvasAndPost(c);
                }
            }
            sleepTime = ticksPS-(System.currentTimeMillis() - startTime);

            try {
                if (sleepTime > 0)
                    sleep(sleepTime);
                else
                    sleep(10);
            } catch (Exception e) {}
        }
        if (sleepTime == 0) {
            System.out.println("test");
        }
    }
}
