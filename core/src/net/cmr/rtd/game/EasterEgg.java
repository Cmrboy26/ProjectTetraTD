package net.cmr.rtd.game;

import java.util.Calendar;

import net.cmr.rtd.ProjectTetraTD;
import net.cmr.util.CMRGame;

public class EasterEgg {

    public static void youJustLostTheGame() {
        System.exit(69420);
    }

    public static boolean forceEasterEggs() {
        return CMRGame.isDebug();
    }

    public static boolean isFelipe(String username) {
        if (forceEasterEggs()) {
            return true;
        }
        return username.equalsIgnoreCase("SirPotato42");
    }

    public static boolean isFelipe() {
        if (forceEasterEggs()) {
            return true;
        }
        String username = ProjectTetraTD.getInstance(ProjectTetraTD.class).getUsername();
        return isFelipe(username);
    }

    public static boolean isMaxwell(String username) {
        if (forceEasterEggs()) {
            return true;
        }
        return username.equalsIgnoreCase("mxwl");
    }

    public static boolean isMaxwell() {
        if (forceEasterEggs()) {
            return true;
        }
        String username = ProjectTetraTD.getInstance(ProjectTetraTD.class).getUsername();
        return isMaxwell(username);
    }

    public static boolean isColten(String username) {
        if (forceEasterEggs()) {
            return true;
        }
        return username.equalsIgnoreCase("cmrboy26");
    }

    public static boolean isColten() {
        if (forceEasterEggs()) {
            return true;
        }
        String username = ProjectTetraTD.getInstance(ProjectTetraTD.class).getUsername();
        return isColten(username);
    }

    public static boolean isAprilFools() {
        if (forceEasterEggs()) {
            return true;
        }
        return Calendar.getInstance().get(Calendar.MONTH) == Calendar.APRIL && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1;
    }
    
}
