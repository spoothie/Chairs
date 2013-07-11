/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cnaude.chairs;

import java.util.ArrayList;
import net.minecraft.server.v1_6_R2.DataWatcher;
import net.minecraft.server.v1_6_R2.WatchableObject;

/**
 *
 * @author cnaude
 */
public class ChairWatcher extends DataWatcher {

    private byte metadata;

    public ChairWatcher(byte i) {
        this.metadata = i;
    }

    @Override
    public ArrayList<WatchableObject> b() {
        ArrayList<WatchableObject> list = new ArrayList<WatchableObject>();
        WatchableObject wo = new WatchableObject(0, 0, this.metadata);        
        list.add(wo);
        return list;
    }
}