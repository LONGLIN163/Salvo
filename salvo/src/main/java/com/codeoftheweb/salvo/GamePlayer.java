package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collector;

@Entity

// create a Java class GamePlayer with an associated table to represent an instance of a specific player playing a specific game.
// or the players play the game.

public class GamePlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    //Each row of the game players data table has a GamePlayer id(their own id).
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    //Each row of the game players data table has a player ID in Column player_id.
    @JoinColumn(name="player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)
    //Each row of the game players data table has a game ID in Column game_id.
    @JoinColumn(name="game_id")
    private Game game;

    @OneToMany(mappedBy="gamePlayer", fetch= FetchType.EAGER)
    private Set<Ship> ships = new HashSet<> ();

    @OneToMany(mappedBy="gamePlayer", fetch= FetchType.EAGER)
    private Set<Salvo> salvos = new HashSet<> ();

    private Date joinTime;

    public GamePlayer(){ }
    public GamePlayer(Date joinTime,Game game,Player player ){
        this.player=player;
        this.game=game;
        this.joinTime=joinTime;
    }

    public Date getDate() {
        return this.joinTime;
    }

    public Long getId() {
        return id;
    }

    public Game getGame() {
        return game;
    }

    public Player getPlayer() {
        return player;
    }

    public void setGame(Game game) {
    }

    public void setPlayer(Player player) {
    }

    public Set<Ship> getShips() {
        return ships;
    }

    public Set<Salvo> getSalvos() {
        return salvos;
    }


}
