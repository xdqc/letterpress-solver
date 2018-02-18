package swing_app;

import DbConnector.DbConnector;
import com.google.gson.*;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.MetalToggleButtonUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class LetterGridPanel extends JPanel implements GamesTableSelectedListener {

    private JPanel grid = new JPanel();
    private JEditorPane definitionPanel = new JEditorPane();
    private JButton clear_btn;
    private JButton find_btn;
    private JToggleButton[] letter_btns = new JToggleButton[25];
    private final JList<String> resultList = new JList<>();

    private Map<Character, Integer> freq = new TreeMap<>();
    private Map<Character, Integer> freqG = new TreeMap<>();
    private List<String> wordResult = new ArrayList<>();
    private boolean ctrlPressing = false;


    public LetterGridPanel() {

        buildUI();

        /*Clear selected letters*/
        clear_btn.addActionListener(e -> {
            for (JToggleButton letter_btn : letter_btns) {
                letter_btn.setSelected(false);
            }
        });

        /*Find words that contains selected letters*/
        find_btn.addActionListener(e -> {
            freq.clear();
            for (int i = 'A'; i <= 'Z'; i++) {
                freq.put((char) i, 0);
            }

            for (JToggleButton letter_btn : letter_btns) {
                if (letter_btn.isSelected()) {
                    char c = letter_btn.getText().charAt(0);
                    int count = freq.getOrDefault(c, 0);
                    freq.put(c, ++count);
                }
            }
            wordResult.clear();
            find_btn.setEnabled(false);

            SwingWorker<List<String>, Void> findWordsWorker = new FindWordsWorker();
            findWordsWorker.execute();
        });

        /*Remove selected word from dictionary*/
        resultList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = ((JList) e.getSource()).getSelectedIndex();
                    String word = resultList.getModel().getElementAt(row);

                    SwingWorker<String, Void> getDefinitionWorker = new DefinitionWorker(word, "inflections");
                    getDefinitionWorker.execute();

                    JScrollPane scrollPane = new JScrollPane(definitionPanel);
                    scrollPane.setPreferredSize(new Dimension(600, 350));
                    scrollPane.setMaximumSize(new Dimension(600, 350));

                    String[] options = {"Remove [" + word + "]", "Cancel"};
                    int res = JOptionPane.showOptionDialog(null, scrollPane, "Remove " + word + " ?",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, null);
                    if (res == JOptionPane.OK_OPTION) {
                        SwingWorker<Void, Void> removeWordWorker = new RemoveWordWorker(word);
                        removeWordWorker.execute();
                    }
                }
            }
        });

        /*Keyboard shortcuts*/
        grid.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int i1 = e.getKeyCode();
                if (i1 == KeyEvent.VK_ENTER) {
                    find_btn.doClick();
                } else if (i1 == KeyEvent.VK_ESCAPE) {
                    for (JToggleButton letter_btn : letter_btns) {
                        letter_btn.setSelected(false);
                    }
                } else if (i1 >= 'A' && i1 <= 'Z') {
                    for (JToggleButton letter_btn : letter_btns) {
                        if (letter_btn.getText().charAt(0) == i1) {
                            letter_btn.setSelected(!letter_btn.isSelected());
                        }
                    }
                } else if (i1 == KeyEvent.VK_CONTROL) {
                    ctrlPressing = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    ctrlPressing = false;
                }
            }
        });
    }

    private void buildUI() {
        clear_btn = new JButton("Clear selected");
        find_btn = new JButton("Find words!");

        clear_btn.setEnabled(false);
        find_btn.setEnabled(false);
        grid.setLayout(new GridLayout(5, 5));
        grid.setBorder(new EmptyBorder(0, 60, 0, 60));

        resultList.setVisibleRowCount(20);
        resultList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        resultList.setFixedCellWidth(300);

        JPanel btns = new JPanel();
        btns.add(clear_btn);
        btns.add(Box.createRigidArea(new Dimension(10, 0)));
        btns.add(find_btn);

        JScrollPane scrollPane = new JScrollPane(resultList);
        scrollPane.setPreferredSize(new Dimension(350, 350));
        scrollPane.setMaximumSize(new Dimension(350, 350));

        JPanel wordResultPanel = new JPanel();
        wordResultPanel.setPreferredSize(new Dimension(350, 400));
        wordResultPanel.setBorder(BorderFactory.createTitledBorder("Word result"));
        wordResultPanel.add(scrollPane);

        Border padding = new EmptyBorder(0, 0, 0, 0);
        Border margin = BorderFactory.createTitledBorder("Selected grid");
        setBorder(new CompoundBorder(margin, padding));

        GroupLayout layout = new GroupLayout(this);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        this.setLayout(layout);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(btns)
                        .addComponent(grid))
                .addComponent(scrollPane));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(btns)
                        .addComponent(grid))
                .addComponent(scrollPane));
    }

    /**
     * Show toggle clickable game grid
     *
     * @param letters 25 letters of the grid
     */
    @Override
    public void onGameRecordDoubleClicked(String letters) {
        grid.removeAll();

        freqG.clear();
        for (int i = 'A'; i <= 'Z'; i++) {
            freqG.put((char) i, 0);
        }

        for (int i = 0; i < letter_btns.length; i++) {
            int count = freqG.getOrDefault(letters.charAt(i), 0);
            freqG.put(letters.charAt(i), ++count);

            letter_btns[i] = new JToggleButton(String.valueOf(letters.charAt(i)));
            letter_btns[i].setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
            letter_btns[i].setBackground(Color.white);
            letter_btns[i].setForeground(Color.darkGray);
            letter_btns[i].setUI(new MetalToggleButtonUI() {
                @Override
                protected Color getSelectColor() {
                    return new Color(0xCCB4DCFA, true);
                }
            });

            /*let keyboard shortcuts focus on grid*/
            letter_btns[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    grid.requestFocusInWindow();
                }
            });

            /*toggle letter button on mouse over*/
            int finalI = i;
            letter_btns[i].addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (ctrlPressing) {
                        letter_btns[finalI].setSelected(true);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!ctrlPressing) {
                        letter_btns[finalI].setSelected(letter_btns[finalI].isSelected());
                    } else {
                        letter_btns[finalI].setSelected(!letter_btns[finalI].isSelected());
                    }
                }
            });

            grid.add(letter_btns[i]);
        }

        clear_btn.setEnabled(true);
        find_btn.setEnabled(true);
        grid.requestFocusInWindow();
        updateUI();
    }

    private class FindWordsWorker extends SwingWorker<List<String>, Void> {
        @Override
        protected List<String> doInBackground() throws Exception {
            return DbConnector.findMatchingWords(freqG, freq);
        }

        @Override
        protected void done() {
            try {
                wordResult.addAll(get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            find_btn.setEnabled(true);

            // setup resultList Model
            DefaultListModel<String> wordsResultModel = new DefaultListModel<>();
            for (String word : wordResult) {
                wordsResultModel.addElement(word);
            }
            resultList.setModel(wordsResultModel);
        }

    }

    private class RemoveWordWorker extends SwingWorker<Void, Void> {

        private final String word;

        private RemoveWordWorker(String word) {
            this.word = word;
        }

        @Override
        protected Void doInBackground() throws Exception {
            DbConnector.removeWordFromDictionary(word);
            return null;
        }

        @Override
        protected void done() {
            System.out.println(word + " removed.");
        }
    }

    /**
     * Retrieve word definition via Oxford Dictionary API
     */
    private class DefinitionWorker extends SwingWorker<String, Void> {
        private final String word;
        private final String queryType;
        private final String language = "en";
        private final String app_id = "6b874550";
        private final String app_key = "2f871c9f331916a1c34945113e15cc72";
        Gson gson = new Gson();

        /**
         * @param word      the word or root word to be queried
         * @param queryType "inflections" or "entries"
         */
        private DefinitionWorker(String word, String queryType) {
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
                    System.out.println(line);
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
                    JsonArray senses = ((JsonObject) entry).get("senses").getAsJsonArray();
                    //if no definition, use derivation
                    if (senses.size() == 0) {
                        String derivation = ((JsonObject) lex).get("derivativeOf").getAsJsonArray().get(1).getAsJsonObject().get("text").getAsString();
                        doc.insertBeforeStart(doc.getElement(lexCategory), "<li>" + derivation + "</li>");
                    } else {
                        // display each definition
                        for (JsonElement sense : senses) {
                            String definition = ((JsonObject) sense).get("definitions").getAsJsonArray().get(0).getAsString();
                            doc.insertBeforeStart(doc.getElement(lexCategory), "<li>" + definition + "</li>");
                        }
                    }
                }
            }
            JsonObject firstEntry = lexical.get(0).getAsJsonObject().get("entries").getAsJsonArray().get(0).getAsJsonObject();
            if (firstEntry.has("etymologies")) {
                String etymology = firstEntry.get("etymologies").getAsJsonArray().get(0).getAsString();
                doc.insertBeforeEnd(doc.getElement("def"), "<h4>Origin</h4><ul><li>" + etymology + "<ul></li>");
            }
        }

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
            definitionPanel.setText("<html><head></head><body><h2>" + word + "</h2>from the root word <strong>" + inflectionOf + "</strong>" +
                    "<div id='def'></div><hr><p>Definition powered by " + apiProvider + "</p></body></html>");

            /*chain of api query to get the definition of root word*/
            SwingWorker<String, Void> getDefinitionWorker = new DefinitionWorker(inflectionOf, "entries");
            getDefinitionWorker.execute();
        }
    }
}


