package com.mossman.darren.elves_v_goblins;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Window;

import java.util.ArrayList;
import java.util.List;

public class Game extends Activity implements ThreadCompleteListener {

    private char[][] map;
    private List<Unit> units;

    private GameThread gameThread;
    private AnimationThread animationThread;
    private GameView gameView;

    private void initGame() {

        Unit.elfAttackPower = (int)(Math.random() * 10 + 3);

        ArrayList<String> input = Utils.readFile(this, "Y2K18_15.txt");

        map = new char[input.size()][];
        for (int i = 0; i < input.size(); i++) {
            map[i] = input.get(i).toCharArray();
        }
        units = new ArrayList<>();

        int b = 0; int g = 0;
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                char c = map[y][x];
                int id = 0;
                if (c == 'E') {
                    switch (g+1) {
                        case 1: id = R.drawable.good1; break;
                        case 2: id = R.drawable.good2; break;
                        case 3: id = R.drawable.good3; break;
                        case 4: id = R.drawable.good4; break;
                        case 5: id = R.drawable.good5; break;
                        case 6: id = R.drawable.good6; break;
                    }
                    g = (g+1) % 6;
                }
                else if (c == 'G') {
                    switch (b+1) {
                        case 1: id = R.drawable.bad1; break;
                        case 2: id = R.drawable.bad2; break;
                        case 3: id = R.drawable.bad3; break;
                        case 4: id = R.drawable.bad4; break;
                        case 5: id = R.drawable.bad5; break;
                        case 6: id = R.drawable.bad6; break;
                    }
                    b = (b+1) % 6;
                }
                if (id != 0) {
                    Bitmap bmp = BitmapFactory.decodeResource(getResources(), id);
                    units.add(new Unit(bmp, map, c, x, y));
                }
            }
        }
    }

    public void notifyOfThreadComplete(final Thread thread) {
        if (thread.equals(gameThread)) {
            gameView.stop();
            finish();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initGame();
        gameThread = new GameThread(this, units);
        animationThread = new AnimationThread();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        gameView = new GameView(this, gameThread, animationThread, map, units);
        setContentView(gameView);
    }
}
