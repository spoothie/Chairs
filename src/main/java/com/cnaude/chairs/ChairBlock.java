/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cnaude.chairs;

import org.bukkit.Material;

/**
 *
 * @author cnaude
 */
public class ChairBlock {
    private final Material mat;
    private final double sitHeight;
    private final byte data;
    
    public ChairBlock(Material m, double s, String d) {
        mat = m;
        sitHeight = s;
        data = Byte.parseByte(d);
    }   
    
    public Material getMat() {
        return mat;
    }
    
    public double getSitHeight() {
        return sitHeight;
    }
    
    public byte getDamage() {
        return data;
    }
}
