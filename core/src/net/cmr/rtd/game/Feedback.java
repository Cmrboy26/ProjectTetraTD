package net.cmr.rtd.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.function.Function;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.cmr.rtd.screen.MainMenuScreen;
import net.cmr.util.Log;
import net.cmr.util.Sprites;

public class Feedback {
    
    public static final String API_URL = "https://api.coltenreissmann.me";

    /*
     
    Example Form JSON:
    {
        "idForm": 3, *garunteed
        "creationDate": "2021-04-01T00:00:00Z",*garunteed
        "formData": {
            "fields": [
                {
                    "type": "scale-10",
                    "label": "How much do you like this game?",
                    "required": true
                },
                {
                    "type": "short-text",
                    "label": "What is your favorite tower?",
                    "required": true
                },
                {
                    "type": "long-text",
                    "label": "What is your favorite part of the game?",
                    "required": true,
                    "minLength": 25
                },
                {
                    "type": "multiple-choice",
                    "label": "What is your favorite color?",
                    "required": true,
                    "choices": [
                        "Red",
                        "Blue",
                        "Green",
                        "Yellow",
                        "Orange",
                        "Purple",
                        "Black",
                        "White"
                    ]
                },
                {
                    "type": "boolean",
                    "label": "Do you like the music?",
                    "required": true
                }
            ]
        }
    }

     */

