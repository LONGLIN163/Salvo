package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private SalvoRepository salvoRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private ShipRepository shipRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;


    @RequestMapping("/games")
    public Map<String, Object> getAllGamesDetails(Authentication authentication) {
        Map<String, Object> gamesDto = new HashMap<> ();
        if (!isGuest (authentication)) {
            gamesDto.put ("player", makePlayerDto (getCurrentUser (authentication)));
        } else {
            gamesDto.put ("player", "stranger");
        }

        gamesDto.put ("games", gameRepository.findAll ()
                .stream ()
                .map (game -> makeGameDTO (game))
                .collect (Collectors.toList ()));
        return  gamesDto;
    }

    private Map<String, Object> makeGameDTO(Game game) {
        Map<String, Object> gameDto = new HashMap<> ();
        gameDto.put ("id", game.getId ());
        gameDto.put ("createDate", game.getDate ());

        gameDto.put ("gamePlayer", game.getGamePlayer ()
                    .stream ()
                    .sorted ((gp1, gp2) -> gp1.getId ().compareTo (gp2.getId ()))
                    .map (gamePlayer -> makeGamePlayerDTO (gamePlayer))
                    .collect (Collectors.toList ()));

        gameDto.put ("scores", game.getGamePlayer ()
                    .stream ()
                    .filter (gp-> gp.getPlayer ().getScore (gp.getGame ()) != null)
                    .map (gp -> makeScoreDto (gp.getPlayer ().getScore (gp.getGame ())))
                    .collect (Collectors.toSet ()));

        return gameDto;
    }

    private Map<String, Object> makeScoreDto(Score score) {
        Map<String, Object> scoreDto = new HashMap<> ();
            scoreDto.put ("player",score.getPlayer ().getUserName ());
            scoreDto.put ("player_id", score.getPlayer ().getId ());
            scoreDto.put ("scores", score.getScore ());
        return scoreDto;
    }

    private Map<String, Object> makeGamePlayerDTO(GamePlayer gamePlayer) {
        Map<String, Object> gamePlayerDto = new HashMap<> ();
        gamePlayerDto.put ("id", gamePlayer.getId ());
        gamePlayerDto.put ("player", makePlayerDto (gamePlayer.getPlayer ()));
        return gamePlayerDto;
    }

    private Map<String, Object> makePlayerDto(Player player) {
        Map<String, Object> playerDto = new HashMap<> ();
        playerDto.put ("id", player.getId ());
        playerDto.put ("name", player.getUserName ());
        return playerDto;
    }


    @RequestMapping(path ="/game_view/{gpID}",method = RequestMethod.GET)
    public ResponseEntity <Map<String, Object>> getGameView(@PathVariable Long gpID,Authentication authentication) {
        GamePlayer gamePlayer = gamePlayerRepository.getOne (gpID);
        Map<String, Object> someGameInfo = new HashMap<> ();
        //Game someGameInfo=gamePlayer.getGame ();
        if (gamePlayer.getPlayer ().getId ()!=getCurrentUser (authentication).getId ()) {
            return new ResponseEntity<> (responseDto ("error","u r not the ownner of this gamePlayer"), HttpStatus.FORBIDDEN);
        }else {
        someGameInfo.put ("id", gamePlayer.getGame ().getId ());
        someGameInfo.put ("createDate", gamePlayer.getGame ().getDate ());
        someGameInfo.put ("gamePlayers", gamePlayer.getGame ().getGamePlayer ()
                .stream ()
                .sorted ((gp1, gp2) -> gp1.getId ().compareTo (gp2.getId ()))
                .map (gp -> makeGamePlayerDTO (gp))
                .collect (Collectors.toList ()));

        someGameInfo.put ("ships", gamePlayer.getShips ()
                .stream ()
                .map (ship -> makeShipDto (ship))
                .collect (Collectors.toList ()));

        someGameInfo.put ("salvos", gamePlayer.getGame ().getGamePlayer ()
                .stream ()
                .map (gp -> makeSalvoDto (gp))
                .collect (Collectors.toList ()));

        someGameInfo.put("currentGameplayerSalvos",getCurrentGamePlayerSalvoDto(gamePlayer));
        someGameInfo.put("opponentGameplayerSalvos",getOponentGamePlayerSalvoDto(gamePlayer));

            return new ResponseEntity<> (responseDto ("success",someGameInfo), HttpStatus.CREATED);
        }
    }

    private Object getCurrentGamePlayerSalvoDto (GamePlayer gamePlayer){
        Map<String,Object> turns = new HashMap<> ();
            Set<Salvo> currentGamePlayerSalvos=gamePlayer.getSalvos ();

            Set <GamePlayer> gamePlayers = gamePlayer.getGame ().getGamePlayer ();
            GamePlayer opponentGamePlayer = gamePlayers
                .stream ()
                .filter(gp -> !gp.getId ().equals (gamePlayer.getId ())).findFirst ().orElse (null);

            if(!opponentGamePlayer.equals (null)){
                Set<Ship> opponentShips=opponentGamePlayer.getShips ();
                for (Salvo salvo:currentGamePlayerSalvos) {
                    ArrayList<String> carrierHitsPosition=new ArrayList<> ();
                    ArrayList<String> battleshipHitsPosition=new ArrayList<> ();
                    ArrayList<String> submarineHitsPosition=new ArrayList<> ();
                    ArrayList<String> destroyerHitsPosition=new ArrayList<> ();
                    ArrayList<String> patrolBoatHitsPosition=new ArrayList<> ();

                    List<String> currentPlayerFireLocations=salvo.getFireLocations ();
                    for (String OponentFireLocation:currentPlayerFireLocations) {
                        for (Ship ship : opponentShips) {
                            if (ship.getShipType ().equals ("Carrier")) {
                                if (ship.getLocations ().contains (OponentFireLocation.replace ("PL", ""))) {
                                    carrierHitsPosition.add (OponentFireLocation.replace ("PL", ""));
                                }
                            } else if (ship.getShipType ().equals ("Battleship")) {
                                if (ship.getLocations ().contains (OponentFireLocation.replace ("PL", ""))) {
                                    battleshipHitsPosition.add (OponentFireLocation.replace ("PL", ""));
                                }
                            } else if (ship.getShipType ().equals ("Submarine")) {
                                if (ship.getLocations ().contains (OponentFireLocation.replace ("PL", ""))) {
                                    submarineHitsPosition.add (OponentFireLocation.replace ("PL", ""));
                                }
                            } else if (ship.getShipType ().equals ("Destroyer")) {
                                if (ship.getLocations ().contains (OponentFireLocation.replace ("PL", ""))) {
                                    destroyerHitsPosition.add (OponentFireLocation.replace ("PL", ""));
                                }
                            } else {
                                if (ship.getLocations ().contains (OponentFireLocation.replace ("PL", ""))) {
                                    patrolBoatHitsPosition.add (OponentFireLocation.replace ("PL", ""));
                                }
                            }
                        }
                    }

                    Map<String, Object> carrierHits = new HashMap<> ();
                    carrierHits.put ("hitTimes",carrierHitsPosition.size ());
                    carrierHits.put ("shipStatus",shipStatus("Carrier",carrierHitsPosition.size ()));
                    ArrayList<String> carrierHitsPositiontemp=new ArrayList<> ();
                    carrierHitsPositiontemp.addAll (carrierHitsPosition);
                    carrierHits.put ("hitsPosition",carrierHitsPositiontemp);

                    Map<String, Object> battleshipHits = new HashMap<> ();
                    battleshipHits.put ("hitTimes",battleshipHitsPosition.size ());
                    battleshipHits.put ("shipStatus",shipStatus("Battleship",battleshipHitsPosition.size ()));
                    ArrayList<String> battleshipHitsPositiontemp=new ArrayList<> ();
                    battleshipHitsPositiontemp.addAll (battleshipHitsPosition);
                    battleshipHits.put ("hitsPosition",battleshipHitsPositiontemp);

                    Map<String, Object> submarineHits = new HashMap<> ();
                    submarineHits.put ("hitTimes",submarineHitsPosition.size ());
                    submarineHits.put ("shipStatus",shipStatus("Submarine",submarineHitsPosition.size ()));
                    ArrayList<String> submarineHitsPositiontemp=new ArrayList<> ();
                    submarineHitsPositiontemp.addAll (submarineHitsPosition);
                    submarineHits.put ("hitsPosition",submarineHitsPositiontemp);

                    Map<String, Object> destroyerHits = new HashMap<String, Object> ();
                    destroyerHits.put ("hitTimes", destroyerHitsPosition.size ());
                    destroyerHits.put ("shipStatus",shipStatus("Destroyer",destroyerHitsPosition.size ()));
                    ArrayList<String> destroyerHitsPositiontemp=new ArrayList<> ();
                    destroyerHitsPositiontemp.addAll (destroyerHitsPosition);
                    destroyerHits.put ("hitsPosition", destroyerHitsPositiontemp);

                    Map<String, Object> patrolBoatHits = new HashMap<> ();
                    patrolBoatHits.put ("hitTimes",patrolBoatHitsPosition.size ());
                    patrolBoatHits.put ("shipStatus",shipStatus("PatrolBoat",patrolBoatHitsPosition.size ()));
                    ArrayList<String> patrolBoatHitsPositiontemp=new ArrayList<> ();
                    patrolBoatHitsPositiontemp.addAll (patrolBoatHitsPosition);
                    patrolBoatHits.put ("hitsPosition",patrolBoatHitsPositiontemp);

                    Map<String, Object> currentPlayerHits = new HashMap<> ();
                    currentPlayerHits.put ("Carrier", carrierHits);
                    currentPlayerHits.put ("Battleship", battleshipHits);
                    currentPlayerHits.put ("Submarine", submarineHits);
                    currentPlayerHits.put ("Destroyer", destroyerHits);
                    currentPlayerHits.put ("PatrolBoat", patrolBoatHits);

                    currentPlayerHits.put ("shipsLeft", shipsLeft(currentPlayerHits));

                    turns.put ("turn"+salvo.getTurn (),currentPlayerHits );
                }

                Set<String> set=turns.keySet ();
                for(String s:set){
                    Map<String, Object> value= (HashMap<String, Object>) turns.get (s);
                    Set<String> set2=value.keySet ();
                    for(String s2:set2){
                        Object obj=value.get (s2);
                    }
                }

                TreeMap<String, Object> turnsTreeMap = new TreeMap<> (
                        (Comparator<String>) (s1, s2) -> s1.compareTo(s2)
                );
                turnsTreeMap.putAll(turns);

                return  turnsTreeMap;
            }else{
                return "wating";}
        }

    private Object getOponentGamePlayerSalvoDto (GamePlayer gamePlayer){
        Set <GamePlayer> gamePlayers = gamePlayer.getGame ().getGamePlayer ();
        Set<Ship> gamePlayerShips=gamePlayer.getShips ();
        Map<String, Object> turns = new HashMap<String, Object> ();
        GamePlayer opponentGamePlayer = gamePlayers
                .stream ()
                .filter(gp -> !gp.getId ().equals (gamePlayer.getId ())).findFirst ().orElse (null);

        if(!opponentGamePlayer.equals (null)){
            ArrayList<String> carrierHitsPosition=new ArrayList<> ();
            ArrayList<String> battleshipHitsPosition=new ArrayList<> ();
            ArrayList<String> submarineHitsPosition=new ArrayList<> ();
            ArrayList<String> destroyerHitsPosition=new ArrayList<> ();
            ArrayList<String> patrolBoatHitsPosition=new ArrayList<> ();

            Set<Salvo> opponentGamePlayerSalvos=opponentGamePlayer.getSalvos ();
            for (Salvo salvo:opponentGamePlayerSalvos){
                List<String> opponentFireLocations=salvo.getFireLocations ();
                    for (String opponentFireLocation:opponentFireLocations){
                        for(Ship ship:gamePlayerShips) {
                            if (ship.getShipType ().equals ("Carrier")) {
                                if (ship.getLocations ().contains (opponentFireLocation.replace ("PL", ""))) {
                                    carrierHitsPosition.add(opponentFireLocation.replace ("PL", ""));
                                }
                            }else if (ship.getShipType ().equals ("Battleship")){
                                if (ship.getLocations ().contains (opponentFireLocation.replace ("PL", ""))) {
                                    battleshipHitsPosition.add(opponentFireLocation.replace ("PL", ""));
                                }
                            }else if (ship.getShipType ().equals ("Submarine")){
                                if (ship.getLocations ().contains (opponentFireLocation.replace ("PL", ""))) {
                                    submarineHitsPosition.add(opponentFireLocation.replace ("PL", ""));
                                }
                            }else if (ship.getShipType ().equals ("Destroyer")){
                                if (ship.getLocations ().contains (opponentFireLocation.replace ("PL", ""))) {
                                    destroyerHitsPosition.add(opponentFireLocation.replace ("PL", ""));
                                }
                            }else{
                                if (ship.getLocations ().contains (opponentFireLocation.replace ("PL", ""))) {
                                    patrolBoatHitsPosition.add(opponentFireLocation.replace ("PL", ""));
                                }
                            }
                        }
                    }

                    Map<String, Object> carrierHits = new HashMap<> ();
                    carrierHits.put ("hitsPosition",carrierHitsPosition);

                    Map<String, Object> battleshipHits = new HashMap<String, Object> ();
                    battleshipHits.put ("hitsPosition",battleshipHitsPosition);

                    Map<String, Object> submarineHits = new HashMap<String, Object> ();
                    submarineHits.put ("hitsPosition",submarineHitsPosition);

                    Map<String, Object> destroyerHits = new HashMap<String, Object> ();
                    destroyerHits.put ("hitsPosition", destroyerHitsPosition);

                    Map<String, Object> patrolBoatHits = new HashMap<String, Object> ();
                    patrolBoatHits.put ("hitsPosition",patrolBoatHitsPosition);

                    Map<String, Object> opponentHits = new HashMap<String, Object> ();
                    opponentHits.put ("CarrierHits", carrierHits);
                    opponentHits.put ("BattleshipHits", battleshipHits);
                    opponentHits.put ("SubmarineHits", submarineHits);
                    opponentHits.put ("DestroyerHits", destroyerHits);
                    opponentHits.put ("PatrolBoatHits", patrolBoatHits);
                    opponentHits.put ("shipsLeft", shipsLeft(opponentHits));

                    turns.put ("turn"+salvo.getTurn (),opponentHits );

            }

            Set<String> set=turns.keySet ();
            for(String s:set){
                HashMap<String, Object> value= (HashMap<String, Object>) turns.get (s);
                Set<String> set2=value.keySet ();
                for(String s2:set2){
                    Object obj=value.get (s2);
                }
            }

            TreeMap<String, Object> turnsTreeMap = new TreeMap<> (
                    (Comparator<String>) (s1, s2) -> s1.compareTo(s2)
            );
            turnsTreeMap.putAll(turns);

            return  turnsTreeMap;
        }else{
        return "wating";}
    }

    private int shipsLeft(Map<String, Object> shipsHits) {
        int num=5;
        Set<String> shipsSet=shipsHits.keySet ();
        for(String s:shipsSet){
            int shipLength;
            if(s.equals ("Carrier")){
                shipLength=5;
            }else if(s.equals ("Battleship")){
                shipLength=4;
            }else if(s.equals ("Submarine")){
                shipLength=4;
            }else if(s.equals ("Destroyer")){
                shipLength=3;
            }else{
                shipLength=2;
            }
            Map<String,Object> shipHitsMap= (Map<String, Object>) shipsHits.get (s);
            Set<String> shipHitSet=shipHitsMap.keySet ();
            for(String ss:shipHitSet){
                if (ss.equals ("hitTimes")){
                    if (shipHitsMap.get (ss).equals (shipLength)){
                     num--;
                  }
                }
            }
        }

         return num;
    }

    private String shipStatus(String shipName,int shipHitsPositionLength){
        String shipStatus;
        int shipLength;
        if(shipName.equals ("Carrier")){
            shipLength=5;
        }else if(shipName.equals ("Battleship")){
            shipLength=4;
        }else if(shipName.equals ("Submarine")){
            shipLength=4;
        }else if(shipName.equals ("Destroyer")){
            shipLength=3;
        }else{
            shipLength=2;
        }

        if(shipHitsPositionLength==0) {
            shipStatus = "safe";
        }else if(shipHitsPositionLength==shipLength){
            shipStatus = "sunk";
        }else{
            shipStatus = "sink";
        }
        return shipStatus;
    }

    private Map<String, Map<String, Object>> makeSalvoDto(GamePlayer gamePlayer) {
        Map<String, Map<String, Object>> salvoDto = new HashMap<String, Map<String, Object>> ();
        for (Salvo salvo : gamePlayer.getSalvos ()) {
            Map<String, Object> eachSalvoDto = new HashMap<String, Object> ();
            eachSalvoDto.put (gamePlayer.getId ().toString (), salvo.getFireLocations ());
            salvoDto.put (String.valueOf (salvo.getTurn ()), eachSalvoDto);
        }
        return salvoDto;
    }

    private Map<String, Object> makeShipDto(Ship ship) {
        Map<String, Object> shipDto = new HashMap<> ();
        shipDto.put ("shipType", ship.getShipType ());
        shipDto.put ("locations", ship.getLocations ());
        return shipDto;
    }

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> register(@RequestBody Player player) {

        if (player.getUserName ().isEmpty () || player.getPassword ().isEmpty ()) {
            return new ResponseEntity<> (responseDto ("error","Missing data"), HttpStatus.NOT_FOUND);
        }

        else if (playerRepository.findByUserName (player.getUserName ()) != null) {
            return new ResponseEntity<> (responseDto ("error","Name already in use"), HttpStatus.FORBIDDEN);
        }

        else {
        playerRepository.save (new Player (player.getUserName (), passwordEncoder.encode (player.getPassword ())));
        return new ResponseEntity<> (responseDto ("success",player.getUserName ()),HttpStatus.CREATED);}
    }


    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> createNewGame(Authentication authentication) {
        if (isGuest (authentication)) {
            return new ResponseEntity<> (responseDto ("fail","You need to signup!!!"), HttpStatus.UNAUTHORIZED);
        } else {
            Game g1=new Game (new Date ());
            GamePlayer gp1=new GamePlayer (new Date (), g1, getCurrentUser (authentication));
            gameRepository.save (g1);
            gamePlayerRepository.save (gp1);
            return new ResponseEntity<> (responseDto("gp1",gp1.getId ()),HttpStatus.CREATED);}
    }

    @RequestMapping(path = "/game/{gId}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> joinNewGame(Authentication authentication,@PathVariable Long gId) {

        Game game=gameRepository.getOne (gId);
        List<GamePlayer> gamePlayers = new ArrayList<> ();
        gamePlayers.addAll (game.getGamePlayer ());
        List<Player> players = new ArrayList<> ();

        for(int x=0;x<gamePlayers.size ();x++){
            Player player=gamePlayers.get (x).getPlayer ();
            players.add (player);
        }
        if (isGuest (authentication)) {
            return new ResponseEntity<> (responseDto ("tip","You need to signUp!!!"), HttpStatus.UNAUTHORIZED);
        } else {
            if (gamePlayers.size () == 2) {
                if (players.contains (getCurrentUser (authentication))) {
                    GamePlayer currentUserGamePlayer=new GamePlayer ();
                    for(GamePlayer gamePlayer:gamePlayers){
                        if (gamePlayer.getPlayer ().equals (getCurrentUser (authentication))){
                            currentUserGamePlayer=gamePlayer;
                        }
                    }
                    return new ResponseEntity<> (responseDto ("currentUserGpID1",currentUserGamePlayer.getId ()), HttpStatus.ACCEPTED);

                } else {
                    return new ResponseEntity<> (responseDto ("sorry", "Game is full!!!"), HttpStatus.FORBIDDEN);
                }
            }else{
                if (gamePlayers.get (0).getPlayer ().equals (getCurrentUser (authentication))) {
                    return new ResponseEntity<> (responseDto ("currentUserGpID2", gamePlayers.get (0).getId ()), HttpStatus.FORBIDDEN);
                } else{
                    GamePlayer gp2 = new GamePlayer (new Date (), game, getCurrentUser (authentication));
                    gameRepository.save (game);
                    gamePlayerRepository.save (gp2);
                    return new ResponseEntity<> (responseDto ("gp2Id", gp2.getId ()), HttpStatus.CREATED);
                }
            }
        }
    }

    @RequestMapping(path = "/games/players/{gpId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> placeShips(Authentication authentication,@PathVariable Long gpId,@RequestBody Set<Ship> ships) {
        GamePlayer gp=gamePlayerRepository.getOne (gpId);

        if (isGuest (authentication)) {
            return new ResponseEntity<> (responseDto ("no","you r no current user logged in!!!"), HttpStatus.UNAUTHORIZED);
        } else if(ships.size ()>5){
            return new ResponseEntity<> (responseDto ("cross","you are palce to many ships!!!"), HttpStatus.FORBIDDEN);
        }  else if(ships.size ()<5){
            return new ResponseEntity<> (responseDto ("insufficient","you need to place more ships!!!"), HttpStatus.FORBIDDEN);
        }
        else{
            if(gp.getShips().containsAll (ships)){
                return new ResponseEntity<> (responseDto ("overlap","already has ships placed here!!!"), HttpStatus.FORBIDDEN);
            } else {
                for(Ship ship : ships){
                    ship.setGamePlayer (gp);
                    shipRepository.save (ship);
                }
                return new ResponseEntity<> (responseDto ("success",gp.getShips ()),HttpStatus.CREATED);
            }
        }
    }

    @RequestMapping(path = "/games/players/{gpId}/salvos", method = RequestMethod.POST)
        public ResponseEntity<Map<String,Object>> placesalvos(Authentication authentication,@PathVariable Long gpId, @RequestBody Salvo mySalvo) {
        GamePlayer gp = gamePlayerRepository.getOne (gpId);
        Set<Salvo> salvos = gp.getSalvos ();

        if (isGuest (authentication)) {
            return new ResponseEntity<> (responseDto ("no", "you r no current user logged in!!!"), HttpStatus.UNAUTHORIZED);
        }else if(!getCurrentUser (authentication).getId ().equals (gp.getPlayer ().getId ())){
            return new ResponseEntity<> (responseDto ("sorry","you are not the owner of this game"), HttpStatus.FORBIDDEN);
        }else {
            if(mySalvo.getFireLocations ().size ()<5){
            return new ResponseEntity<> (responseDto ("insufficient","you can fire more shots!!!"), HttpStatus.FORBIDDEN);

            }else if(mySalvo.getFireLocations ().size ()>5){
            return new ResponseEntity<> (responseDto ("cross","you can only fire five shots in one turn!!!"), HttpStatus.FORBIDDEN);

            }else {
            if (salvos.size () == 0) {
                Salvo salvo = new Salvo ();
                salvo.setTurn (1);
                salvo.setFireLocations (mySalvo.getFireLocations ());
                gp.addSalvo (salvo);
                salvoRepository.save (salvo);
                return new ResponseEntity<> (responseDto ("success", salvo), HttpStatus.CREATED);
            } else {
                int beforeTurn = salvos.size ();
                Salvo salvo = new Salvo ();
                if (beforeTurn < 3) {
                    salvo.setTurn (beforeTurn + 1);
                    salvo.setFireLocations (mySalvo.getFireLocations ());
                    gp.addSalvo (salvo);
                    salvoRepository.save (salvo);
                    return new ResponseEntity<> (responseDto ("success", salvo), HttpStatus.CREATED);
                } else {
                    return new ResponseEntity<> (responseDto ("finish", "you only can fire three turns!!!"), HttpStatus.CREATED);
                }
            }
          }
        }
    }

    private Map<String, Object> responseDto(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }


    private Player getCurrentUser(Authentication authentication) {
        return playerRepository.findByUserName (authentication.getName ());
    }

    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }
}








