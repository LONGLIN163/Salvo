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
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer;

    private String shipType;

    @ElementCollection
    @Column(name="shipLocations")
    private List<String> locations=new ArrayList<String> ();

    public Ship(){ }

    public Ship(String shipType,List<String> locations,GamePlayer gamePlayer){
        this.shipType=shipType;
        this.locations=locations;
        this.gamePlayer=gamePlayer;
    }

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
