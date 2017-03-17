/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.joba.pokemonbattle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *
 * @author jonas
 */
public class Pokemon {
    
    public static double CRITICAL_HIT_CHANCE = 0.1;
    public final UUID uuid;
    private final int attack, defense, maxHealth, speed, level, power;
    private final Type type;
    private int health;
    private Pokemon killer;
    
    public Pokemon(Type type, int attack, int defense, int maxHealth, int speed) {
        this(type, attack, defense, maxHealth, speed, UUID.randomUUID());
    }
    
    private Pokemon(Type type, int attack, int defense, int maxHealth, int speed, UUID uuid) {
        this.attack = attack;
        this.defense = defense;
        this.health = maxHealth;
        this.maxHealth = maxHealth;
        this.speed = speed;
        this.type = type;
        this.level = 100;
        this.power = 200;
        this.uuid = uuid;
    }
    
    public Pokemon clone() {
        return new Pokemon(type, attack, defense, maxHealth, speed, uuid);
    }
    
    public void simulateAttack(Pokemon pokemon) {
        boolean isCrit = Math.random() + CRITICAL_HIT_CHANCE >= 1.0;
        double level = isCrit ? pokemon.level * 2 : pokemon.level;
        double ad = (double)pokemon.attack / (double)this.defense;
        double damage = ((2 * level / 5.0) * power * ad / 50 + 2);
        damage *= pokemon.getType().getEffectivity(type);
        int dealt = (int) Math.round(damage);
        this.health = Math.max(0, this.health - dealt);
        if(health <= 0) {
            killer = pokemon;
        }
    }
    
    public Pokemon getKiller() {
        return killer;
    }
    
    public void reset() {
        this.killer = null;
        this.health = this.maxHealth;
    }

    public Type getType() {
        return type;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getSpeed() {
        return speed;
    }

    public int getHealth() {
        return health;
    }
}
