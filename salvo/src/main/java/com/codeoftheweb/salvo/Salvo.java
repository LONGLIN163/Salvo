package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.*;
import java.util.*;
@Entity
public class Salvo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;


    @ManyToOne(fetch = FetchType.EAGER)
    //Each row of the game players data table has a game ID in Column game_id.
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer;

    //define the turns variables.
    private int turn;
    //private Map<String,Map<String,Object>> turn=new HashMap<>();

    //define the fire locations variables.create lists of embeddable objects.
    @ElementCollection
    @Column(name="fireLocations")
    private List<String> fireLocations=new ArrayList<String> ();

    //define a default (no-argument) constructor for JPA.
    public Salvo(){ }

    //create Salvo constructor.
    public Salvo(int turn,List<String> fireLocations,GamePlayer gamePlayer){
        this.turn=turn;
        this.fireLocations=fireLocations;
        this.gamePlayer=gamePlayer;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public int getTurn() {
        return turn;
    }

    public void setFireLocations(List<String> fireLocations) {
        this.fireLocations = fireLocations;
    }

    public List<String> getFireLocations() {
        return fireLocations;
    }

    public Long getId() {
        return id;
    }

    @JsonIgnore
    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public void setGame(Game game) {
    }
}
