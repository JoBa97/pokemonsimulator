/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.joba.pokemonbattle;

import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.JPanel;

/**
 *
 * @author jonas
 */
public class DrawThread extends Thread {

    private final BlockingQueue<Type[][]> writeQueue;
    private boolean shutdown = false;
    private final JPanel panel;
    private final GifThread gifThread;
    private final int maxX, maxY, pixelSize;

    public DrawThread(int maxX, int maxY, int pixelSize, JPanel panel, GifThread gifThread) {
        this.writeQueue = new LinkedBlockingQueue<>(10);
        this.panel = panel;
        this.maxX = maxX;
        this.maxY = maxY;
        this.pixelSize = pixelSize;
        this.gifThread = gifThread;
    }

    public void pushImage(Pokemon[][] pokemons) {
        if (!shutdown) {
            if (panel == null && gifThread == null) {
                return;
            }
            Type[][] types = new Type[maxX][maxY];
            for (int x = 0; x < pokemons.length; x++) {
                for (int y = 0; y < pokemons[x].length; y++) {
                    types[x][y] = pokemons[x][y].getType();
                }
            }
            writeQueue.offer(types);
        }
    }

    @Override
    public void run() {
        while (!shutdown || !writeQueue.isEmpty()) {
            try {
                Type[][] types = writeQueue.take();
                BufferedImage image = new BufferedImage(maxX * pixelSize, maxY * pixelSize, BufferedImage.TYPE_INT_RGB);
                for (int x = 0; x < maxX; x++) {
                    for (int y = 0; y < maxY; y++) {
                        for (int i = 0; i < pixelSize * pixelSize; i++) {
                            image.setRGB(x * pixelSize + i % pixelSize, y * pixelSize + i / pixelSize, types[x][y].getColor().getRGB());
                        }
                    }
                }
                if(panel != null) {
                    panel.getGraphics().drawImage(image, 0, 0, null);
                }
                if(gifThread != null) {
                    gifThread.pushImage(image);
                }
            } catch (InterruptedException ex) {
                
            }
        }
    }

    public void shutdown() {
        if (writeQueue.isEmpty()) {
            this.interrupt();
        }
        shutdown = true;
    }
}