//    private Object getCurrentGamePlayerSalvoDto (GamePlayer gamePlayer){
//        Map<String,Object> turns = new HashMap<> ();
//        Set<Salvo> currentGamePlayerSalvos=gamePlayer.getSalvos ();
//
//        Set <GamePlayer> gamePlayers = gamePlayer.getGame ().getGamePlayer ();
//        GamePlayer opponentGamePlayer = gamePlayers
//                .stream ()
//                .filter(gp -> !gp.getId ().equals (gamePlayer.getId ())).findFirst ().orElse (null);
//
//        if(!opponentGamePlayer.equals (null)){
//            Set<Ship> opponentShips=opponentGamePlayer.getShips ();
//            for (Salvo salvo:currentGamePlayerSalvos) {
//                ArrayList<String> carrierHitsPosition=new ArrayList<> ();
//                ArrayList<String> battleshipHitsPosition=new ArrayList<> ();
//                ArrayList<String> submarineHitsPosition=new ArrayList<> ();
//                ArrayList<String> destroyerHitsPosition=new ArrayList<> ();
//                ArrayList<String> patrolBoatHitsPosition=new ArrayList<> ();
//
//                List<String> currentPlayerFireLocations=salvo.getFireLocations ();
//                for (String OponentFireLocation:currentPlayerFireLocations) {
//                    for (Ship ship : opponentShips) {
//                        if (ship.getShipType ().equals ("Carrier")) {
//                            if (ship.getLocations ().contains (OponentFireLocation.replace ("PL", ""))) {
//                                carrierHitsPosition.add (OponentFireLocation.replace ("PL", ""));
//                            }
//                        } else if (ship.getShipType ().equals ("Battleship")) {
//                            if (ship.getLocations ().contains (OponentFireLocation.replace ("PL", ""))) {
//                                battleshipHitsPosition.add (OponentFireLocation.replace ("PL", ""));
//                            }
//                        } else if (ship.getShipType ().equals ("Submarine")) {
//                            if (ship.getLocations ().contains (OponentFireLocation.replace ("PL", ""))) {
//                                submarineHitsPosition.add (OponentFireLocation.replace ("PL", ""));
//                            }
//                        } else if (ship.getShipType ().equals ("Destroyer")) {
//                            if (ship.getLocations ().contains (OponentFireLocation.replace ("PL", ""))) {
//                                destroyerHitsPosition.add (OponentFireLocation.replace ("PL", ""));
//                            }
//                        } else {
//                            if (ship.getLocations ().contains (OponentFireLocation.replace ("PL", ""))) {
//                                patrolBoatHitsPosition.add (OponentFireLocation.replace ("PL", ""));
//                            }
//                        }
//                    }
//                }
//
//                Map<String, Object> carrierHits = new HashMap<> ();
//                carrierHits.put ("hitTimes",carrierHitsPosition.size ());
//                carrierHits.put ("shipStatus",shipStatus("Carrier",carrierHitsPosition.size ()));
//                ArrayList<String> carrierHitsPositiontemp=new ArrayList<> ();
//                carrierHitsPositiontemp.addAll (carrierHitsPosition);
//                carrierHits.put ("hitsPosition",carrierHitsPositiontemp);
//
//                Map<String, Object> battleshipHits = new HashMap<> ();
//                battleshipHits.put ("hitTimes",battleshipHitsPosition.size ());
//                battleshipHits.put ("shipStatus",shipStatus("Battleship",battleshipHitsPosition.size ()));
//                ArrayList<String> battleshipHitsPositiontemp=new ArrayList<> ();
//                battleshipHitsPositiontemp.addAll (battleshipHitsPosition);
//                battleshipHits.put ("hitsPosition",battleshipHitsPositiontemp);
//
//                Map<String, Object> submarineHits = new HashMap<> ();
//                submarineHits.put ("hitTimes",submarineHitsPosition.size ());
//                submarineHits.put ("shipStatus",shipStatus("Submarine",submarineHitsPosition.size ()));
//                ArrayList<String> submarineHitsPositiontemp=new ArrayList<> ();
//                submarineHitsPositiontemp.addAll (submarineHitsPosition);
//                submarineHits.put ("hitsPosition",submarineHitsPositiontemp);
//
//                Map<String, Object> destroyerHits = new HashMap<String, Object> ();
//                destroyerHits.put ("hitTimes", destroyerHitsPosition.size ());
//                destroyerHits.put ("shipStatus",shipStatus("Destroyer",destroyerHitsPosition.size ()));
//                ArrayList<String> destroyerHitsPositiontemp=new ArrayList<> ();
//                destroyerHitsPositiontemp.addAll (destroyerHitsPosition);
//                destroyerHits.put ("hitsPosition", destroyerHitsPositiontemp);
//
//                Map<String, Object> patrolBoatHits = new HashMap<> ();
//                patrolBoatHits.put ("hitTimes",patrolBoatHitsPosition.size ());
//                patrolBoatHits.put ("shipStatus",shipStatus("PatrolBoat",patrolBoatHitsPosition.size ()));
//                ArrayList<String> patrolBoatHitsPositiontemp=new ArrayList<> ();
//                patrolBoatHitsPositiontemp.addAll (patrolBoatHitsPosition);
//                patrolBoatHits.put ("hitsPosition",patrolBoatHitsPositiontemp);
//
//                Map<String, Object> currentPlayerHits = new HashMap<> ();
//                currentPlayerHits.put ("Carrier", carrierHits);
//                currentPlayerHits.put ("Battleship", battleshipHits);
//                currentPlayerHits.put ("Submarine", submarineHits);
//                currentPlayerHits.put ("Destroyer", destroyerHits);
//                currentPlayerHits.put ("PatrolBoat", patrolBoatHits);
//
//                currentPlayerHits.put ("shipsLeft", shipsLeft(currentPlayerHits));
//
//                turns.put ("turn"+salvo.getTurn (),currentPlayerHits );
//            }
//
//            Set<String> set=turns.keySet ();
//            for(String s:set){
//                Map<String, Object> value= (HashMap<String, Object>) turns.get (s);
//                Set<String> set2=value.keySet ();
//                for(String s2:set2){
//                    Object obj=value.get (s2);
//                }
//            }
//
//            TreeMap<String, Object> turnsTreeMap = new TreeMap<> (
//                    (Comparator<String>) (s1, s2) -> s1.compareTo(s2)
//            );
//            turnsTreeMap.putAll(turns);
//
//            return  turnsTreeMap;
//        }else{
//            return "wating";}
//    }
//
//    private Object getOponentGamePlayerSalvoDto (GamePlayer gamePlayer){
//        Set <GamePlayer> gamePlayers = gamePlayer.getGame ().getGamePlayer ();
//        Set<Ship> gamePlayerShips=gamePlayer.getShips ();
//        Map<String, Object> turns = new HashMap<String, Object> ();
//        GamePlayer opponentGamePlayer = gamePlayers
//                .stream ()
//                .filter(gp -> !gp.getId ().equals (gamePlayer.getId ())).findFirst ().orElse (null);
//
//        if(!opponentGamePlayer.equals (null)){
//            ArrayList<String> carrierHitsPosition=new ArrayList<> ();
//            ArrayList<String> battleshipHitsPosition=new ArrayList<> ();
//            ArrayList<String> submarineHitsPosition=new ArrayList<> ();
//            ArrayList<String> destroyerHitsPosition=new ArrayList<> ();
//            ArrayList<String> patrolBoatHitsPosition=new ArrayList<> ();
//
//            Set<Salvo> opponentGamePlayerSalvos=opponentGamePlayer.getSalvos ();
//            for (Salvo salvo:opponentGamePlayerSalvos){
//                List<String> opponentFireLocations=salvo.getFireLocations ();
//                for (String opponentFireLocation:opponentFireLocations){
//                    for(Ship ship:gamePlayerShips) {
//                        if (ship.getShipType ().equals ("Carrier")) {
//                            if (ship.getLocations ().contains (opponentFireLocation.replace ("PL", ""))) {
//                                carrierHitsPosition.add(opponentFireLocation.replace ("PL", ""));
//                            }
//                        }else if (ship.getShipType ().equals ("Battleship")){
//                            if (ship.getLocations ().contains (opponentFireLocation.replace ("PL", ""))) {
//                                battleshipHitsPosition.add(opponentFireLocation.replace ("PL", ""));
//                            }
//                        }else if (ship.getShipType ().equals ("Submarine")){
//                            if (ship.getLocations ().contains (opponentFireLocation.replace ("PL", ""))) {
//                                submarineHitsPosition.add(opponentFireLocation.replace ("PL", ""));
//                            }
//                        }else if (ship.getShipType ().equals ("Destroyer")){
//                            if (ship.getLocations ().contains (opponentFireLocation.replace ("PL", ""))) {
//                                destroyerHitsPosition.add(opponentFireLocation.replace ("PL", ""));
//                            }
//                        }else{
//                            if (ship.getLocations ().contains (opponentFireLocation.replace ("PL", ""))) {
//                                patrolBoatHitsPosition.add(opponentFireLocation.replace ("PL", ""));
//                            }
//                        }
//                    }
//                }
//
//                Map<String, Object> carrierHits = new HashMap<> ();
//                carrierHits.put ("hitTimes",carrierHitsPosition.size ());
//                carrierHits.put ("shipStatus",shipStatus("Carrier",carrierHitsPosition.size ()));
//                ArrayList<String> carrierHitsPositiontemp=new ArrayList<> ();
//                carrierHitsPositiontemp.addAll (carrierHitsPosition);
//                carrierHits.put ("hitsPosition",carrierHitsPositiontemp);
//
//                Map<String, Object> battleshipHits = new HashMap<String, Object> ();
//                battleshipHits.put ("hitTimes",battleshipHitsPosition.size ());
//                battleshipHits.put ("shipStatus",shipStatus("Battleship",battleshipHitsPosition.size ()));
//                ArrayList<String> battleshipHitsPositiontemp=new ArrayList<> ();
//                battleshipHitsPositiontemp.addAll (battleshipHitsPosition);
//                battleshipHits.put ("hitsPosition",battleshipHitsPositiontemp);
//
//                Map<String, Object> submarineHits = new HashMap<String, Object> ();
//                submarineHits.put ("hitTimes",submarineHitsPosition.size ());
//                submarineHits.put ("shipStatus",shipStatus("Submarine",submarineHitsPosition.size ()));
//                ArrayList<String> submarineHitsPositiontemp=new ArrayList<> ();
//                submarineHitsPositiontemp.addAll (submarineHitsPosition);
//                submarineHits.put ("hitsPosition",submarineHitsPositiontemp);
//
//                Map<String, Object> destroyerHits = new HashMap<String, Object> ();
//                destroyerHits.put ("hitTimes", destroyerHitsPosition.size ());
//                destroyerHits.put ("shipStatus",shipStatus("Destroyer",destroyerHitsPosition.size ()));
//                ArrayList<String> destroyerHitsPositiontemp=new ArrayList<> ();
//                destroyerHitsPositiontemp.addAll (destroyerHitsPosition);
//                destroyerHits.put ("hitsPosition", destroyerHitsPositiontemp);
//
//                Map<String, Object> patrolBoatHits = new HashMap<String, Object> ();
//                patrolBoatHits.put ("hitTimes",patrolBoatHitsPosition.size ());
//                patrolBoatHits.put ("shipStatus",shipStatus("PatrolBoat",patrolBoatHitsPosition.size ()));
//                ArrayList<String> patrolBoatHitsPositiontemp=new ArrayList<> ();
//                patrolBoatHitsPositiontemp.addAll (patrolBoatHitsPosition);
//                patrolBoatHits.put ("hitsPosition",patrolBoatHitsPositiontemp);
//
//                Map<String, Object> opponentHits = new HashMap<String, Object> ();
//                opponentHits.put ("CarrierHits", carrierHits);
//                opponentHits.put ("BattleshipHits", battleshipHits);
//                opponentHits.put ("SubmarineHits", submarineHits);
//                opponentHits.put ("DestroyerHits", destroyerHits);
//                opponentHits.put ("PatrolBoatHits", patrolBoatHits);
//                opponentHits.put ("shipsLeft", shipsLeft(opponentHits));
//
//                turns.put ("turn"+salvo.getTurn (),opponentHits );
//
//            }
//
//            Set<String> set=turns.keySet ();
//            for(String s:set){
//                HashMap<String, Object> value= (HashMap<String, Object>) turns.get (s);
//                Set<String> set2=value.keySet ();
//                for(String s2:set2){
//                    Object obj=value.get (s2);
//                }
//            }
//
//            TreeMap<String, Object> turnsTreeMap = new TreeMap<> (
//                    (Comparator<String>) (s1, s2) -> s1.compareTo(s2)
//            );
//            turnsTreeMap.putAll(turns);
//
//            return  turnsTreeMap;
//        }else{
//            return "wating";}
//    }




