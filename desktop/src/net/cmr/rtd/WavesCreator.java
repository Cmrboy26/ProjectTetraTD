package net.cmr.rtd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.cmr.rtd.game.world.EnemyFactory.EnemyType;
import net.cmr.rtd.waves.Wave;
import net.cmr.rtd.waves.WaveUnit;
import net.cmr.rtd.waves.WavesData;
import net.cmr.rtd.waves.WavesData.DifficultyRating;

public class WavesCreator {
    
    WavesData wavesData;
    Scanner scanner;
    PrintStream outputStream;

    public WavesCreator(PrintStream outputStream, InputStream inputStream) {
        scanner = new Scanner(inputStream);
        this.outputStream = outputStream;
        wavesData = new WavesData();

        boolean running = false;
        while (true) {
            String input = getInput();
            String command = input.split(" ")[0];
            String withoutCommand = input.substring(command.length()).trim();
            String[] args = withoutCommand.split(" ");
            ArrayList<String> argList = new ArrayList<String>(Arrays.asList(args));
            argList.removeIf(String::isEmpty);
            args = argList.toArray(new String[0]);

            switch (command) {
                case "import": {
                    if (args.length < 1) {
                        outputStream.println("Please enter the JSON data to import.");
                        break;
                    }
                    String everythingAfterCommand = input.substring(command.length()).trim();
                    everythingAfterCommand = everythingAfterCommand.replace("\"", "");

                    System.out.println("Importing: \""+everythingAfterCommand+"\"");
                    File file = new File(everythingAfterCommand);
                    String json = "{}";
                    if (!file.exists()) {
                        outputStream.println("Importing from argument...");
                        json = args[0];
                    } else {
                        outputStream.println("Importing from file...");
                        try (FileInputStream fis = new FileInputStream(file)) {
                            byte[] data = new byte[(int) file.length()];
                            fis.read(data);
                            json = new String(data, "UTF-8");
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        JSONParser parser = new JSONParser();
                        JSONObject main = (JSONObject) parser.parse(json);
                        wavesData = WavesData.deserialize(main);
                        outputStream.println("Imported waves data.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "export":
                    try {
                        JSONObject main = new JSONObject();
                        wavesData.serialize(main);
                        outputStream.println(main.toJSONString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } 
                    break;
                case "params": {
                    int param = -1;
                    boolean skipinput = false;
                    if (args.length >= 1) {
                        try {
                            param = Integer.parseInt(args[0]);
                            skipinput = true;
                        } catch (NumberFormatException e) {
                            skipinput = false;
                        }
                    }
                    if (!skipinput) {
                        outputStream.println("Select the paramater to edit: ");
                        outputStream.println("1: Name");
                        outputStream.println("2: Preparation Time");
                        outputStream.println("3: Starting Money");
                        outputStream.println("4: Starting Health");
                        outputStream.println("5: Difficulty");
                        outputStream.println("6: Exit");
                        param = getNextInt();
                    }
                    switch (param) {
                        case 1:
                            outputStream.println("Enter the name of the name of the wavesdata: ");
                            String name = getInput();
                            wavesData.name = name;
                            break;
                        case 2:
                            outputStream.println("Enter the time to prepare for each wave: ");
                            int preparationTime = getNextInt();
                            wavesData.preparationTime = preparationTime;
                            break;
                        case 3:
                            outputStream.println("Enter the starting money (recommended: 35): ");
                            int startingCash = getNextInt();
                            wavesData.startingMoney = startingCash;
                            break;
                        case 4:
                            outputStream.println("Enter starting health: ");
                            int startingHealth = getNextInt();
                            wavesData.startingHealth = startingHealth;
                            break;
                        case 5:
                            outputStream.println("Finally, enter the difficulty level: ");
                            int difficulty = 0;
                            while (true) {
                                String line = getInput();
                                try {
                                    difficulty = Integer.parseInt(line);
                                    if (difficulty >= 1 && difficulty < DifficultyRating.values().length) {
                                        break;
                                    } else {
                                        outputStream.println("Invalid difficulty. Please enter a number between 1 and " + (DifficultyRating.values().length - 1));
                                    }
                                } catch (NumberFormatException e) {
                                    outputStream.println("Invalid input. Please enter a number.");
                                }
                            }
                            wavesData.difficulty = DifficultyRating.values()[difficulty];
                            break;
                        case 6:
                            break;
                    }
                    break;
                }
                case "wave": {
                    String subCommand;
                    if (args.length >= 1) {
                        subCommand = args[0];
                    } else {
                        outputStream.println("Select command: ");
                        outputStream.println("- \"add\"");
                        outputStream.println("- \"set\"");
                        outputStream.println("- \"get\"");
                        subCommand = getInput();
                    }
                    switch (subCommand.toLowerCase()) {
                        case "add": {
                            int waveNum = wavesData.waves.size();
                            modifyWave(waveNum);
                            break;
                        }
                        case "get": {
                            int waveNum = -1;
                            if (args.length >= 2) {
                                try {
                                    waveNum = Integer.parseInt(args[1]);
                                    if (waveNum <= 0) {
                                        outputStream.println("Invalid wave number. Must be greater than 0.");
                                        break;
                                    }
                                } catch (NumberFormatException e) {
                                    outputStream.println("Invalid wave number.");
                                    break;
                                }   
                            } else {
                                outputStream.println("Enter the wave number: ");
                                waveNum = getNextInt();
                            }
                            Wave wave = wavesData.getWave(waveNum);
                            if (wave == null) {
                                outputStream.println("Wave "+(waveNum)+" does not exist.");
                            } else {
                                outputStream.println("Wave "+(waveNum)+":");
                                outputStream.println("Duration: "+wave.getWaveTime());
                                outputStream.println("Warn: "+(wave.shouldWarnPlayer() ? "Yes" : "No"));
                                outputStream.println("Additional Preparation Time: "+wave.getAdditionalPrepTime());
                                outputStream.println("Units: ");
                                for (WaveUnit unit : wave.getWaveUnits()) {
                                    outputStream.println("-- "+unit);
                                }
                            }
                            break;
                        }
                        case "set": {
                            int waveNum = -1;
                            if (args.length >= 2) {
                                try {
                                    waveNum = Integer.parseInt(args[1]) - 1;
                                    if (waveNum < 0) {
                                        outputStream.println("Invalid wave number. Must be greater than 0.");
                                        break;
                                    }
                                } catch (NumberFormatException e) {
                                    outputStream.println("Invalid wave number.");
                                    break;
                                }
                            } else {
                                outputStream.println("Enter the wave number: ");
                                waveNum = getNextInt() - 1;
                                if (waveNum < 0) {
                                    outputStream.println("Invalid wave number. Must be greater than 0.");
                                    break;
                                }
                            }
                            modifyWave(waveNum);
                            break; 
                        }
                        default: {
                            outputStream.println("Invalid subcommand.");
                        }
                    }
                    break;
                }
                case "exit":
                    System.exit(0);
                    break;
                default: {
                    outputStream.println("Invalid command. Available commands: ");
                    outputStream.println("- import [file or json]");
                    outputStream.println("- export");
                    outputStream.println("- params [param]");
                    outputStream.println("- wave [add|set <wave number>]");
                    outputStream.println("- exit");
                    break;
                }
            }
        }
    }

    public String getInput() {
        outputStream.print("> ");
        return scanner.nextLine();
    }

    public void modifyWave(int waveNumber) {
        waveNumber++;
        Wave wave = wavesData.getWave(waveNumber);
        if (wave == null) {
            // Create new wave
            wave = new Wave(0);
        }
        while (true) {
            outputStream.print("Wave "+waveNumber+" ");
            String input = getInput();
            String command = input.split(" ")[0];
            String withoutCommand = input.substring(command.length()).trim();
            String[] args = withoutCommand.split(" ");
            ArrayList<String> argList = new ArrayList<String>(Arrays.asList(args));
            argList.removeIf(String::isEmpty);
            args = argList.toArray(new String[0]);

            switch (command.toLowerCase()) {
                case "exit": {
                    wavesData.waves.put(waveNumber, wave);
                    return;
                }
                case "duration": {
                    if (args.length < 1) {
                        outputStream.println("Enter the duration of the wave: ");
                        wave.setWaveTime(getNextInt());
                    } else {
                        try {
                            wave.setWaveTime(Integer.parseInt(args[0]));
                        } catch (NumberFormatException e) {
                            outputStream.println("Invalid input. Please enter a number.");
                        }
                    }
                    break;
                }
                case "warn": {
                    if (args.length < 1) {
                        outputStream.println("Enter whether to warn the player: ");
                        outputStream.println("1: Yes");
                        outputStream.println("2: No");
                        int warn = getNextInt();
                        wave.setWarnPlayer(warn == 1);
                    } else {
                        wave.setWarnPlayer(args[0].equalsIgnoreCase("yes"));
                    }
                    break;
                }
                case "prep": {
                    if (args.length < 1) {
                        outputStream.println("Enter the additional preparation time: ");
                        wave.setAdditionalPrepTime(getNextInt());
                    } else {
                        try {
                            wave.setAdditionalPrepTime(Integer.parseInt(args[0]));
                        } catch (NumberFormatException e) {
                            outputStream.println("Invalid input. Please enter a number.");
                        }
                    }
                    break;
                }
                case "unit": {
                    int unitNumber = -1;
                    int quantity = 0;
                    float startTime = 0;
                    float endTime = 0;

                    if (args.length == 1) {
                        if (args[0].equalsIgnoreCase("clear")) {
                            wave.clearWaveUnits();
                            outputStream.println("Cleared wave units.");
                            break;
                        } 
                    }

                    if (args.length >= 1) {
                        try {
                            unitNumber = Integer.parseInt(args[0]);
                            if (unitNumber >= 0 && unitNumber < EnemyType.values().length) {
                                break;
                            } else {
                                outputStream.println("Invalid unit number. Please enter a number between 0 and " + (EnemyType.values().length - 1));
                                break;
                            }
                        } catch (NumberFormatException e) {
                            outputStream.println("Invalid unit number.");
                            break;
                        }
                    } else {
                        outputStream.println("Enter the unit number (\"l\" for list): ");
                        boolean exit = false;
                        while (true) {
                            input = getInput();
                            if (input.equalsIgnoreCase("l")) {
                                outputStream.println("Available types: ");
                                for (int i = 0; i < EnemyType.values().length; i++) {
                                    outputStream.println("-- "+i + ": " + EnemyType.values()[i]);
                                }
                            }
                            if (input.equals("")) { exit = true; break; }
                            try {
                                unitNumber = Integer.parseInt(input);
                            } catch (NumberFormatException e) {
                                outputStream.println("Invalid unit number.");
                                continue;
                            }
                            if (unitNumber >= 0 && unitNumber < EnemyType.values().length) {
                                break;
                            } else {
                                outputStream.println("Invalid unit number. Please enter a number between 0 and " + (EnemyType.values().length - 1));
                            }
                        }
                        if (exit) { break; }
                    }

                    if (args.length >= 2) {
                        try {
                            quantity = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            outputStream.println("Invalid quantity.");
                            break;
                        }
                    } else {
                        outputStream.println("Enter the quantity: ");
                        quantity = getNextInt();
                    }
                    boolean distributed = false;
                    if (args.length >= 3) {
                        if (args[2].equals("dist")) {
                            startTime = 0;
                            distributed = true;
                        } else {
                            try {
                                startTime = Float.parseFloat(args[2]);
                            } catch (NumberFormatException e) {
                                outputStream.println("Invalid start time.");
                                break;
                            }
                        }
                    } else {
                        outputStream.println("Enter the start time: ");
                        input = getInput();
                        if (input.equals("") || input.equalsIgnoreCase("dist")) {
                            startTime = 0;
                            endTime = wave.getWaveTime();
                            distributed = true;
                        } else {
                            try {
                                startTime = Float.parseFloat(input);
                            } catch (NumberFormatException e) {
                                outputStream.println("Invalid start time.");
                                break;
                            }
                        }
                    }
                    if (!distributed) {
                        if (args.length >= 4) {
                            try {
                                endTime = Float.parseFloat(args[3]);
                            } catch (NumberFormatException e) {
                                outputStream.println("Invalid end time.");
                                break;
                            }
                        } else {
                            outputStream.println("Enter the end time: ");
                            endTime = getNextInt();
                        }
                    }
                    WaveUnit unit = new WaveUnit(startTime, endTime, EnemyType.values()[unitNumber], quantity);
                    wave.addWaveUnit(unit);
                    outputStream.println("Added wave unit: "+unit);

                    break;
                }
                default: {
                    outputStream.println("Invalid command. Available commands: ");
                    outputStream.println("- duration [time]");
                    outputStream.println("- warn [yes/no]");
                    outputStream.println("- prep [time]");
                    outputStream.println("- unit");
                    outputStream.println("- exit");
                    break;
                }
            }
        }
    }

    /*public WavesCreator() {
        scanner = new Scanner(System.in);
        wavesData = new WavesData();
        int wavenumber = 0;

        outputStream.println("Enter the name of the name of the selection: ");
        String name = scanner.nextLine();
        wavesData.name = name;

        outputStream.println("Enter the time to prepare for each wave: ");
        int preparationTime = getNextInt();
        wavesData.preparationTime = preparationTime;
        outputStream.println("Enter the starting money (recommended: 35): ");
        int startingCash = getNextInt();
        wavesData.startingMoney = startingCash;
        outputStream.println("Enter starting health: ");
        int startingHealth = getNextInt();
        wavesData.startingHealth = startingHealth;

        while (true) {
            wavenumber++;
            outputStream.println("Wave "+wavenumber+": Enter the duration of the wave (or press just enter to exit): ");
            int duration = 0;
            Wave wave;
            boolean exit = false;
            while (true) {
                String line = scanner.nextLine();
                if (line.equalsIgnoreCase("")) {
                    exit = true;
                    break;
                }
                try {
                    duration = Integer.parseInt(line);
                    break;
                } catch (NumberFormatException e) {
                    outputStream.println("Invalid input. Please enter a number.");
                }
            }
            if (exit) { break; }
            
            wave = new Wave(duration);
            while (true) {
                EnemyType enemy = null;
                float startTime = 0;
                float endTime = 0;
                int quantity = 0;

                while (true) {
                    outputStream.println("Enter the enemy type: (Type l to list the available types, enter to exit)");
                    String input = scanner.nextLine();
                    if (input.equals("")) {
                        exit = true;
                        break;
                    }
                    if (input.equalsIgnoreCase("l")) {
                        outputStream.println("Available types: ");
                        for (int i = 0; i < EnemyType.values().length; i++) {
                            outputStream.println("- "+i + ": " + EnemyType.values()[i]);
                        }
                    } else {
                        try {
                            int type = Integer.parseInt(input);
                            if (type >= 0 && type < EnemyType.values().length) {
                                enemy = EnemyType.values()[type];
                                break;
                            } else {
                                outputStream.println("Invalid type. Please enter a number between 0 and " + (EnemyType.values().length - 1));
                            }
                        } catch (Exception e) {
                            outputStream.println("Invalid input. Please enter a number.");
                        }
                    }
                }
                if (exit) { break; }

                outputStream.println("Enter quantity of enemies: ");
                quantity = getNextInt();
                outputStream.println("Enter start time (enter for even distribution): ");
                while (true) {
                    String line = scanner.nextLine();
                    if (line.equals("")) {
                        startTime = 0;
                        endTime = duration;
                        break;
                    }
                    try {
                        startTime = Float.parseFloat(line);
                        break;
                    } catch (NumberFormatException e) {
                        outputStream.println("Invalid input. Please enter a number.");
                    }
                }
                if (endTime == 0) {
                    outputStream.println("Enter end time: ");
                    endTime = getNextInt();
                }
                
                WaveUnit unit = new WaveUnit(startTime, endTime, enemy, quantity);
                wave.addWaveUnit(unit);
                outputStream.println("Added wave unit: "+unit);
            }
            wavesData.waves.put(wavenumber, wave);
        }

        outputStream.println("Finally, enter the difficulty level: ");
        int difficulty = 0;
        while (true) {
            String line = scanner.nextLine();
            try {
                difficulty = Integer.parseInt(line);
                if (difficulty >= 1 && difficulty < DifficultyRating.values().length) {
                    break;
                } else {
                    outputStream.println("Invalid difficulty. Please enter a number between 1 and " + (DifficultyRating.values().length - 1));
                }
            } catch (NumberFormatException e) {
                outputStream.println("Invalid input. Please enter a number.");
            }
        }
        wavesData.difficulty = DifficultyRating.values()[difficulty];

        try {
            JSONObject main = new JSONObject();
            wavesData.serialize(main);
            outputStream.println(main.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }*/

    public int getNextInt() {
        while (true) {
            String line = getInput();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                outputStream.println("Invalid input. Please enter a number.");
            }
        }
    }

    public static void main(String[] args) {
        new WavesCreator(System.out, System.in);
    }

}
