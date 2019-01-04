package com.mossman.darren.elves_v_goblins;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

public class GameView extends SurfaceView {

    private AnimationLoopThread gameLoopThread;
    private Bitmap bmpBlood;
    private Bitmap bmpBackground;

    private char[][] map;
    private List<Unit> units;

    public void stop() {
        gameLoopThread.setPaused(false);
        gameLoopThread.setRunning(false);
    }

    public GameView(Context context, char[][] map, List<Unit> units) {
        super(context);
        this.map = map;
        this.units = units;

        gameLoopThread = new AnimationLoopThread(this);
        getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                gameLoopThread.setPaused(true);
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (gameLoopThread.getPaused()) {
                    gameLoopThread.setPaused(false);
                    gameLoopThread.interrupt();

                } else {
                    gameLoopThread.setRunning(true);
                    gameLoopThread.start();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

        });
        bmpBlood = BitmapFactory.decodeResource(getResources(), R.drawable.blood);
        bmpBackground = BitmapFactory.decodeResource(getResources(), R.drawable.background);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas == null) return;
        drawMap(canvas);
        drawUnits(canvas);
    }

    private void drawMap(Canvas canvas) {
        if (map == null) return;

        int hgt = map.length;
        int wid = map[0].length;
        int sx = getWidth() / wid;
        int sy = getHeight() / hgt;

        canvas.drawBitmap(bmpBackground, 0, 0, null);

        for (int y = 0; y < map.length; y++) {
            int l = map[y].length;
            for (int x = 0; x < l; x++) {
                int xx = x*sx, yy = y*sy;
                char c = map[y][x];
                if (c != '#') {
                    canvas.drawRect(xx, yy, xx+sx, yy+sy, new Paint(Color.BLACK));
                } else {
                    if (x > 0 && map[y][x-1] != '#') {
                        canvas.drawRect(xx, yy, xx+sx/4, yy+sy, new Paint(Color.BLACK));
                    }
                    if (x < l-1 && map[y][x+1] != '#') {
                        canvas.drawRect(xx+sx*3/4, yy, xx+sx, yy+sy, new Paint(Color.BLACK));
                    }
                    if (y > 0 && map[y-1][x] != '#') {
                        canvas.drawRect(xx, yy, xx+sx, yy+sy/4, new Paint(Color.BLACK));
                    }
                    if (y < map.length-1 && map[y+1][x] != '#') {
                        canvas.drawRect(xx, yy+sy*3/4, xx+sx, yy+sy, new Paint(Color.BLACK));
                    }
                }
            }
        }
    }

    private void drawUnits(Canvas canvas) {
        if (map == null || units == null) return;

        int hgt = map.length;
        int wid = map[0].length;
        int sx = getWidth() / wid;
        int sy = getHeight() / hgt;

        synchronized (units) {
            // draw blood under units
            for (Unit unit : units) {
                unit.drawBlood(canvas, sx, sy, bmpBlood);
            }
            for (Unit unit : units) {
                unit.draw(canvas, sx, sy);
            }
        }
    }
}