//    private int shipsLeft(Map<String, Object> shipsHits) {
//        int num=5;
//        Set<String> shipsSet=shipsHits.keySet ();
//        for(String s:shipsSet){
//            int shipLength;
//            if(s.equals ("Carrier")){
//                shipLength=5;
//            }else if(s.equals ("Battleship")){
//                shipLength=4;
//            }else if(s.equals ("Submarine")){
//                shipLength=4;
//            }else if(s.equals ("Destroyer")){
//                shipLength=3;
//            }else{
//                shipLength=2;
//            }
//            Map<String,Object> shipHitsMap= (Map<String, Object>) shipsHits.get (s);
//            Set<String> shipHitSet=shipHitsMap.keySet ();
//            for(String ss:shipHitSet){
//                if (ss.equals ("hitTimes")){
//                    if (shipHitsMap.get (ss).equals (shipLength)){
//                        num--;
//                    }
//                }
//            }
//        }
//
//        return num;
//    }
//
//    private String shipStatus(String shipName,int shipHitsPositionLength){
//        String shipStatus;
//        int shipLength;
//        if(shipName.equals ("Carrier")){
//            shipLength=5;
//        }else if(shipName.equals ("Battleship")){
//            shipLength=4;
//        }else if(shipName.equals ("Submarine")){
//            shipLength=4;
//        }else if(shipName.equals ("Destroyer")){
//            shipLength=3;
//        }else{
//            shipLength=2;
//        }
//
//        if(shipHitsPositionLength==0) {
//            shipStatus = "safe";
//        }else if(shipHitsPositionLength==shipLength){
//            shipStatus = "sunk";
//        }else{
//            shipStatus = "sink";
//        }
//        return shipStatus;
//    }



