package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
public class GamePlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)

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

    @JsonIgnore
    public Game getGame() {
        return game;
    }

    @JsonIgnore
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

    public void addSalvo(Salvo salvo){
        salvo.setGamePlayer (this);
        this.salvos.add(salvo);
    }
}
