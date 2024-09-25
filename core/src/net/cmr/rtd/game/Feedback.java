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
import java.util.function.Function;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;

import net.cmr.rtd.ProjectTetraTD;
import net.cmr.rtd.game.achievements.AchievementManager;
import net.cmr.rtd.game.achievements.custom.FeedbackAchievement;
import net.cmr.rtd.game.achievements.custom.TutorialCompleteAchievement;
import net.cmr.util.Audio;
import net.cmr.util.Audio.GameSFX;
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
        FeedbackForm form = new FeedbackForm(json);
        Long lastForm = ProjectTetraTD.getLastFormID();
        Long formId = form.formId;
        if (formId == lastForm) {
            throw new IOException("Feedback form already submitted.");
        }
        return form;
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
                ProjectTetraTD.setLastFormID(form.formId);
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
        JSONObject form;

        public FeedbackForm(JSONObject json) throws InvalidObjectException {
            this.form = json;
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
            emailField.setName("email");
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
            submitButton.setOrigin(Align.bottom);
            submitButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    boolean emailIsPresent = emailField.getText() != null && !emailField.getText().isEmpty() && emailField.getText().contains("@");
                    if (!emailIsPresent) {
                        submitButton.addAction(Actions.sequence(
                            Actions.run(() -> submitButton.setText("Please enter your email.")),
                            Actions.delay(3),
                            Actions.run(() -> submitButton.setText("Submit"))
                        ));
                        return;
                    }
                    SubmitResponse response = canSubmit();
                    if (!response.sendResponse) {
                        String message = response.message;
                        submitButton.addAction(Actions.sequence(
                            Actions.run(() -> submitButton.setText(message)),
                            Actions.delay(3),
                            Actions.run(() -> submitButton.setText("Submit"))
                        ));
                        return;
                    }
                    try {
                        submitButton.remove();
                        submit();

                        table.clear();
                        table.add("Thank you for your feedback! :)", "small").pad(10).row();
                        table.add("Your feedback has been\nsubmitted successfully.", "small").pad(10).row();
                        AchievementManager.getInstance().setAchievementValue(FeedbackAchievement.class, true);
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

        public SubmitResponse canSubmit() {
            for (Entry entry : entryList) {
                // Check if the entry is required
                if (entry.isRequired()) {
                    // Check if the entry is empty
                    if (entry.getResponse() == null || entry.getResponse().isEmpty()) {
                        if (entry.type == EntryType.LONG_TEXT) {
                            return SubmitResponse.NOT_LONG_ENOUGH;
                        }
                        if (entry.type == EntryType.SHORT_TEXT) {
                            return SubmitResponse.NOT_LONG_ENOUGH;
                        }
                        if (entry.type == EntryType.MULTIPLE_CHOICE) {
                            return SubmitResponse.EMPTY;
                        }
                        return SubmitResponse.EMPTY;
                    }
                }
            }
            return SubmitResponse.SUCCESS;
        }

        public enum SubmitResponse {
            SUCCESS,
            EMPTY("One or more entries are empty."),
            NOT_LONG_ENOUGH("Response in one or more text\nentries must be longer.")
            ;

            final String message;
            final boolean sendResponse;

            SubmitResponse() {
                this.message = "";
                this.sendResponse = true;
            }
            SubmitResponse(String message) {
                this.message = message;
                this.sendResponse = false;
            }
        }

        public enum EntryType {
            SCALE_10((entry) -> {
                Table table = new Table(Sprites.skin());
                ButtonGroup<TextButton> buttonGroup = new ButtonGroup<TextButton>();
                buttonGroup.setMaxCheckCount(1);
                buttonGroup.setMinCheckCount(0);
                for (int i = 1; i <= 10; i++) {
                    TextButton button = new TextButton(i + "", Sprites.skin(), "toggle-small");
                    button.setName(i + "");
                    buttonGroup.add(button);
                    button.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            super.clicked(event, x, y);
                            for (int j = 0; j < 10; j++) {
                                TextButton button = (TextButton) table.findActor((j + 1) + "");
                                button.setChecked(false);
                            }
                            button.setChecked(true);
                        }
                    });
                    table.add(button).size(25).pad(5);
                }
                return table;
            }, (table) -> {
                JSONObject response = new JSONObject();
                for (int i = 1; i <= 10; i++) {
                    TextButton button = table.findActor(i + "");
                    if (button.isChecked()) {
                        response.put("response", i);
                        return response;
                    }
                }
                return null;
            }),
            SHORT_TEXT((entry) -> {
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
                if (field.getText().isEmpty()) {
                    return null;
                }
                response.put("response", field.getText());
                return response;
            }),
            LONG_TEXT((entry) -> {
                final int MAX_LENGTH = 1000;
                Table table = new Table(Sprites.skin());
                Label minLengthDataLabel = new Label(entry.get("minLength")+"", Sprites.skin(), "small");
                minLengthDataLabel.setName("minLength");
                minLengthDataLabel.setVisible(false);
                table.add(minLengthDataLabel).size(0);
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

                if (field.getText().isEmpty()) {
                    return null;
                }
                long minLength = (long) Long.parseLong(((Label) table.findActor("minLength")).getText().toString());
                if (field.getText().length() < minLength) {
                    return null;
                }

                response.put("response", field.getText());
                return response;
            }),
            MULTIPLE_CHOICE((entry) -> {
                Table table = new Table(Sprites.skin());
                SelectBoxStyle style = new SelectBoxStyle(Sprites.skin().get("small", SelectBoxStyle.class));

                float selectionBoxListSpacing = 5;
                style.scrollStyle.background = new NinePatchDrawable(Sprites.skin().get("box", NinePatch.class));
                style.scrollStyle.background.setTopHeight(selectionBoxListSpacing);
                style.scrollStyle.background.setBottomHeight(selectionBoxListSpacing);
                style.scrollStyle.background.setLeftWidth(selectionBoxListSpacing);
                style.scrollStyle.background.setRightWidth(selectionBoxListSpacing);
                style.fontColor = Color.LIGHT_GRAY;
                SelectBox<String> selectBox = new SelectBox<>(style);
                selectBox.setName("multiple-choice");
                JSONArray choices = (JSONArray) entry.get("choices");
                ArrayList<String> choiceList = new ArrayList<>();
                for (int i = 0; i < choices.size(); i++) {
                    choiceList.add((String) choices.get(i));
                }
                choiceList.add(0, "Select an option...");
                selectBox.setItems(choiceList.toArray(new String[0]));
                selectBox.setAlignment(Align.center);
                selectBox.setSelectedIndex(0);
                table.add(selectBox).width(640 * (2/4f)).height(30).row();
                
                return table;
            }, (table) -> {
                JSONObject response = new JSONObject();
                SelectBox<String> field = table.findActor("multiple-choice");
                if (field.getSelectedIndex() == 0) {
                    return null;
                }
                response.put("response", field.getSelected());
                return response;
            }),
            BOOLEAN((entry) -> {
                Table table = new Table(Sprites.skin());
                CheckBox checkBox = new CheckBox("", Sprites.skin());
                checkBox.setName("boolean");
                table.add(checkBox).size(40).row();
                return table;
            }, (table) -> {
                JSONObject response = new JSONObject();
                CheckBox field = table.findActor("boolean");
                response.put("response", field.isChecked());
                return response;
            });

            Function<JSONObject, Table> inputTableCallable;
            Function<Table, JSONObject> responseHandlerCallable;

            EntryType(Function<JSONObject, Table> inputTableCallable, Function<Table, JSONObject> responseHandlerCallable) {
                this.inputTableCallable = inputTableCallable;
                this.responseHandlerCallable = responseHandlerCallable;
            }
        }

        private static class Entry {

            EntryType type;
            final String label;
            final boolean required;
            final JSONObject json;
            Table responseTable;

            public Entry(EntryType type, JSONObject json) {
                // Construct the entry object from the JSON object
                this.type = type;
                this.json = json;
                label = (String) json.get("label");
                required = (boolean) json.get("required");
                try {
                    responseTable = type.inputTableCallable.apply(json);
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

            public JSONObject getJSON() {
                // Return the JSON object of the entry
                return json;
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
