package swing_app;

import DbConnector.DbConnector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

class GamesTablePanel extends JPanel {

    private List<GamesTableSelectedListener> gamesTableSelectedListeners = new ArrayList<>();

    private JButton addGame_btn;
    private JButton fetchGame_btn;
    private JButton remove_btn;
    private JTable gameInfo_table;
    private String newPlayerName;
    private String newLetterGrid;

    GamesTablePanel() {

        buildUI();

        /*Get all active game records*/
        fetchGame_btn.addActionListener(e -> {
            SwingWorker<List<LetterpressGame>, Void> worker = new DBFetchWorker();

            gameInfo_table.removeAll();
            gameInfo_table.setEnabled(false);
            fetchGame_btn.setEnabled(false);
            addGame_btn.setEnabled(false);
            remove_btn.setEnabled(false);
            worker.execute();
        });

        /*Add a new game record*/
        addGame_btn.addActionListener(e -> {
            JTextField playerName = new JTextField(10);
            JTextField letterGrid = new JTextField(30);
            playerName.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            letterGrid.setFont(LetterpressSolverGUI.customFont);

            JPanel inputPanel = new JPanel();
            inputPanel.add(new JLabel("Player Name:"));
            inputPanel.add(playerName);
            inputPanel.add(Box.createHorizontalStrut(15)); // a spacer
            inputPanel.add(new JLabel("Letter Grid:"));
            inputPanel.add(letterGrid);

            int result = JOptionPane.showConfirmDialog(null, inputPanel,
                    "Add new game", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION){
                newPlayerName = playerName.getText();
                newLetterGrid = letterGrid.getText().toUpperCase();
                fetchGame_btn.setEnabled(false);
                addGame_btn.setEnabled(false);
                remove_btn.setEnabled(false);
                SwingWorker<Void, Void> worker = new DBAddWorker();
                worker.execute();
            }

        });

        /*Mark a game record as finished*/
        remove_btn.addActionListener(e -> {
            remove_btn.setEnabled(false);
            addGame_btn.setEnabled(false);
            fetchGame_btn.setEnabled(false);
            int row = gameInfo_table.getSelectedRow();
            String letters = (String)gameInfo_table.getModel().getValueAt(row , 1);
            SwingWorker<Void, Void> dbRemoveWorker = new DBRemoveWorker(letters);
            dbRemoveWorker.execute();
        });

        /*Select a game record and show grid in LetterGridPanel*/
        gameInfo_table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount()==2){
                    // index of the clicked row in view
                    int row = ((JTable)e.getSource()).rowAtPoint(e.getPoint());
                    //return an index of the row in the model based on its index in the view
                    row = gameInfo_table.getRowSorter().convertRowIndexToModel(row);
                    String letters = (String)gameInfo_table.getModel().getValueAt(row , 1);
                    //Pass selected game letters to
                    gamesTableSelectedListeners.forEach(g -> g.onGameRecordDoubleClicked(letters.replace(" ","")));
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                remove_btn.setEnabled(true);
            }
        });

        // fetch games on app start
        fetchGame_btn.doClick();
    }

    private void buildUI() {
        fetchGame_btn = new JButton("Fetch Game");
        addGame_btn = new JButton("Add Game");
        remove_btn = new JButton("Remove Game");
        remove_btn.setEnabled(false);
        gameInfo_table = new JTable();
        gameInfo_table.setModel(new DefaultTableModel());
        gameInfo_table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(gameInfo_table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Available games"));


        GroupLayout layout = new GroupLayout(this);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        setLayout(layout);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addGame_btn)
                        .addGap(20)
                        .addComponent(fetchGame_btn)
                        .addGap(20)
                        .addComponent(remove_btn))
                    .addComponent(scrollPane)
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(addGame_btn)
                        .addComponent(fetchGame_btn)
                        .addComponent(remove_btn))
                    .addComponent(scrollPane)
        );
    }

    public void addGamesTableSelectedListener(GamesTableSelectedListener listener){
        this.gamesTableSelectedListeners.add(listener);
    }

    private class DBFetchWorker extends SwingWorker<List<LetterpressGame>, Void> {

        @Override
        protected List<LetterpressGame> doInBackground() throws Exception {
            List<LetterpressGame> letterpressGames = new ArrayList<>();

            DbConnector.fetchGridFromDB(letterpressGames);
            return letterpressGames;
        }

        @Override
        protected void done() {
            fetchGame_btn.setEnabled(true);
            addGame_btn.setEnabled(true);
            gameInfo_table.setEnabled(true);
            remove_btn.setEnabled(false);

            try {
                TableModel tableModel = new GameTableModel(get());
                gameInfo_table.setModel(tableModel);
                gameInfo_table.getColumnModel().getColumn(0).setPreferredWidth(200);
                gameInfo_table.getColumnModel().getColumn(1).setPreferredWidth(300);
                //sort table by click header
                gameInfo_table.setRowSorter(new TableRowSorter<>(gameInfo_table.getModel()));

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private class DBAddWorker extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            DbConnector.addNewGameToDB(newPlayerName, newLetterGrid);
            return null;
        }

        @Override
        protected void done() {
            fetchGame_btn.setEnabled(true);
            addGame_btn.setEnabled(true);
            fetchGame_btn.doClick();
        }
    }

    private class DBRemoveWorker extends SwingWorker<Void, Void> {

        private final String grid;

        DBRemoveWorker(String letters) {
            this.grid = letters;
        }

        @Override
        protected Void doInBackground() throws Exception {
            DbConnector.removeGame(grid);
            return null;
        }

        @Override
        protected void done() {
            addGame_btn.setEnabled(true);
            fetchGame_btn.setEnabled(true);
        }
    }
}
