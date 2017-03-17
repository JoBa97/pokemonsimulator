package me.joba.pokemonbattle;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import static me.joba.pokemonbattle.Main.PIXEL_SIZE;

/**
 *
 * @author jonas
 */
public class PokemonBattle {

    private final DrawThread drawThread;
    private final Pokemon[][] grid;
    private final int maxX, maxY;
    public static final byte[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private GifThread gifThread;

    public PokemonBattle(JPanel panel, Pokemon[][] pokemons, File output) {
        this.maxX = pokemons.length;
        this.maxY = pokemons[0].length;
        this.grid = pokemons;
        if (output != null) {
            gifThread = new GifThread(output);
        }
        drawThread = new DrawThread(maxX, maxY, PIXEL_SIZE, panel, gifThread);
    }

    public void init() {
        if (gifThread != null) {
            gifThread.start();
        }
        drawThread.start();
    }

    public boolean isDone() {
        return false;
        //return pokemons.parallelStream().map(p -> p.getType()).distinct().count() <= 1;
    }

    public void saveGif() throws InterruptedException {
        if(gifThread != null) {
            gifThread.shutdown();
            gifThread.join();
        }
    }

    private void executeRunnable(Runnable run) {
        int processors = Runtime.getRuntime().availableProcessors();
        Thread[] threads = new Thread[processors];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(run);
            threads[i].start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(PokemonBattle.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void simulatePass(boolean first) {
        Runnable run = new Runnable() {
            private final Lock lock = new ReentrantLock();
            private int x, y;

            @Override
            public void run() {
                while (true) {
                    lock.lock();
                    if (x == maxX - 1 && y == maxY - 1) {
                        lock.unlock();
                        break;
                    }
                    int x_ = x, y_ = y;
                    x++;
                    if (x == maxX) {
                        y++;
                        x = 0;
                    }
                    lock.unlock();
                    attackNeighbors(x_, y_, first);
                }
            }
        };
        executeRunnable(run);
    }

    public void simulate() {
        simulatePass(true);
        simulatePass(false);
        for (int x = 0; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {
                Pokemon p = grid[x][y];
                if (p.getHealth() <= 0) {
                    Pokemon d = p.getKiller();
                    d = d.clone();
                    d.reset();
                    grid[x][y] = d;
                }
                else {
                    p.reset();
                }
            }
        }
    }

    private void attackNeighbors(int x, int y, boolean firstPass) {
        int nx, ny;
        Pokemon pokemon = grid[x][y];
        for (byte[] direction : directions) {
            nx = (direction[0] + x + maxX) % maxX;
            ny = (direction[1] + y + maxY) % maxY;
            Pokemon target = grid[nx][ny];
            if (target.uuid != pokemon.uuid && pokemon.getHealth() > 0 && target.getHealth() > 0) {
                if (firstPass && pokemon.getSpeed() > target.getSpeed()) {
                    target.simulateAttack(pokemon);
                } else if (!firstPass && pokemon.getSpeed() <= target.getSpeed()) {
                    target.simulateAttack(pokemon);
                }
            }
        }
    }

    public void draw() {
        drawThread.pushImage(grid);
    }

    public void printStats() {
        Map<UUID, StatData> data = new HashMap<>();
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                Pokemon pokemon = grid[x][y];
                StatData d = data.get(pokemon.uuid);
                if (d == null) {
                    d = new StatData(pokemon);
                    data.put(pokemon.uuid, d);
                }
                d.count++;
            }
        }
        for (StatData stat : data.values()) {
            System.out.println(stat.pokemon.uuid);
            System.out.println("  Type: " + stat.pokemon.getType());
            System.out.println("  Attack: " + stat.pokemon.getAttack());
            System.out.println("  Defense: " + stat.pokemon.getDefense());
            System.out.println("  Speed: " + stat.pokemon.getSpeed());
            System.out.println("  Left: " + stat.count);
        }
        System.out.println("Alive: " + data.size());
    }

    private class StatData {

        private Pokemon pokemon;
        private int count;

        public StatData(Pokemon p) {
            pokemon = p;
        }
    }
}
