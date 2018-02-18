package swing_app;

import DbConnector.DbConnector;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.MetalToggleButtonUI;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;


public class LetterGridPanel extends JPanel implements GamesTableSelectedListener {

    private JPanel grid = new JPanel();
    private JEditorPane definitionPanel = new JEditorPane();
    private JButton clear_btn;
    private JButton find_btn;
    private JToggleButton[] letter_btns = new JToggleButton[25];
    private final JList<String> resultList = new JList<>();

    private Map<Character, Integer> freq = new TreeMap<>();     // selected letters (lower bound of query)
    private Map<Character, Integer> freqG = new TreeMap<>();    // grid letters (upper bound of query)
    private List<String> wordResult = new ArrayList<>();
    private boolean ctrlPressing = false;
    private boolean isLetterSelected;


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

        /*Display definition panel, opt to remove selected word from dictionary*/
        resultList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = ((JList) e.getSource()).getSelectedIndex();
                    String word = resultList.getModel().getElementAt(row);

                    SwingWorker<String, Void> getDefinitionWorker = new DefinitionWorker(definitionPanel, word, "inflections");
                    getDefinitionWorker.execute();

                    JScrollPane scrollPane = new JScrollPane(definitionPanel);
                    scrollPane.setPreferredSize(new Dimension(600, 400));
                    scrollPane.setMaximumSize(new Dimension(600, 600));

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
            // put grid letter frequency to map
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
            letter_btns[i].addActionListener(e -> grid.requestFocusInWindow());

            /*toggle letter button on mouse over*/
            int finalI = i;
            letter_btns[i].addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    isLetterSelected = !isLetterSelected;
                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    isLetterSelected = letter_btns[finalI].isSelected();
                    letter_btns[finalI].setSelected(ctrlPressing);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!ctrlPressing)
                    letter_btns[finalI].setSelected(isLetterSelected);
                }
            });

            grid.add(letter_btns[i]);
        }

        clear_btn.setEnabled(true);
        find_btn.setEnabled(true);
        grid.requestFocusInWindow();
        updateUI();
    }


    /**
     * Find matching words
     */
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

    /**
     * Remove Word From Dictionary
     */
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

}


