/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.joba.pokemonbattle;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.Switch;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author jonas
 */
public class Main {

    public static Random RANDOM;
    public static int PIXEL_SIZE = 10;
    public static int WIDTH = 100, HEIGHT = 100;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        SimpleJSAP jsap = new SimpleJSAP(
                "Pokemon Battle",
                "Simulation of virtual Pokemon battles",
                new Parameter[]{
                    new FlaggedOption("width", JSAP.INTEGER_PARSER, "100", JSAP.REQUIRED, 'w', "width", "Horizontal field count"),
                    new FlaggedOption("height", JSAP.INTEGER_PARSER, "100", JSAP.REQUIRED, 'h', "height", "Vertical field count"),
                    new FlaggedOption("size", JSAP.INTEGER_PARSER, "10", JSAP.REQUIRED, JSAP.NO_SHORTFLAG, "size", "The size of a pokemon in pixels"),
                    new FlaggedOption("attack", JSAP.STRING_PARSER, "50-500", JSAP.REQUIRED, 'a', "attack", "Attack value range"),
                    new FlaggedOption("defense", JSAP.STRING_PARSER, "50-500", JSAP.REQUIRED, 'd', "defense", "Defense value range"),
                    new FlaggedOption("speed", JSAP.STRING_PARSER, "100-400", JSAP.REQUIRED, 's', "speed", "Speed value range"),
                    new FlaggedOption("health", JSAP.STRING_PARSER, "200-600", JSAP.REQUIRED, JSAP.NO_SHORTFLAG, "health", "Health value range"),
                    new FlaggedOption("crit", JSAP.DOUBLE_PARSER, "0.1", JSAP.REQUIRED, 'c', "crit", "Crit chance"),
                    new FlaggedOption("output", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, 'o', "output", "Output target for the created gif"),
                    new Switch("headless", JSAP.NO_SHORTFLAG, "headless", "Output target for the created gif")
                }
        );

        JSAPResult config = jsap.parse(args);
        if (!config.success()) {
            System.err.println("                " + jsap.getUsage());
            System.exit(1);
        }

        RANDOM = new Random();
        int MIN_ATK = Integer.parseInt(config.getString("attack").split("-")[0]);
        int MAX_ATK = Integer.parseInt(config.getString("attack").split("-")[1]);
        int MIN_DEF = Integer.parseInt(config.getString("defense").split("-")[0]);
        int MAX_DEF = Integer.parseInt(config.getString("defense").split("-")[1]);
        int MIN_HEALTH = Integer.parseInt(config.getString("health").split("-")[0]);
        int MAX_HEALTH = Integer.parseInt(config.getString("health").split("-")[1]);
        int MIN_SPEED = Integer.parseInt(config.getString("speed").split("-")[0]);
        int MAX_SPEED = Integer.parseInt(config.getString("speed").split("-")[1]);
        Pokemon.CRITICAL_HIT_CHANCE = config.getDouble("crit");
        HEIGHT = config.getInt("height");
        WIDTH = config.getInt("width");
        PIXEL_SIZE = config.getInt("size");
        File outputFile = null;
        if (config.contains("output")) {
            outputFile = new File(config.getString("output"));
        }
        int width, height;
        JPanel myPanel = null;
        if (!config.getBoolean("headless")) {
            JFrame frame = new JFrame("Pokemon Simulation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            myPanel = new JPanel();
            myPanel.setPreferredSize(new Dimension(WIDTH * PIXEL_SIZE, HEIGHT * PIXEL_SIZE));
            myPanel.setLayout(null);
            frame.add(myPanel);
            frame.setVisible(true);
            frame.pack();
            width = myPanel.getWidth();
            height = myPanel.getHeight();
        } else {
            width = WIDTH * PIXEL_SIZE;
            height = HEIGHT * PIXEL_SIZE;
        }
        Type[] types = Type.values();
        Pokemon[][] pokemons = new Pokemon[width / PIXEL_SIZE][height / PIXEL_SIZE];
        for (int x = 0; x < pokemons.length; x++) {
            for (int y = 0; y < pokemons[x].length; y++) {
                int atk = gaussianInt(MIN_ATK, MAX_ATK);
                int def = gaussianInt(MIN_DEF, MAX_DEF);
                int health = gaussianInt(MIN_HEALTH, MAX_HEALTH);
                int speed = gaussianInt(MIN_SPEED, MAX_SPEED);
                Type type = types[RANDOM.nextInt(types.length)];
                pokemons[x][y] = new Pokemon(type, atk, def, health, speed);
            }
        }
        PokemonBattle battle = new PokemonBattle(myPanel, pokemons, outputFile);
        ReentrantLock lock = new ReentrantLock(true);//Fair lock
        AtomicInteger delay = new AtomicInteger(50);
        new Thread() {
            @Override
            public void run() {
                Scanner scan = new Scanner(System.in);
                String l;
                while (true) {
                    try {

                        while (!(l = scan.nextLine()).equalsIgnoreCase("exit")) {
                            switch (l.split(" ")[0]) {
                                case "pause": {
                                    lock.lock();
                                    break;
                                }
                                case "resume": {
                                    lock.unlock();
                                    break;
                                }
                                case "delay": {
                                    delay.set(Integer.parseInt(l.split(" ")[1]));
                                    break;
                                }
                                case "stats": {
                                    battle.printStats();
                                }
                            }
                        }
                        System.exit(0);
                    } catch (Exception e) {
                    }
                }
            }
        }.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println("Stopping...");
                    battle.saveGif();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        battle.init();
        while (!battle.isDone()) {
            lock.lock();
            battle.draw();
            battle.simulate();
            lock.unlock();
            Thread.sleep(delay.get());
        }
    }

    private static int gaussianInt(int min, int max) {
        int g;
        do {
            g = (int) Math.round(Math.abs(RANDOM.nextGaussian()) * (max - min) + min);
        } while (g > max || g < min);
        return g;
    }
}
