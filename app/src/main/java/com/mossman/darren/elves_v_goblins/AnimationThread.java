package com.mossman.darren.elves_v_goblins;

import android.annotation.SuppressLint;
import android.graphics.Canvas;

public class AnimationThread extends Thread {
    static final long FPS = 10;
    private static final long ticksPS = 1000 / FPS;

    private GameView view;
    private boolean running = false;
    private boolean paused = false;

    public AnimationThread() {
    }

    public void setView(GameView view) {
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
        while (running) {
            while (paused) {
                try {
                    sleep(100);
                } catch (InterruptedException ie) {
                }
            }
            Canvas canvas = null;
            long startTime = System.currentTimeMillis();
            try {
                canvas = view.getHolder().lockCanvas();
                synchronized (view.getHolder()) {
                    drawView(canvas);
                }
            }
            finally {
                if (canvas != null) {
                    view.getHolder().unlockCanvasAndPost(canvas);
                }
            }
            long sleepTime = ticksPS - (System.currentTimeMillis() - startTime);
            try {
                if (sleepTime > 0) sleep(sleepTime);
                else sleep(10);
            } catch (InterruptedException e) {}
        }
    }
}
