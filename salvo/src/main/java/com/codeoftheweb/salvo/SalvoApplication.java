package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.stream.Location;
import java.lang.reflect.Array;
import java.util.Date;
import java.util.*;

@SpringBootApplication
public class SalvoApplication {
    public static void main(String[] args) {
        SpringApplication.run (SalvoApplication.class, args);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder ();
    }

    @Bean
    public CommandLineRunner initData(PasswordEncoder passwordEncoder, PlayerRepository playerRepository, GameRepository gameRepository, GamePlayerRepository gamePlayerRepository, ShipRepository shipRepository, SalvoRepository salvoRepository, ScoreRestRepository scoreRestRepository) {
        return (args) -> {


            Player p1 = new Player ("JackBauer", passwordEncoder.encode ("aaa"));
            playerRepository.save (p1);

            Player p2 = new Player ("C.obrian",  passwordEncoder.encode ("bbb"));
            playerRepository.save (p2);

            Player p3 = new Player ("Kim Bauer", passwordEncoder.encode ("ccc"));
            playerRepository.save (p3);

            Player p4 = new Player ("T.almeida", passwordEncoder.encode ("ddd"));
            playerRepository.save (p4);




            Game g1 = new Game (new Date ());
            gameRepository.save (g1);

            Game g2 = new Game (new Date ());
            Date.from (g2.getDate ().toInstant ().plusSeconds (3600));
            gameRepository.save (g2);

            Game g3 = new Game (new Date ());
            Date.from (g3.getDate ().toInstant ().plusSeconds (7200));
            gameRepository.save (g3);

            Game g4 = new Game (new Date ());
            Date.from (g4.getDate ().toInstant ().plusSeconds (10800));
            gameRepository.save (g4);

            Game g5 = new Game (new Date ());
            Date.from (g5.getDate ().toInstant ().plusSeconds (14400));
            gameRepository.save (g5);

            Game g6 = new Game (new Date ());
            Date.from (g6.getDate ().toInstant ().plusSeconds (18000));
            gameRepository.save (g6);

            Game g7 = new Game (new Date ());
            Date.from (g7.getDate ().toInstant ().plusSeconds (21600));
            gameRepository.save (g7);

            Game g8 = new Game (new Date ());
            Date.from (g8.getDate ().toInstant ().plusSeconds (25200));
            gameRepository.save (g8);




             //in game 1
             GamePlayer gp1 = new GamePlayer (new Date (), g1, p1);
            gamePlayerRepository.save (gp1);
            GamePlayer gp2 = new GamePlayer (new Date (), g1, p2);
            gamePlayerRepository.save (gp2);

            //in game 2
            GamePlayer gp3 = new GamePlayer (new Date (), g2, p1);
            gamePlayerRepository.save (gp3);
            GamePlayer gp4 = new GamePlayer (new Date (), g2, p2);
            gamePlayerRepository.save (gp4);

            //in game 3
            GamePlayer gp5 = new GamePlayer (new Date (), g3, p2);
            gamePlayerRepository.save (gp5);
            GamePlayer gp6 = new GamePlayer (new Date (), g3, p4);
            gamePlayerRepository.save (gp6);

            //in game 4
            GamePlayer gp7 = new GamePlayer (new Date (), g4, p2);
            gamePlayerRepository.save (gp7);
            GamePlayer gp8 = new GamePlayer (new Date (), g4, p1);
            gamePlayerRepository.save (gp8);

            //in game 5
            GamePlayer gp9 = new GamePlayer (new Date (), g5, p4);
            gamePlayerRepository.save (gp9);
            GamePlayer gp10 = new GamePlayer (new Date (), g5, p1);
            gamePlayerRepository.save (gp10);

            //in game 6
            GamePlayer gp11 = new GamePlayer (new Date (), g6, p3);
            gamePlayerRepository.save (gp11);

            //in game 7
            GamePlayer gp13 = new GamePlayer (new Date (), g7, p4);
            gamePlayerRepository.save (gp13);

            //in game 8
            GamePlayer gp15 = new GamePlayer (new Date (), g8, p3);
            gamePlayerRepository.save (gp15);
            GamePlayer gp16 = new GamePlayer (new Date (), g8, p4);
            gamePlayerRepository.save (gp16);





            //gamePlayer1-ship
            Ship ship1 = new Ship ("Destroyer", Arrays.asList ("H2", "H3", "H4"), gp1);
            shipRepository.save (ship1);

            Ship ship2 = new Ship ("Submarine", Arrays.asList ("E1", "F1", "G1"), gp1);
            shipRepository.save (ship2);

            Ship ship3 = new Ship ("Patrol Boat", Arrays.asList ("B4", "B5"), gp1);
            shipRepository.save (ship3);

            //gamePlayer2-ship
            Ship ship4 = new Ship ("Destroyer", Arrays.asList ("B5", "C5", "D5"), gp2);
            shipRepository.save (ship4);

            Ship ship5 = new Ship ("Patrol Boat", Arrays.asList ("F1", "F2"), gp2);
            shipRepository.save (ship5);

            //in game 2
            //gamePlayer3-ship
            Ship ship6 = new Ship ("Destroyer", Arrays.asList ("B5", "C5", "D5"), gp3);
            shipRepository.save (ship6);

            Ship ship7 = new Ship ("Patrol Boat", Arrays.asList ("C6", "C7"), gp3);
            shipRepository.save (ship7);

            //gamePlayer4-ship
            Ship ship8 = new Ship ("Submarine", Arrays.asList ("A2", "A3", "A4"), gp4);
            shipRepository.save (ship8);

            Ship ship9 = new Ship ("Patrol Boat", Arrays.asList ("G6", "H6"), gp4);
            shipRepository.save (ship9);


            //in game 3
            //gamePlayer5-ship
            Ship ship10 = new Ship ("Destroyer", Arrays.asList ("B5", "C5", "D5"), gp5);
            shipRepository.save (ship10);

            Ship ship11 = new Ship ("Patrol Boat", Arrays.asList ("C6", "C7"), gp5);
            shipRepository.save (ship11);

            //gamePlayer6-ship
            Ship ship12 = new Ship ("Submarine", Arrays.asList ("A2", "A3", "A4"), gp6);
            shipRepository.save (ship12);

            Ship ship13 = new Ship ("Patrol Boat", Arrays.asList ("G6", "H6"), gp6);
            shipRepository.save (ship13);


            //in game 4
            //gamePlayer7-ship
            Ship ship14 = new Ship ("Destroyer", Arrays.asList ("B5", "C5", "D5"), gp7);
            shipRepository.save (ship14);

            Ship ship15 = new Ship ("Patrol Boat", Arrays.asList ("C6", "C7"), gp7);
            shipRepository.save (ship15);

            //gamePlayer8-ship
            Ship ship16 = new Ship ("Submarine", Arrays.asList ("A2", "A3", "A4"), gp8);
            shipRepository.save (ship16);

            Ship ship17 = new Ship ("Patrol Boat", Arrays.asList ("G6", "H6"), gp8);
            shipRepository.save (ship17);


            //in game 5
            //gamePlayer9-ship
            Ship ship18 = new Ship ("Destroyer", Arrays.asList ("B5", "C5", "D5"), gp9);
            shipRepository.save (ship18);

            Ship ship19 = new Ship ("Patrol Boat", Arrays.asList ("C6", "C7"), gp9);
            shipRepository.save (ship19);

            //gamePlayer10-ship
            Ship ship20 = new Ship ("Submarine", Arrays.asList ("A2", "A3", "A4"), gp10);
            shipRepository.save (ship20);

            Ship ship21 = new Ship ("Patrol Boat", Arrays.asList ("G6", "H6"), gp10);
            shipRepository.save (ship21);


            //in game 6
            //gamePlayer11-ship
            Ship ship22 = new Ship ("Destroyer", Arrays.asList ("B5", "C5", "D5"), gp11);
            shipRepository.save (ship22);

            Ship ship23 = new Ship ("Patrol Boat", Arrays.asList ("C6", "C7"), gp11);
            shipRepository.save (ship23);


            //in game 7
            //gamePlayer13-ship
            Ship ship26 = new Ship ("Destroyer", Arrays.asList ("B5", "C5", "D5"), gp13);
            shipRepository.save (ship26);

            Ship ship27 = new Ship ("Patrol Boat", Arrays.asList ("C6", "C7"), gp13);
            shipRepository.save (ship27);

            //in game 8
            //gamePlayer15-ship
            Ship ship30 = new Ship ("Destroyer", Arrays.asList ("B5", "C5", "D5"), gp15);
            shipRepository.save (ship30);

            Ship ship31 = new Ship ("Patrol Boat", Arrays.asList ("C6", "C7"), gp15);
            shipRepository.save (ship31);

            //gamePlayer16-ship
            Ship ship32 = new Ship ("Submarine", Arrays.asList ("A2", "A3", "A4"), gp16);
            shipRepository.save (ship32);

            Ship ship33 = new Ship ("Patrol Boat", Arrays.asList ("G6", "H6"), gp16);
            shipRepository.save (ship33);




            //game1
            //gamePlayer1-Salvo
            Salvo salvo1 = new Salvo (1, Arrays.asList ("B5", "C5", "F1"), gp1);
            salvoRepository.save (salvo1);

            Salvo salvo2 = new Salvo (2, Arrays.asList ("F2", "D5"), gp1);
            salvoRepository.save (salvo2);


            //gamePlayer2-Salvo
            Salvo salvo3 = new Salvo (1, Arrays.asList ("B4", "B5", "B6"), gp2);
            salvoRepository.save (salvo3);

            Salvo salvo4 = new Salvo (2, Arrays.asList ("E1", "H3", "A2"), gp2);
            salvoRepository.save (salvo4);


            //game2
            //gamePlayer1-Salvo
            Salvo salvo5 = new Salvo (1, Arrays.asList ("A2", "A4", "G6"), gp3);
            salvoRepository.save (salvo5);

            Salvo salvo6 = new Salvo (2, Arrays.asList ("F2", "D5"), gp3);
            salvoRepository.save (salvo6);


            //gamePlayer2-Salvo
            Salvo salvo7 = new Salvo (1, Arrays.asList ("B4", "B5", "B6"), gp4);
            salvoRepository.save (salvo7);

            Salvo salvo8 = new Salvo (2, Arrays.asList ("E1", "H3", "A2"), gp4);
            salvoRepository.save (salvo8);


            //game3
            //gamePlayer1-Salvo
            Salvo salvo9 = new Salvo (1, Arrays.asList ("G6", "H6", "A4"), gp5);
            salvoRepository.save (salvo9);

            Salvo salvo10 = new Salvo (2, Arrays.asList ("A2", "A3","D8"), gp5);
            salvoRepository.save (salvo10);


            //gamePlayer2-Salvo
            Salvo salvo11 = new Salvo (1, Arrays.asList ("H1", "H2", "H3"), gp6);
            salvoRepository.save (salvo11);

            Salvo salvo12 = new Salvo (2, Arrays.asList ("E1", "F2", "G3"), gp6);
            salvoRepository.save (salvo12);


            //game4
            //gamePlayer1-Salvo
            Salvo salvo13= new Salvo (1, Arrays.asList ("A3", "A4", "F7"), gp7);
            salvoRepository.save (salvo13);

            Salvo salvo14 = new Salvo (2, Arrays.asList ("A2", "G6","H6"), gp7);
            salvoRepository.save (salvo14);


            //gamePlayer2-Salvo
            Salvo salvo15 = new Salvo (1, Arrays.asList ("B5", "C6", "H1"), gp8);
            salvoRepository.save (salvo15);

            Salvo salvo16 = new Salvo (2, Arrays.asList ("C5", "C7", "D5"), gp8);
            salvoRepository.save (salvo16);


            //game4
            //gamePlayer1-Salvo
            Salvo salvo17= new Salvo (1, Arrays.asList ("A1", "A2", "A3"), gp9);
            salvoRepository.save (salvo13);

            Salvo salvo18 = new Salvo (2, Arrays.asList ("G6", "G7","H8"), gp9);
            salvoRepository.save (salvo14);


            //gamePlayer2-Salvo
            Salvo salvo19 = new Salvo (1, Arrays.asList ("B5", "B6", "C7"), gp10);
            salvoRepository.save (salvo19);

            Salvo salvo20 = new Salvo (2, Arrays.asList ("C6", "D6", "E6"), gp10);
            salvoRepository.save (salvo20);

            Salvo salvo21 = new Salvo (3, Arrays.asList ("C6", "D6", "E6"), gp10);
            salvoRepository.save (salvo21);





            Score sc1 = new Score (g1, p1, 1.00);
            scoreRestRepository.save (sc1);
            Score sc2 = new Score (g1, p2, 0.0);
            scoreRestRepository.save (sc2);

            Score sc3 = new Score (g2, p1, 0.5);
            scoreRestRepository.save (sc3);
            Score sc4 = new Score (g2, p2, 0.5);
            scoreRestRepository.save (sc4);

            Score sc5 = new Score (g3, p2, 1.00);
            scoreRestRepository.save (sc5);
            Score sc6 = new Score (g3, p4, 0.0);
            scoreRestRepository.save (sc6);

            Score sc7 = new Score (g4, p2, 1.00);
            scoreRestRepository.save (sc7);
            Score sc8 = new Score (g4, p1, 0.0);
            scoreRestRepository.save (sc8);



        };
    }
}

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

    @Autowired
    PlayerRepository playerRepository;

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService (userName -> {
            Player player = playerRepository.findByUserName (userName);
            if (player != null) {
                return new User (player.getUserName (), player.getPassword (),//why it is new user?????????????????
                        AuthorityUtils.createAuthorityList ("USER"));
            } else {
                throw new UsernameNotFoundException ("Unknown user: " + userName);
            }
        });
    }
}

