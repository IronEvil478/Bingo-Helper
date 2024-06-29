package bingohelper;

import java.util.List;

public class Event {
    private String name;
    private String webhook;
    private List<Team> teams;
    private Board board;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getWebhook() { return webhook; }
    public void setWebhook(String webhook) { this.webhook = webhook; }
    public List<Team> getTeams() { return teams; }
    public void setTeams(List<Team> teams) { this.teams = teams; }
    public Board getBoard() { return board; }
    public void setBoard(Board board) { this.board = board; }
}

class Team {
    private String name;
    private List<String> players;
    private int current_tile;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getPlayers() { return players; }
    public void setPlayers(List<String> players) { this.players = players; }
    public int getCurrentTile() { return current_tile; }
    public void setCurrentTile(int current_tile) { this.current_tile = current_tile; }
}

class Board {
    private List<Tile> tiles;

    // Getters and Setters
    public List<Tile> getTiles() { return tiles; }
    public void setTiles(List<Tile> tiles) { this.tiles = tiles; }
}

class Tile {
    private List<String> items;
    private Integer go_to;

    // Getters and Setters
    public List<String> getItems() { return items; }
    public void setItems(List<String> items) { this.items = items; }
    public Integer getGoTo() { return go_to; }
    public void setGoTo(Integer go_to) { this.go_to = go_to; }
}

