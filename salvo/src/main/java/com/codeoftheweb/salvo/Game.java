package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import javax.persistence.*;
import java.util.*;

@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    //Link with gamePlayer
    @OneToMany(mappedBy="game", fetch= FetchType.EAGER)
    private Set<GamePlayer> gamePlayers = new HashSet<> ();
    public void addGamePlayer(GamePlayer gamePlayer) {
        gamePlayer.setGame(this);
        gamePlayers.add(gamePlayer);
    }

    //Link with Score
    @OneToMany(mappedBy="game", fetch= FetchType.EAGER)
    private Set<Score> scores = new HashSet<> ();
    public void addScore(Score score) {
        score.setGame(this);
        scores.add(score);
    }

    //define the variables.
    private Date date;

    //define a default (no-argument) constructor for JPA.
    public Game(){ }

    //create game constructor.
    public Game(Date date){
        this.date=date;
    }

    //create methods to set and get info.
    public Date getDate() {
        return this.date;
    }

    public Long getId() {
        return id;
    }

    public Set<GamePlayer> getGamePlayer() {
        return gamePlayers;
    }

}
