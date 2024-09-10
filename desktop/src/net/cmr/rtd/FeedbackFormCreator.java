package net.cmr.rtd;

import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import net.cmr.rtd.game.Feedback.FeedbackForm.EntryType;

public class FeedbackFormCreator {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        JSONObject root = new JSONObject();
        JSONArray fields = new JSONArray();
        root.put("fields", fields);
        while (true) {
            System.out.print("Enter field type: (");
            for (EntryType type : EntryType.values()) {
                System.out.print(type.name().toLowerCase() + ", ");
            }
            System.out.print("done): ");
            String type = scanner.nextLine();
            if (type.equals("done")) {
                break;
            }

            EntryType entryType = null;
            try {
                entryType = EntryType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid field type.");
                continue;
            }
            JSONObject field = new JSONObject();
            fields.add(field);
            field.put("type", entryType.name().toLowerCase());

            boolean required = false;
            while (true) {
                System.out.print("Is field required? (y/n): ");
                String response = scanner.nextLine();
                if (response.equalsIgnoreCase("y")) {
                    required = true;
                    break;
                } else {
                    break;
                }
            }
            field.put("required", required);

            String prompt = null;
            while (prompt == null || prompt.isEmpty()) {
                System.out.print("Enter prompt: ");
                prompt = scanner.nextLine();
            }
            field.put("label", prompt);

            switch (entryType) {
                case SCALE_10:
                case BOOLEAN:
                    break;
                case LONG_TEXT:
                case SHORT_TEXT:
                    // Get min length
                    int minLength = 0;
                    while (true) {
                        System.out.print("Enter min length: ");
                        try {
                            minLength = Integer.parseInt(scanner.nextLine());
                            break;
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid number.");
                        }
                    }
                    field.put("minLength", minLength);
                    break;
                case MULTIPLE_CHOICE:
                    JSONArray choices = new JSONArray();
                    while (true) {
                        System.out.print("Enter choice (done): ");
                        String choice = scanner.nextLine();
                        if (choice.equals("done")) {
                            break;
                        }
                        choices.add(choice);
                    }
                    field.put("choices", choices);
                    break;
            }
        }

        System.out.println("Done! Here is your feedback form data:");
        System.out.println(root.toJSONString());
    }

}
