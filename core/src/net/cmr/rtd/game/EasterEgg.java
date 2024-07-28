package net.cmr.rtd.game;

import java.util.Calendar;

import net.cmr.rtd.ProjectTetraTD;

public class EasterEgg {

    public static void youJustLostTheGame() {
        System.exit(69420);
    }

    public static boolean isFelipe() {
        String username = ProjectTetraTD.getInstance(ProjectTetraTD.class).getUsername();
        return username.equalsIgnoreCase("SirPotato42");
    }

    public static boolean isMaxwell() {
        String username = ProjectTetraTD.getInstance(ProjectTetraTD.class).getUsername();
        return username.equalsIgnoreCase("mxwl");
    }

    public static boolean isAprilFools() {
        return Calendar.getInstance().get(Calendar.MONTH) == Calendar.APRIL && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1;
    }
    
}
