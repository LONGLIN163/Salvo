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

    @Autowired
    private ScoreRestRepository scoreRestRepository;


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
                .sorted ((g1, g2) -> (int) (g2.getId ()-(g1.getId ())))
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
                    .sorted ((gp1, gp2) -> gp2.getId ().compareTo (gp1.getId ()))
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
            GamePlayer opponentGamePlayer = findOpponentGameplayer (gpID);
            Map<String, Object> someGameInfo = new HashMap<> ();
            if (gamePlayer.getPlayer ().getId () != getCurrentUser (authentication).getId ()) {
                return new ResponseEntity<> (responseDto ("error", "u r not the ownner of this gamePlayer"), HttpStatus.FORBIDDEN);
            } else {
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

                if (opponentGamePlayer != null) {
                    someGameInfo.put ("hitsOnYou", getHitTurnsDto (gamePlayer, opponentGamePlayer));
                    someGameInfo.put ("hitsOnOppent", getHitTurnsDto (opponentGamePlayer, gamePlayer));
                    someGameInfo.put ("gameStatus", makeGameStatus(gamePlayer,gpID));
                    someGameInfo.put ("score", calculateScore(gamePlayer,gpID));
                    someGameInfo.put ("oppShipsFromBackLength",opponentGamePlayer.getShips ().size () );
                    someGameInfo.put ("salvos",gamePlayer.getSalvos ());

                } else {
                    someGameInfo.put ("gameStatus", "waiting");
                }
                return new ResponseEntity<> (responseDto ("success", someGameInfo), HttpStatus.CREATED);
            }
    }

    private Map<String,Object> finalShipsStatus(Long gpID){
        GamePlayer gamePlayer = gamePlayerRepository.getOne (gpID);
        GamePlayer opponentGamePlayer = findOpponentGameplayer (gpID);
        Map<String,Object> finalShipsData=new HashMap<> ();
        if(opponentGamePlayer==null){
            return null;
        }else {
            Map<String,Object> hitsOnYouTurns=getHitTurnsDto(gamePlayer, opponentGamePlayer);
            Integer carrierFinalHits=0;
            Integer BattleshipFinalHits=0;
            Integer SubmarineFinalHits=0;
            Integer DestroyerFinalHits=0;
            Integer PatrolBoatFinalHits=0;
            for (Map.Entry<String,Object> key : hitsOnYouTurns.entrySet()) {
                Map<String,Map<String,Object>> ss= (Map<String, Map<String, Object>>) key.getValue();
                   for (String key2:ss.keySet ()){
                       if (key2.equals ("CarrierHits")){
                           carrierFinalHits+=(Integer) ss.get (key2).get ("hitTimes");
                       }else if(key2.equals ("BattleshipHits")){
                           BattleshipFinalHits+=(Integer) ss.get (key2).get ("hitTimes");
                       }else if(key2.equals ("SubmarineHits")){
                           SubmarineFinalHits+=(Integer) ss.get (key2).get ("hitTimes");
                       }else if(key2.equals ("DestroyerHits")){
                           DestroyerFinalHits+=(Integer) ss.get (key2).get ("hitTimes");
                       }else {
                           PatrolBoatFinalHits+=(Integer) ss.get (key2).get ("hitTimes");
                       }
                   }
            }
            Integer totalHits=carrierFinalHits+BattleshipFinalHits+SubmarineFinalHits+DestroyerFinalHits+PatrolBoatFinalHits;

            Integer totalShipsLeft=5;

            if(carrierFinalHits==5){
                totalShipsLeft--;
            }else if(BattleshipFinalHits==4){
                totalShipsLeft--;
            }else if(SubmarineFinalHits==3){
                totalShipsLeft--;
            }else if(DestroyerFinalHits==3){
                totalShipsLeft--;
            }else if(PatrolBoatFinalHits==2) {
                totalShipsLeft--;
            }
            System.out.println ("tttttttttttttttttttttttttttt"+totalShipsLeft );

            finalShipsData.put ("totalHits",totalHits);
            finalShipsData.put ("totalShipsLeft",totalHits);
        }
        return finalShipsData;
    }

    private String makeGameStatus(GamePlayer gamePlayer,Long gpID){
            GamePlayer oponentGamePlayer = findOpponentGameplayer(gpID);
            GamePlayer gameCreator;
            GamePlayer gameJoinor;
            String status="waiting";

            if (gamePlayer.getId ()<oponentGamePlayer.getId ()){
                gameCreator=gamePlayer;
                gameJoinor=oponentGamePlayer;
            }else {
                gameCreator=oponentGamePlayer;
                gameJoinor=gamePlayer;
            }

            Map<String,Object> gameCreatorShipsStatus=finalShipsStatus(gameCreator.getId ());
            Map<String,Object> gameCreatorGetHitTurns=getHitTurnsDto(gameCreator,gameJoinor);
            Map<String,Object> gameJoinorShipsStatus=finalShipsStatus(gameJoinor.getId ());
            Map<String,Object> gameJoinorGetHitTurns=getHitTurnsDto(gameJoinor,gameCreator);

            System.out.println ("1111111111---"+gameCreator.getSalvos ().size ());
            System.out.println ("2222222222---"+gameJoinor.getSalvos ().size ());

            if(gameCreator.getShips ().size ()==0 || gameJoinor.getShips ().size ()==0){
                if(gamePlayer==gameCreator){
                    status="waiting";
                }else {
                    status="waiting";
                }
                return status;

            }else{

                if(gameCreator.getSalvos ().size ()==0 && gameJoinor.getSalvos ().size ()==0){
                System.out.println ("333333333---"+(gameCreatorGetHitTurns.size ()==0 && gameJoinorGetHitTurns.size ()==0));

                if(gamePlayer==gameCreator){
                    status="yourTurn";
                }else {
                    status="waiting";
                }

                return status;
            }else if(gameCreator.getSalvos ().size ()>gameJoinor.getSalvos ().size ()){

                if(gamePlayer==gameCreator){
                    status="waiting";
                }else {
                    status="yourTurn";
                }
                return status;

            }else if(gameCreator.getSalvos ().size ()==gameJoinor.getSalvos ().size () && gameCreator.getSalvos ().size ()!=0 && gameJoinor.getSalvos ().size ()!=0){

                if((Integer) gameCreatorShipsStatus.get ("totalShipsLeft")==0 && (Integer)gameJoinorShipsStatus.get ("totalShipsLeft")==0){
                    status="gameOver,tie";
                    return status;
                }
                if((Integer)gameCreatorShipsStatus.get ("totalShipsLeft")==0 && (Integer)gameJoinorShipsStatus.get ("totalShipsLeft")!=0){

                    if(gamePlayer==gameCreator){
                        status="gameOver,youLoose";
                    }else {
                        status="gameOver,youWin";
                    }
                    return status;
                }
                if((Integer)gameCreatorShipsStatus.get ("totalShipsLeft")!=0&&(Integer)gameJoinorShipsStatus.get ("totalShipsLeft")==0){
                    if(gamePlayer==gameCreator){
                        status="gameOver,youWin";
                    }else {
                        status="gameOver,youLoose";
                    }
                    return status;
                }
                if ((Integer)gameCreatorShipsStatus.get ("totalShipsLeft")!=0 && (Integer)gameJoinorShipsStatus.get ("totalShipsLeft")!=0){

                    if (gamePlayer.getSalvos ().size ()==5 && oponentGamePlayer.getSalvos ().size ()==5){

                        if((Integer) gameCreatorShipsStatus.get ("totalShipsLeft") > (Integer)gameJoinorShipsStatus.get ("totalShipsLeft")){

                            if(gamePlayer==gameCreator){
                                status="gameOver,youWin";
                            }else {
                                status="gameOver,youLoose";
                            }

                            return status;
                        }else if((Integer) gameCreatorShipsStatus.get ("totalShipsLeft") < (Integer)gameJoinorShipsStatus.get ("totalShipsLeft")){

                            if(gamePlayer==gameCreator){
                                status="gameOver,youLoose";
                            }else {
                                status="gameOver,youWin";
                            }

                            return status;
                        }else {
                            if ((Integer) gameCreatorShipsStatus.get ("totalHits") > (Integer) gameJoinorShipsStatus.get ("totalHits")) {
                                if (gamePlayer == gameCreator) {
                                    status = "gameOver,youLoose";
                                } else {
                                    status = "gameOver,youWin";
                                }
                                return status;
                            }else if (gameCreatorShipsStatus.get ("totalHits") == gameJoinorShipsStatus.get ("totalHits")) {
                                status = "gameOver,tie";
                                return status;
                            }else{
                                if (gamePlayer == gameCreator) {
                                    status = "gameOver,youWin";
                                } else {
                                    status = "gameOver,youLoose";
                                }
                                return status;
                            }
                        }
                    }else {
                        if(gamePlayer==gameCreator){
                            status="yourTurn";
                        }else {
                            status="waiting";
                        }
                        return status;
                    }
                }
              }
            }

        return "Error";
    }


    private double calculateScore(GamePlayer gamePlayer, Long gpID){
        String gameStatus=makeGameStatus(gamePlayer,gpID);
        Player player=gamePlayer.getPlayer ();
        Game game=gamePlayer.getGame ();
        Score score=new Score ();
        if (gameStatus.equals ("gameOver,tie")){
            score.setScore(0.5);
            score.setGame (game);
            score.setPlayer (player);
            player.getScore (game);
            player.addScore (score);
            player.getScores ();
            game.addScore (score);
            scoreRestRepository.save (score);
            return 0.5;

        }else if(gameStatus.equals ("gameOver,youWin")){
            score.setScore(1.0);
            score.setGame (game);
            score.setPlayer (player);
            player.getScore (game);
            player.addScore (score);
            player.getScores ();
            game.addScore (score);
            scoreRestRepository.save (score);
            return 1.0;
        }else if(gameStatus.equals ("gameOver,youLoose")){
            score.setScore(0.0);
            score.setGame (game);
            score.setPlayer (player);
            player.getScore (game);
            player.addScore (score);
            player.getScores ();
            game.addScore (score);
            scoreRestRepository.save (score);
            return 0;
        }else {
            return 0;
        }
    }

    private GamePlayer findOpponentGameplayer(Long gpID){
        GamePlayer gamePlayer = gamePlayerRepository.getOne (gpID);
        GamePlayer opponentGamePlayer = gamePlayer.getGame ().getGamePlayer ()
                .stream ()
                .filter(gp -> !gp.getId ().equals (gpID)).findFirst ().orElse (null);
        if(opponentGamePlayer==null){
            return null;
        }else {
            return opponentGamePlayer;
        }
    }

    private Map<String,Object> getHitTurnsDto (GamePlayer gpA,GamePlayer gpB){
            Map<String,Object> turns = new HashMap<String,Object> ();
            Set<Ship> gamePlayerShips=gpA.getShips ();
            Set<Salvo> opponentGamePlayerSalvos=gpB.getSalvos ();

            for (Salvo salvo:opponentGamePlayerSalvos){
                ArrayList<String> carrierHitsPosition=new ArrayList<> ();
                ArrayList<String> battleshipHitsPosition=new ArrayList<> ();
                ArrayList<String> submarineHitsPosition=new ArrayList<> ();
                ArrayList<String> destroyerHitsPosition=new ArrayList<> ();
                ArrayList<String> patrolBoatHitsPosition=new ArrayList<> ();

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

                Map<String,Object> carrierHits = new HashMap<String,Object> ();
                carrierHits.put ("hitTimes",carrierHitsPosition.size ());
                carrierHits.put ("shipLength",5);
                carrierHits.put ("hitsPosition",carrierHitsPosition);

                Map<String,Object> battleshipHits = new HashMap<String,Object> ();
                battleshipHits.put ("hitTimes",battleshipHitsPosition.size ());
                battleshipHits.put ("shipLength",4 );
                battleshipHits.put ("hitsPosition",battleshipHitsPosition);

                Map<String,Object> submarineHits = new HashMap<String,Object> ();
                submarineHits.put ("hitTimes",submarineHitsPosition.size ());
                submarineHits.put ("shipLength",3);
                submarineHits.put ("hitsPosition",submarineHitsPosition);

                Map<String,Object> destroyerHits = new HashMap<String,Object> ();
                destroyerHits.put ("hitTimes", destroyerHitsPosition.size ());
                destroyerHits.put ("shipLength", 3);
                destroyerHits.put ("hitsPosition", destroyerHitsPosition);

                Map<String,Object> patrolBoatHits = new HashMap<String,Object> ();
                patrolBoatHits.put ("hitTimes",patrolBoatHitsPosition.size ());
                patrolBoatHits.put ("shipLength",2);
                patrolBoatHits.put ("hitsPosition",patrolBoatHitsPosition);

                Map<String,Object> opponentHits = new HashMap<String,Object> ();
                opponentHits.put ("CarrierHits", carrierHits);
                opponentHits.put ("BattleshipHits", battleshipHits);
                opponentHits.put ("SubmarineHits", submarineHits);
                opponentHits.put ("DestroyerHits", destroyerHits);
                opponentHits.put ("PatrolBoatHits", patrolBoatHits);
                turns.put ("turn"+String.valueOf (salvo.getTurn ()),opponentHits );
            }

            TreeMap<String, Object> turnsTreeMap = new TreeMap<> (
                    (Comparator<String>) (s1, s2) -> s1.compareTo(s2)
            );
            turnsTreeMap.putAll(turns);
            return  turnsTreeMap;
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
            shipLength=3;
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

    private TreeMap<String, Map<String, Object>> makeSalvoDto(GamePlayer gamePlayer) {
        TreeMap<String, Map<String, Object>> salvoDto = new TreeMap<String, Map<String, Object>> (new Comparator<String>() {
            public int compare(String t1, String t2) {
                return Integer.parseInt(t1)-Integer.parseInt(t2);
            }
        });
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

        if (isGuest (authentication)){
            return new ResponseEntity<> (responseDto ("no", "you r no current user logged in!!!"), HttpStatus.UNAUTHORIZED);
        }else if(!getCurrentUser (authentication).getId ().equals (gp.getPlayer ().getId ())){
            return new ResponseEntity<> (responseDto ("sorry","you are not the owner of this game"), HttpStatus.FORBIDDEN);
        }else {
            if(mySalvo.getFireLocations ().size ()<5){
                if(mySalvo.getFireLocations ().size ()==0){
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
                        if (beforeTurn < 5) {
                            salvo.setTurn (beforeTurn + 1);
                            salvo.setFireLocations (mySalvo.getFireLocations ());
                            gp.addSalvo (salvo);
                            salvoRepository.save (salvo);
                            return new ResponseEntity<> (responseDto ("success", salvo), HttpStatus.CREATED);
                        } else {
                            return new ResponseEntity<> (responseDto ("finish", "game over,you only can fire 5 turns!!!"), HttpStatus.CREATED);
                        }
                    }
                }else {
                    return new ResponseEntity<> (responseDto ("insufficient","you can fire more shots!!!"), HttpStatus.FORBIDDEN);
                }
            } else if(mySalvo.getFireLocations ().size ()>5){
            return new ResponseEntity<> (responseDto ("cross","you can only fire five shots in one turn!!!"), HttpStatus.FORBIDDEN);
            } else {
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
                   if (beforeTurn < 5) {
                      salvo.setTurn (beforeTurn + 1);
                      salvo.setFireLocations (mySalvo.getFireLocations ());
                      gp.addSalvo (salvo);
                      salvoRepository.save (salvo);
                      return new ResponseEntity<> (responseDto ("success", salvo), HttpStatus.CREATED);
                   } else {
                     return new ResponseEntity<> (responseDto ("finish", "game over,you only can fire 5 turns!!!"), HttpStatus.FORBIDDEN);
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




//    private Object makeGameStatus(GamePlayer gamePlayer,Long gpID){
//        Object GameStatus="waiting";
//        if (gamePlayer.getGame ().getGamePlayer ().size ()==1){
//            GameStatus="waiting";
//        }else {
//            GamePlayer oponentGamePlayer = findOpponentGameplayer(gpID);
//
//            Map<String,Object> gamePlayerPlayerShipsStatus=finalShipsStatus(gpID);
//            Map<String,Object> gamePlayerGetHitTurns=getHitTurnsDto(gamePlayer,oponentGamePlayer);
//            Map<String,Object> opponentFinalShipsStatus=finalShipsStatus(oponentGamePlayer.getId ());
//            Map<String,Object> opponentGetHitTurns=getHitTurnsDto(oponentGamePlayer,gamePlayer);
//
//            System.out.println ("sizeeeeeeeeeeeeeeeeeeeeee----"+gamePlayerGetHitTurns.size ());
//
//            if(gamePlayerGetHitTurns.size ()==0 && opponentGetHitTurns.size ()==0){
//
//                if(gamePlayer.getId ()>oponentGamePlayer.getId ()){
//                    GameStatus="waiting";
//                }else {
//                    GameStatus="yourTurn";
//                }
//                System.out.println ("rrrrrrrrrrrrrrrrr"+GameStatus.toString ());
//
//            }else if(gamePlayerGetHitTurns.size ()>opponentGetHitTurns.size ()){
//
//                GameStatus="waiting";
//
//            }else if(gamePlayerGetHitTurns.size ()<opponentGetHitTurns.size ()){
//                System.out.println ("wwwwwwwwwwwwwwwwwwwwwwwwww");
//                GameStatus="yourTurn";
//            }else if(gamePlayerGetHitTurns.size ()==opponentGetHitTurns.size() && gamePlayerGetHitTurns.size ()!=0 && opponentGetHitTurns.size ()!=0){
//
//                if((Integer) gamePlayerPlayerShipsStatus.get ("totalShipsLeft")==0 && (Integer)opponentFinalShipsStatus.get ("totalShipsLeft")==0){
//                    GameStatus="gameOver,tie";
//                }
//                if((Integer)gamePlayerPlayerShipsStatus.get ("totalShipsLeft")==0 && (Integer)opponentFinalShipsStatus.get ("totalShipsLeft")!=0){
//                    GameStatus="gameOver,youLoose";
//                }
//                if((Integer)gamePlayerPlayerShipsStatus.get ("totalShipsLeft")!=0&&(Integer)opponentFinalShipsStatus.get ("totalShipsLeft")==0){
//                    GameStatus="gameOver,youWin";
//                }
//                if ((Integer)gamePlayerPlayerShipsStatus.get ("totalShipsLeft")!=0&&(Integer)opponentFinalShipsStatus.get ("totalShipsLeft")!=0){
//
//                    if (gamePlayer.getSalvos ().size ()==5 && oponentGamePlayer.getSalvos ().size ()==5){
//
//                        if ((Integer)gamePlayerPlayerShipsStatus.get ("totalHits") > (Integer)opponentFinalShipsStatus.get ("totalHits")){
//                            GameStatus="gameOver,youLoose";
//                        }
//                        if (gamePlayerPlayerShipsStatus.get ("totalHits")==opponentFinalShipsStatus.get ("totalHits")){
//                            GameStatus="gameOver,tie";
//                        }
//                        if ((Integer)gamePlayerPlayerShipsStatus.get ("totalHits") < (Integer)opponentFinalShipsStatus.get ("totalHits")){
//                            GameStatus="gameOver,youWin";
//                        }
//                    }else{
//                        if(gamePlayer.getId ()>oponentGamePlayer.getId ()){
//                            GameStatus="waiting";
//                        }else {
//                            GameStatus="yourTurn";
//                        }
//                    }
//                }
//            }
//        }
//        System.out.println ("sssssssssssssssssssssss"+GameStatus);
//        return GameStatus;
//    }














//    private Object makeGameStatus(GamePlayer gamePlayer,Long gpID){
//        Object GameStatus="waiting";
//        if (gamePlayer.getGame ().getGamePlayer ().size ()==1){
//            GameStatus="waiting";
//        }else {
//            GamePlayer oponentGamePlayer = findOpponentGameplayer(gpID);
//
//            Map<String,Object> gamePlayerPlayerShipsStatus=finalShipsStatus(gpID);
//            Map<String,Object> gamePlayerGetHitTurns=getHitTurnsDto(gamePlayer,oponentGamePlayer);
//            Map<String,Object> opponentFinalShipsStatus=finalShipsStatus(oponentGamePlayer.getId ());
//            Map<String,Object> opponentGetHitTurns=getHitTurnsDto(oponentGamePlayer,gamePlayer);
//
//            System.out.println ("sizeeeeeeeeeeeeeeeeeeeeee----"+gamePlayerGetHitTurns.size ());
//
//            if(gamePlayerGetHitTurns.size ()==0 && opponentGetHitTurns.size ()==0){
//
//                if(gamePlayer.getId ()>oponentGamePlayer.getId ()){
//                    GameStatus="waiting";
//                }else {
//                    GameStatus="yourTurn";
//                }
//                System.out.println ("rrrrrrrrrrrrrrrrr"+GameStatus.toString ());
//            }
//
//            if (gamePlayer.getSalvos ().size ()==5 && oponentGamePlayer.getSalvos ().size ()==5){
//
//                if((Integer) gamePlayerPlayerShipsStatus.get ("totalShipsLeft")==0 && (Integer)opponentFinalShipsStatus.get ("totalShipsLeft")==0){
//                    GameStatus="gameOver,tie";
//                }
//                if((Integer)gamePlayerPlayerShipsStatus.get ("totalShipsLeft")==0 && (Integer)opponentFinalShipsStatus.get ("totalShipsLeft")!=0){
//                    GameStatus="gameOver,youLoose";
//                }
//                if((Integer)gamePlayerPlayerShipsStatus.get ("totalShipsLeft")!=0&&(Integer)opponentFinalShipsStatus.get ("totalShipsLeft")==0){
//                    GameStatus="gameOver,youWin";
//                }
//
//                if((Integer)gamePlayerPlayerShipsStatus.get ("totalShipsLeft")!=0&&(Integer)opponentFinalShipsStatus.get ("totalShipsLeft")!=0){
//
//                    if ((Integer)gamePlayerPlayerShipsStatus.get ("totalHits") > (Integer)opponentFinalShipsStatus.get ("totalHits")){
//                        GameStatus="gameOver,youLoose";
//                    }
//                    if (gamePlayerPlayerShipsStatus.get ("totalHits")==opponentFinalShipsStatus.get ("totalHits")){
//                        GameStatus="gameOver,tie";
//                    }
//                    if ((Integer)gamePlayerPlayerShipsStatus.get ("totalHits") < (Integer)opponentFinalShipsStatus.get ("totalHits")){
//                        GameStatus="gameOver,youWin";
//                    }
//                }
//            }
//
//
//
//
//
//
//            else if(gamePlayerGetHitTurns.size ()>opponentGetHitTurns.size ()){
//
//                GameStatus="waiting";
//
//            }else if(gamePlayerGetHitTurns.size ()<opponentGetHitTurns.size ()){
//                System.out.println ("wwwwwwwwwwwwwwwwwwwwwwwwww");
//                GameStatus="yourTurn";
//            }else if(gamePlayerGetHitTurns.size ()==opponentGetHitTurns.size() && gamePlayerGetHitTurns.size ()!=0 && opponentGetHitTurns.size ()!=0){
//
//
//                if ((Integer)gamePlayerPlayerShipsStatus.get ("totalShipsLeft")!=0&&(Integer)opponentFinalShipsStatus.get ("totalShipsLeft")!=0){
//
//                    if (gamePlayer.getSalvos ().size ()==5 && oponentGamePlayer.getSalvos ().size ()==5){
//
//                        if ((Integer)gamePlayerPlayerShipsStatus.get ("totalHits") > (Integer)opponentFinalShipsStatus.get ("totalHits")){
//                            GameStatus="gameOver,youLoose";
//                        }
//                        if (gamePlayerPlayerShipsStatus.get ("totalHits")==opponentFinalShipsStatus.get ("totalHits")){
//                            GameStatus="gameOver,tie";
//                        }
//                        if ((Integer)gamePlayerPlayerShipsStatus.get ("totalHits") < (Integer)opponentFinalShipsStatus.get ("totalHits")){
//                            GameStatus="gameOver,youWin";
//                        }
//                    }else{
//                        if(gamePlayer.getId ()>oponentGamePlayer.getId ()){
//                            GameStatus="waiting";
//                        }else {
//                            GameStatus="yourTurn";
//                        }
//                    }
//                }
//            }
//        }
//        System.out.println ("sssssssssssssssssssssss"+GameStatus);
//        return GameStatus;
//    }