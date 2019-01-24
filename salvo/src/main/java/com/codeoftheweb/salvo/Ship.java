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
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    //Each row of the game players data table has a game ID in Column game_id.
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer;


    //define the shipType variables.
    private String shipType;

    //define the locations variables.create lists of embeddable objects.
    @ElementCollection
    @Column(name="shipLocations")
    private List<String> locations=new ArrayList<String> ();

    //define a default (no-argument) constructor for JPA.
    public Ship(){ }

    //create ship constructor.
    public Ship(String shipType,List<String> locations,GamePlayer gamePlayer){
        this.shipType=shipType;
        this.locations=locations;
        this.gamePlayer=gamePlayer;
    }

    //create methods to set and get info.
    public void setShipType(String shipType) {
        this.shipType = shipType;
    }

    public String getShipType() {
        return shipType;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public List<String> getLocations() {
        return locations;
    }

    public Long getId() {
        return id;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public void setGame(Game game) {
    }
}