    /**
     * @throws IOException if the feedback form cannot be retrieved
     * @throws org.json.simple.parser.ParseException 
     * @throws InvalidObjectException if the feedback form could not be constructed from the JSON object
     */
    public static FeedbackForm retrieveFeedbackForm() throws IOException, ParseException, InvalidObjectException {
        // Retrieve the feedback form from the server
        String feedbackPageData = "";
        try {
            URL url = URI.create(API_URL + "/feedback").toURL();
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "PTTD-Game");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(5000);
            
			InputStream ins = con.getInputStream();
            InputStreamReader isr = new InputStreamReader(ins);
            BufferedReader in = new BufferedReader(isr);
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                feedbackPageData += inputLine;
            }
        } catch (Exception e) {
            throw new IOException("Failed to retrieve feedback form", e);
        }
        if (feedbackPageData.isEmpty()) {
            throw new IOException("Failed to retrieve feedback form");
        }
        Log.info(feedbackPageData);

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(feedbackPageData);
        Log.debug("Feedback", "Received feedback form: " + json.toJSONString());
        //json = (JSONObject) parser.parse("");
        return new FeedbackForm(json);
    }

    private static void submitFeedback(FeedbackForm form) throws IOException{
        // Submit the feedback form to the server. (Should be called inside the FeedbackForm class)
        try {
            URL url = URI.create(API_URL + "/feedback").toURL();
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "PTTD-Game");
            con.setConnectTimeout(5000);
            con.setDoOutput(true);
            byte[] input = form.toString().getBytes("utf-8");
            con.setFixedLengthStreamingMode(input.length);
            con.setRequestProperty("Content-Type", "application/json");
            con.connect();
            try (OutputStream os = con.getOutputStream()) {
                os.write(input);
            }
            int responseCode = con.getResponseCode();
            con.disconnect();
            if (responseCode == 200) {
                Log.info("Feedback form submitted successfully");
            } else {
                throw new IOException("Failed to submit feedback form: Got response code " + responseCode +", expected 200");
            }
        } catch (Exception e) {
            throw new IOException("Failed to submit feedback form", e);
        }
    }

    // TODO: Implement the FeedbackForm class
    public static class FeedbackForm {

        ArrayList<Entry> entryList;
        long formId;
        TextField emailField;

        public FeedbackForm(JSONObject json) throws InvalidObjectException {
            // Construct the feedback form object from the JSON object
            try {
                JSONObject formData = (JSONObject) json.get("formData");
                formId = (long) json.get("idForm");
                entryList = new ArrayList<>();
                JSONArray fields = (JSONArray) formData.get("fields");
                for (int i = 0; i < fields.size(); i++) {
                    JSONObject field = (JSONObject) fields.get(i);
                    String type = (String) field.get("type");
                    type = type.toUpperCase().replace("-", "_");
                    try {
                        EntryType entryType = EntryType.valueOf(type);
                        JSONObject entryObject = (JSONObject) fields.get(i);
                        Entry entry = new Entry(entryType, entryObject);
                        entryList.add(entry);
                    } catch (IllegalArgumentException e) {
                        throw new InvalidObjectException("Invalid entry type: " + type);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new InvalidObjectException("Failed to construct feedback form from JSON object");
            }
        }

        public Table getForm() {
            // Return the form as a Table object for modification
            final Table table = new Table(Sprites.skin());
            table.add("- Feedback Form -").pad(10).row();

            TextFieldStyle style = new TextFieldStyle(Sprites.skin().get("small", TextFieldStyle.class));
            style.fontColor = Color.LIGHT_GRAY;
            Label emailLabel = new Label("Your Email", Sprites.skin(), "small");
            emailField = new TextField("", style);
            emailField.setAlignment(Align.center);
            emailField.setMaxLength(120);
            emailField.setMessageText("Type here...");
            table.add(emailLabel).row();
            table.add(emailField).pad(10).width(640 * (2/4f)).row();

            int i = 1;
            for (Entry entry : entryList) {
                String label = entry.getLabel() + (entry.isRequired() ? " *" : "");
                label = i + ". " + label;
                i++;
                Label labelObject = new Label(label, Sprites.skin(), "small");
                table.add(labelObject).row();
                table.add(entry.responseTable).pad(10).row();
            }

            TextButton submitButton = new TextButton("Submit", Sprites.skin(), "small");
            submitButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    if (!canSubmit()) {
                        return;
                    }
                    try {
                        submitButton.remove();
                        submit();
                    } catch (IOException e) {
                        e.printStackTrace();
                        // TODO: Display error message
                        table.clear();
                        table.add(e.getMessage(), "small").pad(10).row();
                        Label errorLabel = new Label(e.getCause().getMessage(), Sprites.skin(), "small");
                        errorLabel.setWrap(true);
                        table.add(errorLabel).pad(10).size(640 * (2/4f), 100).row();
                        table.add("Please try again later.", "small").pad(10).row();
                    }
                }
            });
            submitButton.pad(0, 20, 0, 20);
            submitButton.setWidth(100);
            table.add(submitButton).pad(10).row();

            return table;
        }

        public String getEmail() {
            // Return the email address of the submitter
            return emailField.getText();
        }

        public void submit() throws IOException {
            // Submit the feedback form to the server
            submitFeedback(this);
        }

        public boolean canSubmit() {
            // TODO: Check if all the required fields are filled out enough to submit
            return true;
        }

        public enum EntryType {
            SCALE_10(() -> {
                Table table = new Table(Sprites.skin());
                return table;
            }, (table) -> {
                JSONObject response = new JSONObject();
                return response;
            }),
            SHORT_TEXT(() -> {
                final int MAX_LENGTH = 50;

                Table table = new Table(Sprites.skin());
                TextFieldStyle style = new TextFieldStyle(Sprites.skin().get("small", TextFieldStyle.class));
                style.fontColor = Color.LIGHT_GRAY;
                TextField textArea = new TextField("", style);
                textArea.setName("short-text");
                textArea.setMaxLength(MAX_LENGTH);
                textArea.setMessageText("Type here...");
                textArea.setWidth(640 * (2/3f));
                table.add(textArea).width(640 * (2/4f)).row();
                return table;
            }, (table) -> {
                JSONObject response = new JSONObject();
                TextField field = table.findActor("short-text");
                response.put("response", field.getText());
                return response;
            }),
            LONG_TEXT(() -> {
                final int MAX_LENGTH = 1000;

                Table table = new Table(Sprites.skin());
                TextFieldStyle style = new TextFieldStyle(Sprites.skin().get("small", TextFieldStyle.class));
                style.fontColor = Color.LIGHT_GRAY;
                TextArea textArea = new TextArea("", style);
                textArea.setName("long-text");
                textArea.setMaxLength(MAX_LENGTH);
                textArea.setMessageText("Type here...");
                textArea.setWidth(640 * (2/3f));
                table.add(textArea).width(640 * (2/4f)).height(100).row();
                return table;
            }, (table) -> {
                JSONObject response = new JSONObject();
                TextArea field = table.findActor("long-text");
                response.put("response", field.getText());
                return response;
            }),
            MULTIPLE_CHOICE(() -> {
                Table table = new Table(Sprites.skin());
                return table;
            }, (table) -> {
                JSONObject response = new JSONObject();
                return response;
            }),
            BOOLEAN(() -> {
                Table table = new Table(Sprites.skin());
                return table;
            }, (table) -> {
                JSONObject response = new JSONObject();
                return response;
            });

            Callable<Table> inputTableCallable;
            Function<Table, JSONObject> responseHandlerCallable;

            EntryType(Callable<Table> inputTableCallable, Function<Table, JSONObject> responseHandlerCallable) {
                this.inputTableCallable = inputTableCallable;
                this.responseHandlerCallable = responseHandlerCallable;
            }
        }

        private static class Entry {

            EntryType type;
            final String label;
            final boolean required;
            Table responseTable;
            public Entry(EntryType type, JSONObject json) {
                // Construct the entry object from the JSON object
                this.type = type;
                label = (String) json.get("label");
                required = (boolean) json.get("required");
                try {
                    responseTable = type.inputTableCallable.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public String getLabel() {
                // Return the title of the entry
                return label;
            }

            public boolean isRequired() {
                // Return whether the entry is required
                return required;
            }

            public JSONObject getResponse() {
                // Return the response to the entry
                return type.responseHandlerCallable.apply(responseTable);
            }

        }

        public String toString() {
            JSONObject form = new JSONObject();
            form.put("formId", 3);
            form.put("submitterEmail", getEmail());
            JSONArray responses = new JSONArray();
            for (Entry entry : entryList) {
                responses.add(entry.getResponse());
            }
            form.put("responseData", responses);
            return form.toJSONString();
        }
    }

}