// if (isGuest (authentication)) {
//         return new ResponseEntity<> (responseDto ("tip","You need to signUp!!!"), HttpStatus.UNAUTHORIZED);
//        } else{
//        if(gamePlayers.size ()==2){
//        if(players.contains (getCurrentUser (authentication))){
//        return new ResponseEntity<> (responseDto ("ok1","you r coming to you own game!!!"), HttpStatus.ACCEPTED);
//        }else {
//        return new ResponseEntity<> (responseDto ("sorry","Game is full!!!"), HttpStatus.FORBIDDEN);}
//        } else if(gamePlayers.size ()==1){
//        if(gamePlayers.get(0).getPlayer ().getId ().equals (getCurrentUser (authentication).getId ())){
//        return new ResponseEntity<> (responseDto ("existingpId",gamePlayers.get(0).getPlayer ().getId ()), HttpStatus.ACCEPTED);
//        }else {
//        return new ResponseEntity<> (responseDto ("forbidden","you can not cheat"), HttpStatus.FORBIDDEN);
//        }
//
//        } else {
//        GamePlayer gp2=new GamePlayer (new Date (), game, getCurrentUser (authentication));
//        gameRepository.save (game);
//        gamePlayerRepository.save (gp2);
//        return new ResponseEntity<> (responseDto("gp2Id",gp2.getId ()),HttpStatus.CREATED);
//        }
//        }