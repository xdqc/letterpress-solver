package swing_app;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;

public class LetterpressSolverGUI extends JFrame {
    private static final long serialVersionUID = 1L;
	static Font customFont;

    /**
     * Constructs a new frame that is initially invisible.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.
     *
     * @throws HeadlessException if GraphicsEnvironment.isHeadless()
     *                           returns true.
     * @see GraphicsEnvironment#isHeadless
     * @see Component#setSize
     * @see Component#setVisible
     * @see JComponent#getDefaultLocale
     */
    public LetterpressSolverGUI() throws HeadlessException {

    }


    public static void main(String[] args) {
        new LetterpressSolverGUI().start();
    }

    @Nullable
    private Font loadFont() {
        InputStream is = LetterpressSolverGUI.class.getResourceAsStream("/Fonts/consola.ttf");
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            return font.deriveFont(16f);

        } catch (FontFormatException | IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    private void start() {
        customFont = loadFont();
        DbConnector.initialze();
        setTitle("Letterpress Solver");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);

        GamesTablePanel gamesTablePanel = new GamesTablePanel();
        LetterGridPanel letterGridPanel = new LetterGridPanel();

        /* Wire-up */
        gamesTablePanel.addGamesTableSelectedListener(letterGridPanel);

        SwingUtilities.invokeLater(() -> buildGUI(gamesTablePanel, letterGridPanel));
    }

    private void buildGUI(JPanel gamesTableView, JPanel letterGridView){

        gamesTableView.setPreferredSize(new Dimension(800, 240));
        letterGridView.setPreferredSize(new Dimension(800, 400));


        /* Create main pane for the application. */
        JPanel mainPane = new JPanel();
        mainPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
        mainPane.add(gamesTableView);
        mainPane.add(Box.createRigidArea(new Dimension(10, 0)));
        mainPane.add(letterGridView);
        add(mainPane);

		/* Quit the program in response to the user closing the window. */
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

}
