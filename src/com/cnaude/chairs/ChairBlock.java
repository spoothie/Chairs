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
    private Material mat;
    private double sitHeight;
    
    public ChairBlock(Material m, double d) {
        mat = m;
        sitHeight = d;
    }   
    
    public Material getMat() {
        return mat;
    }
    
    public double getSitHeight() {
        return sitHeight;
    }
}
