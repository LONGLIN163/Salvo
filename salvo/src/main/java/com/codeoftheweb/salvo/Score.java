package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


@Entity
public class Score {
    //create Score id column
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    //Each row of the game players data table has a GamePlayer id(their own id).
    private Long id;

    //got player id column
    @ManyToOne(fetch = FetchType.EAGER)
    //Each row of the game players data table has a player ID in Column player_id.
    @JoinColumn(name="player_id")
    private Player player;

    //got game id column
    @ManyToOne(fetch = FetchType.EAGER)
    //Each row of the game players data table has a game ID in Column game_id.
    @JoinColumn(name="game_id")
    private Game game;

    //difine a Score variable
    private Double score;

    //difine a finishDate variable
    private Date finishDate;

    //declare a constructor sin parameter
    public Score(){ }
    public Score(Date finishDate,Game game,Player player, Double score ){
        this.finishDate=finishDate;
        this.player=player;
        this.game=game;
        this.score=score;
    }

    public Long getId() {
        return id;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getScore() {
        return score;
    }
}