@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests ()
                .antMatchers ("/api/**").permitAll ()
                .antMatchers ("/admin/**").hasAuthority ("ADMIN")
                .antMatchers ("/web/game.html").hasAuthority ("USER")
                .antMatchers ("/web/game.js").hasAuthority ("USER")
                .antMatchers ("/web/games.html").permitAll ()
                .antMatchers ("/web/games.js").permitAll ()
                .and ();
        http.formLogin ()
                .usernameParameter ("userName")
                .passwordParameter ("password")
                .loginPage ("/api/login");

        http.logout ().logoutUrl ("/api/logout");
        http.csrf ().disable ();

        http.exceptionHandling ().authenticationEntryPoint ((req, res, exc) -> res.sendError (HttpServletResponse.SC_UNAUTHORIZED));
        http.formLogin ().successHandler ((req, res, auth) -> clearAuthenticationAttributes (req));
        http.formLogin ().failureHandler ((req, res, exc) -> res.sendError (HttpServletResponse.SC_UNAUTHORIZED));
        http.logout ().logoutSuccessHandler (new HttpStatusReturningLogoutSuccessHandler ());

        http.headers().frameOptions().disable();
    }

    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession (false);
        if (session != null) {
            session.removeAttribute (WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }

}


//    Score sc1 = new Score (new Date (), g1, p1, 1.00);
//            scoreRestRepository.save (sc1);
//                    Score sc2 = new Score (new Date (), g1, p2, 0.0);
//                    scoreRestRepository.save (sc2);
//
//                    Score sc3 = new Score (new Date (), g2, p1, 0.5);
//                    scoreRestRepository.save (sc3);
//                    Score sc4 = new Score (new Date (), g2, p2, 0.5);
//                    scoreRestRepository.save (sc4);
//
//                    Score sc5 = new Score (new Date (), g3, p2, 1.00);
//                    scoreRestRepository.save (sc5);
//                    Score sc6 = new Score (new Date (), g3, p4, 0.0);
//                    scoreRestRepository.save (sc6);
//
//                    Score sc7 = new Score (new Date (), g4, p2, 1.00);
//                    scoreRestRepository.save (sc7);
//                    Score sc8 = new Score (new Date (), g4, p1, 0.0);
//                    scoreRestRepository.save (sc8);




