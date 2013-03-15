/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.spoothie.chairs;

import java.util.ArrayList;
import net.minecraft.server.v1_5_R1.DataWatcher;
import net.minecraft.server.v1_5_R1.WatchableObject;


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
        WatchableObject wo = new WatchableObject(0, 0, Byte.valueOf(this.metadata));
        list.add(wo);
        return list;
    }
}
