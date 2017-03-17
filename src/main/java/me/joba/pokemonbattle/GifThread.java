/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.joba.pokemonbattle;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

/**
 *
 * @author jonas
 */
public class GifThread extends Thread {

    private final BlockingQueue<BufferedImage> writeQueue;
    private boolean shutdown = false;
    private GifSequenceWriter gifWriter;
    private ImageOutputStream stream;

    public GifThread(File output) {
        this.writeQueue = new LinkedBlockingQueue<>(100);
        try {
            output.createNewFile();
            stream = new FileImageOutputStream(output);
            gifWriter = new GifSequenceWriter(stream, BufferedImage.TYPE_INT_RGB, 0, false);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void pushImage(BufferedImage image) {
        if (!shutdown) {
            writeQueue.offer(image);
        }
    }

    @Override
    public void run() {
        while (!shutdown || !writeQueue.isEmpty()) {
            if(shutdown) {
                printStatus();
            }
            try {
                gifWriter.writeToSequence(writeQueue.take());
            } catch (InterruptedException | IOException ex) {}
        }
    }
    
    private void printStatus() {
        if(writeQueue.size() % 10 == 0) {
            System.out.println("Images left: " + writeQueue.size());
        }
    }

    public void shutdown() {
        if(writeQueue.isEmpty()) {
            this.interrupt();
        }
        shutdown = true;
    }
}
