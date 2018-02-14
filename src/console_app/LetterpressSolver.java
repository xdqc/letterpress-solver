package console_app;

import ORM_jooq.Tables;
import ORM_jooq.tables.DbEnglishWords;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class LetterpressSolver {

    public static void main(String[] args) {
        new LetterpressSolver().start();
    }

    private void start() {
        Properties dbProps = new Properties();

        try (FileInputStream fIn = new FileInputStream("web/WEB-INF/mysql.properties")) {
            dbProps.load(fIn);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Welcome to the Letterpress Solver!");
        while (true) {
            System.out.println("Please select an option from the following:\n" +
                    "1. Add new game\n" +
                    "2. Find existing game\n" +
                    "3. Delete game\n" +
                    "4. Exit\n");

            String userChoice = Keyboard.readInput();
            assert userChoice != null;
            switch (userChoice) {
                case "1":
                    addNewGame(dbProps);
                    continue;
                case "2":
                    findGame(dbProps);
                    continue;
                case "3":
                    delete(dbProps);
                    continue;
                case "4":
                    break;
                default:
                    continue;
            }
            break;
        }
    }

    private void delete(Properties dbProps) {
        findGame(dbProps);

    }

    /**
     * Add new record to game grid db
     *
     * @param dbProps db properties
     */
    private void addNewGame(Properties dbProps) {
        System.out.println("Please enter the player name:");
        String playerName = Keyboard.readInput();
        System.out.println("Please enter the letter grid:");
        String letterGrid = Keyboard.readInput();

        try (Connection conn = DriverManager.getConnection(dbProps.getProperty("url"), dbProps)) {
            DSLContext create = DSL.using(conn, SQLDialect.MYSQL);

            assert letterGrid != null;
            create.insertInto(Tables.DB_ENGLISH_GRIDS,
                    Tables.DB_ENGLISH_GRIDS.PLAYERNAME,
                    Tables.DB_ENGLISH_GRIDS.GRID,
                    Tables.DB_ENGLISH_GRIDS.OPENTIME)
                    .values(playerName, letterGrid.toUpperCase(), new Date(Calendar.getInstance().getTime().getTime()))
                    .execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Find a grid
     *
     * @param dbProps db properties
     */
    private void findGame(Properties dbProps) {
        System.out.println("Please enter the start letters:");
        String letterGrid = Keyboard.readInput();

        try (Connection conn = DriverManager.getConnection(dbProps.getProperty("url"), dbProps)) {
            DSLContext create = DSL.using(conn, SQLDialect.MYSQL);

            assert letterGrid != null;
            Result<Record3<String, String, Date>> result = create
                    .select(Tables.DB_ENGLISH_GRIDS.PLAYERNAME,
                            Tables.DB_ENGLISH_GRIDS.GRID,
                            Tables.DB_ENGLISH_GRIDS.OPENTIME)
                    .from(Tables.DB_ENGLISH_GRIDS)
                    .where(Tables.DB_ENGLISH_GRIDS.ISFINISHED.isFalse()
                            .and(Tables.DB_ENGLISH_GRIDS.GRID.startsWith(letterGrid.toUpperCase())))
                    .fetch();

            if (result.size() > 0) {
                for (Record3<String, String, Date> record : result) {
                    System.out.println(String.format("%s %s %s",
                            record.component1(), record.component2(), record.component3()));
                }

                Record3<String, String, Date> foundRecord;

                do {
                    if (result.size() == 1){
                        foundRecord = result.get(0);
                    } else {
                        System.out.println("Enter more letters:");
                        letterGrid = Keyboard.readInput();
                        assert letterGrid != null;
                        String finalLetterGrid = letterGrid.toUpperCase();
                        foundRecord = result.stream()
                                .filter(r -> r.component2().startsWith(finalLetterGrid))
                                .findFirst().orElse(null);
                    }
                } while(foundRecord == null);

                // Do solving
                String neededLetters;
                while (true) {
                    System.out.println("Please enter needed letters:");
                    neededLetters = Keyboard.readInput();
                    assert neededLetters != null;
                    if (neededLetters.isEmpty())
                        break;

                    solveGame(foundRecord.component2().replace(" ", "").toUpperCase(),
                            neededLetters.replace(" ", "").toUpperCase(),
                            dbProps);
                }

            } else {
                System.out.println("No game found...");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Find words that meet certain criteria
     *
     * @param foundGrid     grid to work with
     * @param neededLetters words should contain these chars
     */
    private void solveGame(String foundGrid, String neededLetters, Properties dbProps) {
        System.out.println(foundGrid + " " + neededLetters);
        Map<Character, Integer> freq = new TreeMap<>();
        Map<Character, Integer> freqG = new TreeMap<>();

        for (int i = 'A'; i <= 'Z'; i++) {
            freq.put((char)i, 0);
            freqG.put((char)i, 0);
        }

        for (char c : neededLetters.toCharArray()) {
            int count = freq.getOrDefault(c,0);
            freq.put(c, ++count);
        }

        for (char c : foundGrid.toCharArray()) {
            int count = freqG.getOrDefault(c,0);
            freqG.put(c, ++count);
        }

        try (Connection conn = DriverManager.getConnection(dbProps.getProperty("url"), dbProps)) {
            DSLContext create = DSL.using(conn, SQLDialect.MYSQL);
            final DbEnglishWords TABLE = Tables.DB_ENGLISH_WORDS;
            Result<Record1<String>> result = create
                    .select(TABLE.WORD)
                    .from(TABLE)
                    .where(TABLE.NUMBER_OF_A.between(freq.get('A'), freqG.get('A')))
                    .and(TABLE.NUMBER_OF_B.between(freq.get('B'), freqG.get('B')))
                    .and(TABLE.NUMBER_OF_C.between(freq.get('C'), freqG.get('C')))
                    .and(TABLE.NUMBER_OF_D.between(freq.get('D'), freqG.get('D')))
                    .and(TABLE.NUMBER_OF_E.between(freq.get('E'), freqG.get('E')))
                    .and(TABLE.NUMBER_OF_F.between(freq.get('F'), freqG.get('F')))
                    .and(TABLE.NUMBER_OF_G.between(freq.get('G'), freqG.get('G')))
                    .and(TABLE.NUMBER_OF_H.between(freq.get('H'), freqG.get('H')))
                    .and(TABLE.NUMBER_OF_I.between(freq.get('I'), freqG.get('I')))
                    .and(TABLE.NUMBER_OF_J.between(freq.get('J'), freqG.get('J')))
                    .and(TABLE.NUMBER_OF_K.between(freq.get('K'), freqG.get('K')))
                    .and(TABLE.NUMBER_OF_L.between(freq.get('L'), freqG.get('L')))
                    .and(TABLE.NUMBER_OF_M.between(freq.get('M'), freqG.get('M')))
                    .and(TABLE.NUMBER_OF_N.between(freq.get('N'), freqG.get('N')))
                    .and(TABLE.NUMBER_OF_O.between(freq.get('O'), freqG.get('O')))
                    .and(TABLE.NUMBER_OF_P.between(freq.get('P'), freqG.get('P')))
                    .and(TABLE.NUMBER_OF_Q.between(freq.get('Q'), freqG.get('Q')))
                    .and(TABLE.NUMBER_OF_R.between(freq.get('R'), freqG.get('R')))
                    .and(TABLE.NUMBER_OF_S.between(freq.get('S'), freqG.get('S')))
                    .and(TABLE.NUMBER_OF_T.between(freq.get('T'), freqG.get('T')))
                    .and(TABLE.NUMBER_OF_U.between(freq.get('U'), freqG.get('U')))
                    .and(TABLE.NUMBER_OF_V.between(freq.get('V'), freqG.get('V')))
                    .and(TABLE.NUMBER_OF_W.between(freq.get('W'), freqG.get('W')))
                    .and(TABLE.NUMBER_OF_X.between(freq.get('X'), freqG.get('X')))
                    .and(TABLE.NUMBER_OF_Y.between(freq.get('Y'), freqG.get('Y')))
                    .and(TABLE.NUMBER_OF_Z.between(freq.get('Z'), freqG.get('Z')))
                    .orderBy(TABLE.LENGTH.desc())
                    .fetch();

            System.out.println(result.size());

            for (Record1<String> record : result) {
                System.out.println(String.format("%-20s", record.component1()));
            }
            System.out.println();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
