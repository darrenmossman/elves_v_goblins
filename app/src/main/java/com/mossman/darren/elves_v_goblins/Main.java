package com.mossman.darren.elves_v_goblins;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main extends Activity {


    private char[][] map;
    private List<Unit> units;
    private GameView gameView;

    private void playGame() {

        MediaPlayer[] mp = new MediaPlayer[5];
        mp[0] = MediaPlayer.create(this, R.raw.choking);
        mp[1] = MediaPlayer.create(this, R.raw.shield_sword);
        mp[2] = MediaPlayer.create(this, R.raw.straining_grunt);
        mp[3] = MediaPlayer.create(this, R.raw.straining_grunt_2);
        mp[4] = MediaPlayer.create(this, R.raw.growl);

        MediaPlayer mpw = MediaPlayer.create(this, R.raw.walking);
        MediaPlayer mpf = MediaPlayer.create(this, R.raw.gong);
        mpw.start();

        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}

            boolean noTargets = false;
            for (Unit unit : units) {
                if (unit.isDead) {
                    continue;
                }
                unit.shortestPath = null;
                unit.attackTarget = null;
                List<Unit> targets = unit.getTargets(units);

                if (targets.size() == 0) {
                    noTargets = true;
                    unit.dir = Unit.Direction.none;
                    break;
                }
                for (Unit target : targets) {
                    if (unit.inRangeOf(target)) {
                        if (unit.attackTarget == null || target.hitPoints < unit.attackTarget.hitPoints) {
                            unit.attackTarget = target;
                        }
                    } else if (unit.attackTarget == null) {
                        Node node = unit.getShortestPath(target);
                        if (node != null) {
                            if (unit.shortestPath == null || node.dist < unit.shortestPath.dist) {
                                unit.shortestPath = node;
                            } else if (node.dist == unit.shortestPath.dist) {
                                if (node.y < unit.shortestPath.y ||
                                        node.y == unit.shortestPath.y && node.x < unit.shortestPath.x) {
                                    unit.shortestPath = node;
                                }
                            }
                        }
                    }
                }
                if (unit.attackTarget != null) {
                    unit.attack(unit.attackTarget);
                } else if (unit.shortestPath != null) {
                    unit.moveTowards(unit.shortestPath);
                    for (Unit target : targets) {
                        if (unit.inRangeOf(target)) {
                            if (unit.attackTarget == null || target.hitPoints < unit.attackTarget.hitPoints) {
                                unit.attackTarget = target;
                            }
                        }
                    }
                    if (unit.attackTarget != null) {
                        unit.attack(unit.attackTarget);
                    }
                } else {
                    unit.dir = Unit.Direction.none;
                }
                if (unit.attackTarget != null && unit.arrived()) {
                    int m = (int)(Math.random() * mp.length);
                    if (!mp[m].isPlaying()) mp[m].start();
                }
            }
            if (noTargets) {
                synchronized (units) {
                    for (Unit unit : units) {
                        unit.dir = Unit.Direction.none;
                    }
                }
                break;
            } else {
                if (!mpw.isPlaying()) {
                    mpw.start();
                }
            }
            synchronized (units) {
                Collections.sort(units);
            }
        }
        mpw.stop();
        mpf.start();
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {}

        gameView.stop();
        finish();
    }


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

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initGame();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        gameView = new GameView(this, map, units);
        setContentView(gameView);

        new Handler().postDelayed(
            new Runnable() {
               @Override
               public void run() {
                   playGame();
               }
           }, 1000);
    }
}
