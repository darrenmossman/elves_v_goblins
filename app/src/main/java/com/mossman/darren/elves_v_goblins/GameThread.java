package com.mossman.darren.elves_v_goblins;

import android.content.Context;
import android.media.MediaPlayer;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class GameThread extends Thread {

    private Context context;
    private List<Unit> units;

    public GameThread(Context context, List<Unit> units) {
        this.context = context;
        this.units = units;
    }

    private void playGame() {

        MediaPlayer[] mp = new MediaPlayer[5];
        mp[0] = MediaPlayer.create(context, R.raw.choking);
        mp[1] = MediaPlayer.create(context, R.raw.shield_sword);
        mp[2] = MediaPlayer.create(context, R.raw.straining_grunt);
        mp[3] = MediaPlayer.create(context, R.raw.straining_grunt_2);
        mp[4] = MediaPlayer.create(context, R.raw.growl);

        MediaPlayer mpw = MediaPlayer.create(context, R.raw.walking);
        MediaPlayer mpf = MediaPlayer.create(context, R.raw.gong);
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

    }

    private final Set<ThreadCompleteListener> listeners = new CopyOnWriteArraySet<>();

    public final void addListener(final ThreadCompleteListener listener) {
        listeners.add(listener);
    }
    public final void removeListener(final ThreadCompleteListener listener) {
        listeners.remove(listener);
    }
    private final void notifyListeners() {
        for (ThreadCompleteListener listener : listeners) {
            listener.notifyOfThreadComplete(this);
        }
    }

    @Override
    public void run() {
        playGame();
        notifyListeners();
    }

}
