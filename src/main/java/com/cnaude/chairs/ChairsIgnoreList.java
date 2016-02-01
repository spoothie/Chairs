package com.cnaude.chairs;

import java.io.*;
import java.util.ArrayList;

/**
 *
 * @author naudec
 */
@SuppressWarnings("serial")
public class ChairsIgnoreList implements Serializable{
    private static ArrayList<String> ignoreList = new ArrayList<>();
    private static final String IGNORE_FILE = "plugins/Chairs/ignores.ser";
    
    @SuppressWarnings("unchecked")
    public void load() {        
        File file = new File(IGNORE_FILE);
        if (!file.exists()) {
            Chairs.get().logInfo("Ignore file '"+file.getAbsolutePath()+"' does not exist.");
            return;
        }
        try {                
            FileInputStream f_in = new FileInputStream(file);
            try (ObjectInputStream obj_in = new ObjectInputStream (f_in)) {
                ignoreList = (ArrayList<String>) obj_in.readObject();
            }               
            Chairs.get().logInfo("Loaded ignore list. (Count = "+ignoreList.size()+")");
        }
        catch(IOException | ClassNotFoundException e) {
          Chairs.get().logError(e.getMessage());
        }
    }
    
    public void save() {
        try {
            File file = new File(IGNORE_FILE); 
            FileOutputStream f_out = new FileOutputStream (file);
            try (ObjectOutputStream obj_out = new ObjectOutputStream (f_out)) {
                obj_out.writeObject (ignoreList);
            }
            Chairs.get().logInfo("Saved ignore list. (Count = "+ignoreList.size()+")");
        }
        catch(Exception e) {
          Chairs.get().logError(e.getMessage());
        }
    }
    
    public void addPlayer(String s) {
        if (ignoreList.contains(s)) {
            return;
        }
        //Chairs.get().logInfo("Adding " + s + " to ignore list.");
        ignoreList.add(s);
    }
    
    public void removePlayer(String s) {
        //Chairs.get().logInfo("Removing " + s + " from ignore list.");
        ignoreList.remove(s);
    }
    
    public boolean isIgnored(String s) {
        return ignoreList.contains(s);
    }
}