package swing_app;

import java.io.Serializable;
import java.util.Date;

public class LetterpressGame implements Serializable{


    private String playerName;
    private String grid;
    private Date openDate;
    private Boolean finished;

    public LetterpressGame(String playerName, String grid, Date openDate) {
        this.playerName = playerName;
        this.grid = grid;
        this.openDate = openDate;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getGrid() {
        return grid;
    }

    public Date getOpenDate() {
        return openDate;
    }

    public Boolean getFinished() {
        return finished;
    }

    private void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    private void setGrid(String grid) {
        this.grid = grid;
    }

    private void setOpenDate(Date openDate) {
        this.openDate = openDate;
    }

    public void setFinished(Boolean finished){
        this.finished = finished;
    }

    @Override
    public String toString() {
        return String.format("%-16s %30s %-4s", playerName, grid, openDate);
    }


}
