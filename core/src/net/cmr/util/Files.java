package net.cmr.util;

import java.io.File;
import java.io.FilenameFilter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;

public class Files {
    
    private final NativeFileChooser fileChooser;
    private Files(NativeFileChooser fileChooser) {
        this.fileChooser = fileChooser;
    }

    private static Files instance;
    public static Files getInstance() {
        return instance;
    }
    public static void initializeFiles(NativeFileChooser fileChooser) {
        instance = new Files(fileChooser);
    }

    public static NativeFileChooserConfiguration getDefaultFileChooserConfiguration() {
        NativeFileChooserConfiguration conf = new NativeFileChooserConfiguration();
        conf.directory = Gdx.files.absolute(System.getProperty("user.home"));
        return conf;
    }

    public static NativeFileChooserConfiguration setImageFilter(NativeFileChooserConfiguration conf) {
        conf.mimeFilter = "image/png";
        conf.nameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("png");
            }
        };
        return conf;
    }

    public void promptFile(NativeFileChooserConfiguration conf, NativeFileChooserCallback callback) {
        fileChooser.chooseFile(conf, callback);
    }

    public void doSomething() {
        NativeFileChooserConfiguration conf = new NativeFileChooserConfiguration();
        conf.directory = Gdx.files.absolute(System.getProperty("user.home"));

        // Filter out all files which do not have the .ogg extension and are not of an audio MIME type - belt and braces
        conf.mimeFilter = "audio/*";
        conf.nameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("ogg");
            }
        };

        // Add a nice title
        conf.title = "Choose audio file";
        fileChooser.chooseFile(conf, new NativeFileChooserCallback() {
            @Override
            public void onFileChosen(FileHandle file) {

            }

            @Override
            public void onCancellation() {
                // Warn user how rude it can be to cancel developer's effort
            }

            @Override
            public void onError(Exception exception) {
                // Handle error (hint: use exception type)
            }
        });
    }

}
