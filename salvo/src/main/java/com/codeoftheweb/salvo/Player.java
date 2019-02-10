package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import javax.persistence.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//@Entity tells Spring to create a player table for this class.it will be stored in the database;
//@Entity class will be like a row in database,and Repository class will be like a table:row and table.
@Entity
public class Player {

    @Id//@Id tell in database table,hold the unique id.

    //id will be generated when the instance is created.By who i dont know.?????????
    //below two annotation tell JPA to use whatever ID generator is provided by the database system.
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")

    private Long id;

    //link to the gamePlayer
    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    Set<GamePlayer> gamePlayers;
    public void addGamePlayer(GamePlayer gamePlayer) {
        gamePlayer.setPlayer(this);
        gamePlayers.add(gamePlayer);
    }

    //Link with Score
    @OneToMany(mappedBy="player", fetch= FetchType.EAGER)
    private Set<Score> scores = new HashSet<> ();
    public void addScore(Score score) {
        score.setPlayer(this);
        scores.add(score);
    }

    private String userName;

    private String password;

    public Player(){ }

    public Player(String userName,String password) {
        this.userName=userName;
        this.password=password;
    }

     public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public Long getId() {
        return id;
    }

    public Set<Score> getScores() {
        return scores;
    }

    public void getGame() {
    }

    public Score getScore(Game game) {

       return  this.scores.stream().filter(sc -> sc.getGame ().equals (game)).findFirst().orElse(null);
    }

}
