package swing_app;

import ORM_jooq.Tables;
import ORM_jooq.tables.DbEnglishGrids;
import ORM_jooq.tables.DbEnglishWords;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class DbConnector {
    private static Properties dbProps = new Properties();

    public static void initialze() {
        try (FileInputStream fIn = new FileInputStream("web/WEB-INF/mysql.properties")) {
            dbProps.load(fIn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetch games info from database
     * @param letterpressGames A list to store games fetched from db
     */
    public static void fetchGridFromDB(List<LetterpressGame> letterpressGames) {

        try (Connection conn = DriverManager.getConnection(dbProps.getProperty("url"), dbProps)) {
            DSLContext create = DSL.using(conn, SQLDialect.MYSQL);

            final DbEnglishGrids GRIDS = Tables.DB_ENGLISH_GRIDS;
            Result<Record3<String, String, Date>> result = create
                    .select(GRIDS.PLAYERNAME,
                            GRIDS.GRID,
                            GRIDS.OPENTIME)
                    .from(GRIDS)
                    .where(GRIDS.ISFINISHED.isFalse())
                    .fetch();

            if (result.size() > 0) {
                for (Record3<String, String, Date> record3 : result) {
                    letterpressGames.add(new LetterpressGame(
                            record3.component1(),
                            record3.component2(),
                            record3.component3()
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Add new record to database
     * @param newPlayerName newPlayerName
     * @param newLetterGrid newLetterGrid
     */
    public static void addNewGameToDB(String newPlayerName, String newLetterGrid) {
        try (Connection conn = DriverManager.getConnection(dbProps.getProperty("url"), dbProps)) {
            DSLContext create = DSL.using(conn, SQLDialect.MYSQL);

            final DbEnglishGrids GRIDS = Tables.DB_ENGLISH_GRIDS;
            create.insertInto(GRIDS, GRIDS.PLAYERNAME, GRIDS.GRID, GRIDS.OPENTIME)
                    .values(newPlayerName, newLetterGrid, new Date(Calendar.getInstance().getTime().getTime()))
                    .execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> findMatchingWords(Map<Character, Integer> freqG, Map<Character, Integer> freq) {
        List<String> foundWords = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbProps.getProperty("url"), dbProps)) {
            DSLContext create = DSL.using(conn, SQLDialect.MYSQL);
            final DbEnglishWords TABLE = Tables.DB_ENGLISH_WORDS;
            Result<Record1<String>> result = create
                    .select(TABLE.WORD)
                    .from(TABLE)
                    .where(TABLE.VALID.eq(1))
                    .and(TABLE.NUMBER_OF_A.between(freq.get('A'), freqG.get('A')))
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

            System.out.println(result.size() + " words found.");

            for (Record1<String> record : result) {
                foundWords.add(record.component1());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return foundWords;
    }

    public static void removeWordFromDictionary(String word) {
        try (Connection conn = DriverManager.getConnection(dbProps.getProperty("url"), dbProps)) {
            DSLContext create = DSL.using(conn, SQLDialect.MYSQL);

            final DbEnglishWords DICTIONARY = Tables.DB_ENGLISH_WORDS;
            create.update(DICTIONARY)
                    .set(DICTIONARY.VALID, 0)
                    .where(DICTIONARY.WORD.eq(word.toLowerCase()))
                    .execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeGame(String gridLetters) {
        try (Connection conn = DriverManager.getConnection(dbProps.getProperty("url"), dbProps)) {
            DSLContext create = DSL.using(conn, SQLDialect.MYSQL);

            final DbEnglishGrids GRID = Tables.DB_ENGLISH_GRIDS;
            create.update(GRID)
                    .set(GRID.ISFINISHED, 1)
                    .where(GRID.GRID.eq(gridLetters))
                    .execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
