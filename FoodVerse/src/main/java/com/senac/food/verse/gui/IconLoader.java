package com.senac.food.verse.gui;
/**/
import javax.swing.*;
import java.awt.*;
import java.net.URL;

public final class IconLoader {
    private IconLoader(){}

    public static Icon load(String path, int w, int h){
        try{
            URL u = IconLoader.class.getResource(path);
            if(u==null) return null;
            ImageIcon ic = new ImageIcon(u);
            if(w>0 && h>0){
                return new ImageIcon(ic.getImage().getScaledInstance(w,h,Image.SCALE_SMOOTH));
            }
            return ic;
        }catch(Exception e){
            return null;
        }
    }
}