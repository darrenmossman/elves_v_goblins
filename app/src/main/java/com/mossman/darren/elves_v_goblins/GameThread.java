package com.mossman.darren.elves_v_goblins;

import android.content.Context;
import android.media.MediaPlayer;

import java.util.Collections;
import java.util.List;

public class GameThread extends NotifyingThread {

    private static final long FPS = 2;
    private static final long ticksPS = 1000 / FPS;

    private Context context;
    private List<Unit> units;
    private boolean paused;

    public GameThread(Context context, List<Unit> units) {
        this.context = context;
        this.units = units;
    }

    public void doRun() {

        MediaPlayer[] mp = new MediaPlayer[5];
        mp[0] = MediaPlayer.create(context, R.raw.choking);
        mp[1] = MediaPlayer.create(context, R.raw.shield_sword);
        mp[2] = MediaPlayer.create(context, R.raw.straining_grunt);
        mp[3] = MediaPlayer.create(context, R.raw.straining_grunt_2);
        mp[4] = MediaPlayer.create(context, R.raw.growl);

        MediaPlayer mpw = MediaPlayer.create(context, R.raw.walking);
        MediaPlayer mpg = MediaPlayer.create(context, R.raw.gong);
        try {
            mpw.prepare();
        } catch (Exception e) {
            String s = e.getMessage();
        }
        mpw.start();

        while (true) {
            if (paused && mpw.isPlaying()) mpw.pause();
            while (paused) {
                try {
                    sleep(100);
                } catch (InterruptedException e) {}
            }
            if (!mpw.isPlaying()) {
                mpw.start();
            }
            long startTime = System.currentTimeMillis();

            boolean noTargets = false;
            for (Unit unit : units) {
                if (paused && mpw.isPlaying()) mpw.pause();

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
                    if (paused && mpw.isPlaying()) mpw.pause();
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
                if (!paused && unit.attackTarget != null && unit.arrived()) {
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
            }
            synchronized (units) {
                Collections.sort(units);
            }

            long sleepTime = ticksPS-(System.currentTimeMillis() - startTime);
            try {
                if (sleepTime > 0) sleep(sleepTime);
                else sleep(10);
            } catch (InterruptedException e) {}
        }
        mpw.stop();
        mpg.start();
        try {
            sleep(20000);
        } catch (InterruptedException e) {}

    }

    public void setPaused(boolean pause) {
        paused = pause;
    }
}
