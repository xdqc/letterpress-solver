package swing_app;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Retrieve word definition via Oxford Dictionary API
 */
public class DefinitionWorker extends SwingWorker<String, Void> {
    private static Properties apiProps = new Properties();
    static {
        try (FileInputStream fIn = new FileInputStream("web/WEB-INF/od_api.properties")) {
            apiProps.load(fIn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private final String word;
    private final String queryType;
    private final String language = "en";
    private String app_id = (String) apiProps.get("app_id");
    private String app_key = (String) apiProps.get("app_key");
    private JEditorPane definitionPanel;

    /**
     * @param word      the word or root word to be queried
     * @param queryType "inflections" or "entries"
     */
    DefinitionWorker(JEditorPane panel, String word, String queryType) {
        this.definitionPanel = panel;
        this.word = word.toLowerCase();
        this.queryType = queryType;
    }

    @Override
    protected String doInBackground() throws Exception {
        URL url = new URL("https://od-api.oxforddictionaries.com:443/api/v1/" + queryType + "/" + language + "/" + word);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestProperty("app_id", app_id);
        urlConnection.setRequestProperty("app_key", app_key);

        // read the output from the server
        StringBuilder stringBuilder;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
            stringBuilder = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "Not found " + word + " in Oxford Dictionary";
        }
        return stringBuilder.toString();
    }

    @Override
    protected void done() {
        //parseJson, display definitions and etymologies
        try {
            if (queryType.equals("inflections")) {
                if (get().startsWith("Not")) {
                    definitionPanel.setText(get());
                    return;
                }
                parseInflection();
            } else if (queryType.equals("entries")) {
                if (get().startsWith("Not")) return;
                parseDefinition();
            }

        } catch (InterruptedException | ExecutionException | BadLocationException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse word definition json to html
     */
    private void parseDefinition() throws InterruptedException, ExecutionException, BadLocationException, IOException {
        HTMLDocument doc = (HTMLDocument) definitionPanel.getDocument();

        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(get()).getAsJsonObject();
        JsonArray lexical = json.get("results").getAsJsonArray().get(0).getAsJsonObject().get("lexicalEntries").getAsJsonArray();
        for (JsonElement lex : lexical) {
            String lexCategory = ((JsonObject) lex).get("lexicalCategory").getAsString();
            doc.insertBeforeEnd(doc.getElement("def"), "<h4>" + lexCategory + "</h4><ul id='" + lexCategory + "'><ul>");
            JsonArray entries = ((JsonObject) lex).get("entries").getAsJsonArray();
            for (JsonElement entry : entries) {
                //if no definition, use derivation
                if (!((JsonObject) entry).has("senses")) {
                    String derivation = ((JsonObject) lex).get("derivativeOf").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
                    doc.insertBeforeEnd(doc.getElement(lexCategory), "<li>" + derivation + "</li>");
                } else {
                    JsonArray senses = ((JsonObject) entry).get("senses").getAsJsonArray();
                    // display each definition
                    for (JsonElement sense : senses) {
                        String definition = ((JsonObject) sense).get("definitions").getAsJsonArray().get(0).getAsString();
                        doc.insertBeforeEnd(doc.getElement(lexCategory), "<li>" + definition + "</li>");
                        // also display sub-senses
                        if (((JsonObject) sense).has("subsenses")) {
                            for (JsonElement subsense : ((JsonObject) sense).get("subsenses").getAsJsonArray()) {
                                definition = ((JsonObject) subsense).get("definitions").getAsJsonArray().get(0).getAsString();
                                doc.insertBeforeEnd(doc.getElement(lexCategory), "<li>" + definition + "</li>");
                            }
                        }
                    }
                }
            }
            // remove the empty <ul> child in <ul id='lexCategory'>
            doc.removeElement(doc.getElement(lexCategory).getElement(0));
        }

        // show word origin
        JsonObject firstEntry = lexical.get(0).getAsJsonObject().get("entries").getAsJsonArray().get(0).getAsJsonObject();
        if (firstEntry.has("etymologies")) {
            String etymology = firstEntry.get("etymologies").getAsJsonArray().get(0).getAsString();
            doc.insertBeforeEnd(doc.getElement("def"), "<h4>Origin</h4><ul><li>" + etymology + "<ul></li>");
        }

        // show word pronunciation
        if (lexical.get(0).getAsJsonObject().has("pronunciations")){
            String pronunciation = lexical.get(0).getAsJsonObject().get("pronunciations").getAsJsonArray().get(0).getAsJsonObject()
                    .get("phoneticSpelling").getAsString();
            doc.insertAfterEnd(doc.getElement("title"), "/" + pronunciation + "/");
        }
    }

    /**
     * Parse word inflection json to html
     */
    private void parseInflection() throws InterruptedException, ExecutionException {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(get()).getAsJsonObject();
        JsonObject lexical = json.get("results").getAsJsonArray().get(0).getAsJsonObject().get("lexicalEntries").getAsJsonArray().get(0).getAsJsonObject();
        String inflectionOf = lexical.get("inflectionOf").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
        // remove trailing postfix
        if (inflectionOf.indexOf('-') > 0) {
            inflectionOf = inflectionOf.substring(0, inflectionOf.indexOf('-'));
        }
        String apiProvider = json.get("metadata").getAsJsonObject().get("provider").getAsString();
        definitionPanel.setText("");
        definitionPanel.setContentType("text/html");
        definitionPanel.setText("<html><head><style>body{font-family:SANS-SERIF}</style></head><body><h2 id='title'>" + word + "</h2>from the root word <strong>" + inflectionOf + "</strong>" +
                "<div id='def'></div><hr><p>Definition powered by " + apiProvider + "</p></body></html>");

            /*chain of api query to get the definition of root word*/
        SwingWorker<String, Void> getDefinitionWorker = new DefinitionWorker(definitionPanel, inflectionOf, "entries");
        getDefinitionWorker.execute();
    }

}

