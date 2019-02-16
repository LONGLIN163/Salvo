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
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer;

    private Integer turn;

    @ElementCollection
    @Column(name="fireLocations")
    private List<String> fireLocations=new ArrayList<String> ();
    public Salvo(){ }
    public Salvo(Integer turn,List<String> fireLocations,GamePlayer gamePlayer){
        this.turn=turn;
        this.fireLocations=fireLocations;
        this.gamePlayer=gamePlayer;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public Integer getTurn() {
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
