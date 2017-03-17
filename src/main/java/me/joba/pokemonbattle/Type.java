/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.joba.pokemonbattle;

import java.awt.Color;

/**
 *
 * @author jonas
 */
public enum Type {
    NORMAL(0, "909060"),
    FIRE(1, "E06A15"),
    WATER(2, "4B7AED"),
    ELECTRIC(3, "F2C410"),
    GRASS(4, "67B640"),
    ICE(5, "7CCDCD"),
    FIGHTING(6, "A42923"),
    POISON(7, "853585"),
    GROUND(8, "D9B246"),
    FLYING(9, "8171AD"),
    PSYCHIC(10, "F74F82"),
    BUG(11, "9FAE1E"),
    ROCK(12, "A79133"),
    GHOST(13, "67518C"),
    DRAGON(14, "4D0AEF"),
    DARK(15, "654F41"),
    STEEL(16, "A4A4C3"),
    FAIRY(17, "E282E2");
    
    private final Color color;
    private final int id;

    private Type(int id, String hex) {
        this.color = hexToRGB(hex);
        this.id = id;
    }
    
    private static Color hexToRGB(String colorStr) {
        return new Color(
                Integer.valueOf( colorStr.substring( 0, 2 ), 16 ),
                Integer.valueOf( colorStr.substring( 2, 4 ), 16 ),
                Integer.valueOf( colorStr.substring( 4, 6 ), 16 ) );
    }

    public Color getColor() {
        return color;
    }
    
    public double getEffectivity(Type t) {
        return effectivityMatrix[id][t.id] / 2.0;
    }
    //1 = not very effective, 2 = normal, 4 = very effective, 0 = no effect
    //how effective is [id1] against [id2]
    private static final byte[][] effectivityMatrix = {
        {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0, 2, 2, 1, 2},
        {2, 1, 1, 2, 4, 4, 2, 2, 2, 2, 2, 4, 1, 2, 1, 2, 4, 2},
        {2, 4, 1, 2, 1, 2, 2, 2, 4, 2, 2, 2, 4, 2, 1, 2, 2, 2},
        {2, 2, 4, 1, 1, 2, 2, 2, 0, 4, 2, 2, 2, 2, 1, 2, 2, 2},
        {2, 1, 4, 2, 1, 2, 2, 1, 4, 1, 2, 1, 4, 2, 1, 2, 1, 2},
        {2, 1, 1, 2, 4, 1, 2, 2, 4, 4, 2, 2, 2, 2, 4, 2, 1, 2},
        {4, 2, 2, 2, 2, 4, 2, 1, 2, 1, 1, 1, 2, 0, 2, 4, 4, 1},
        {2, 2, 2, 2, 4, 2, 2, 1, 1, 2, 2, 2, 1, 1, 2, 2, 0, 4},
        {2, 4, 2, 4, 1, 2, 2, 4, 2, 0, 2, 1, 4, 2, 2, 2, 4, 2},
        {2, 2, 2, 1, 4, 2, 4, 2, 2, 2, 2, 4, 1, 2, 2, 2, 1, 2},
        {2, 2, 2, 2, 2, 2, 4, 4, 2, 2, 1, 2, 2, 2, 2, 0, 1, 2},
        {2, 1, 2, 2, 4, 2, 1, 1, 2, 1, 4, 2, 2, 1, 2, 4, 1, 1},
        {2, 4, 2, 2, 2, 4, 1, 2, 1, 4, 2, 4, 2, 2, 2, 2, 1, 2},
        {0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 4, 2, 2, 4, 2, 1, 2, 2},
        {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 4, 2, 1, 0},
        {2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 4, 2, 2, 4, 2, 1, 2, 1},
        {2, 1, 1, 1, 2, 4, 2, 2, 2, 2, 2, 2, 4, 2, 2, 2, 1, 2},
        {2, 1, 2, 2, 2, 2, 4, 1, 2, 2, 2, 2, 2, 2, 4, 4, 1, 2}
    };
}
