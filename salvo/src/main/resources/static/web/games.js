/* eslint-env browser */
/* eslint "no-console": "off"  */
/* global$ */


var app = new Vue({
    el: "#vue-app",
    data: {
        GamesUrl: "/api/games",
        noRepeatPlayer: [],
        activeSth: "login",
        allScores: [],
        calledRankData: "",
        changedEachPlayerArr: [],
        games: [],
        currentPlayer: {},
        signupStatus: false,
        loginStatus: false,
        userName: "",
        password: ""

    },

    methods: {

        getGameData: function () {

            fetch(this.GamesUrl, {
                    method: "GET",
                })
                .then(function (response) {
                    if (response.ok) {
                        return response.json();
                    }
                    throw new Error(response.statusText);
                })
                .then(function (json) {
                    console.log(json);
                    app.data = json;
                    app.currentPlayer = json.player;
                    if (json.player.id) {
                        app.makeSthActive("logout");
                        app.loginStatus = true;
                    }
                    app.games = json.games;
                    //app.changePlayerData();
                    console.log(app.games);
                    console.log(app.games.player);

                    app.getNoRepeatPlayer();
                    app.getAllScores();
                    app.getTallScoreForEachPlayer();

                })
                .catch(function (error) {
                    console.log("Request failed: " + error.message);
                });

        },


        getNoRepeatPlayer: function () {
            var allPlayersTemp = [];
            for (var key in this.games) {
                for (var key2 in this.games[key].gamePlayer) {
                    var gp = this.games[key].gamePlayer[key2]
                    allPlayersTemp.push(gp.player.name);
                    console.log(this.games[key].gamePlayer[key2])
                }
            }

            this.noRepeatPlayer = Array.from(new Set(allPlayersTemp));

            console.log(this.noRepeatPlayer);

        },


        getAllScores: function () {
            for (var key in this.games) {
                if (this.games[key].scores.length != 0) {
                    for (var key2 in this.games[key].scores) {
                        //  this.games[key].scores[key2].forEach(el => this.allScores.push(el));
                        console.log(this.games[key].scores[key2])
                        this.allScores.push(this.games[key].scores[key2])
                    }
                }
            }
            console.log(this.allScores);
        },


        createNewGame: function () {


            fetch("/api/games", {
                    credentials: 'include',
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    }

                })
                .then(function (data) {

                    return data.json();

                })
                .then(function (data) {
                    console.log(data);
                    //app.joinGame(data.success.id);

                    if (data.fail) {
                        alert(data.fail);
                    } else {
                        alert("create game success");
                        window.location.assign("http://localhost:8080/web/games.html")

                    }

                })
                .catch(function (error) {
                    console.log('Request failure: ', error);
                });

        },
        //        joinGame: function (gId,pId,gpId) {
        //
        //            console.log("gId:" + gId);
        //            console.log("pId:" + pId);
        //            console.log("gpId:" + gpId);
        //
        //            fetch("/api/game/" + gId + "/players", {
        //                    credentials: 'include',
        //                    method: 'POST',
        //                    headers: {
        //                        'Content-Type': 'application/json'
        //                    }
        //
        //                })
        //                .then(function (data) {
        //
        //                    return data.json();
        //
        //                })
        //                .then(function (data) {
        //                    console.log(data);
        //                    if (data.tip) {
        //                        alert(data.tip);
        //                    } else if (data.erro) {
        //                        alert(data.erro);
        //                    } else if (data.hi) {
        //                        alert(data.hi);
        //                        if (gId == app.currentPlayer.id) {
        //                            alert("Of course!You can come to your own game!!!")
        //                            window.location.assign("/web/game.html?gp=" + gpId)
        //                        }
        //                    } else if (data.forbidden) {
        //                        alert(data.forbidden);
        //                    } else {
        //                        //if (gId == null) {
        //                            console.log(data.gp2Id);
        //                            window.location.assign("/web/game.html?gp=" + data.gp2Id)
        //                        //}
        //                    }
        //
        //                })
        //                .catch(function (error) {
        //                    console.log('Request failure: ', error);
        //                });
        //        },

        joinGame: function (gId, pId, gpId) {

            console.log("gId:" + gId);
            console.log("pId:" + pId);
            console.log("gpId:" + gpId);

            if (pId == "flag") {
                alert("click the join game button!!!")
            } else {

                fetch("/api/game/" + gId + "/players", {
                        credentials: 'include',
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        }

                    })
                    .then(function (data) {

                        return data.json();

                    })
                    .then(function (data) {
                        console.log(data);
                        if (data.tip) {
                            alert(data.tip);
                        } else if (data.erro) {
                            alert(data.erro);
                        } else if (data.hi) {

                            if (pId == app.currentPlayer.id) {
                                alert("Of course!You can come to your own game!!!")
                                window.location.assign("/web/game.html?gp=" + gpId)
                            }
                            if (pId != app.currentPlayer.id) {
                                alert(data.hi+"join the other game!!!")
                            }

                        } else if (data.forbidden) {
                            alert(data.forbidden);
                        } else {
                            console.log("gp2Id:" + data.gp2Id);
                            if (pId == null) {
                                window.location.assign("/web/game.html?gp=" + data.gp2Id)
                            }


                        }
                    })
                    .catch(function (error) {
                        console.log('Request failure: ', error);
                    });
            }
        },

        getTallScoreForEachPlayer: function () {

            for (var i = 0; i < this.noRepeatPlayer.length; i++) {

                var changedEachPlayerObj = {};
                var countTotalScores = 0;
                var winCount = 0;
                var losesCount = 0;
                var tiesCount = 0;

                for (var j = 0; j < this.allScores.length; j++) {

                    if (this.noRepeatPlayer[i] == this.allScores[j].player) {
                        countTotalScores += this.allScores[j].scores;
                    }
                    if (this.noRepeatPlayer[i] == this.allScores[j].player && this.allScores[j].scores == 1) {
                        winCount++;
                    }
                    if (this.noRepeatPlayer[i] == this.allScores[j].player && this.allScores[j].scores == 0.5) {
                        tiesCount++;
                    }
                    if (this.noRepeatPlayer[i] == this.allScores[j].player && this.allScores[j].scores == 0) {
                        losesCount++;
                    }

                    console.log(countTotalScores);
                }
                console.log(countTotalScores);

                changedEachPlayerObj = {
                    "name": this.noRepeatPlayer[i],
                    "totalScore": countTotalScores,
                    "wins": winCount,
                    "ties": tiesCount,
                    "loses": losesCount,
                    "playTimes": winCount + tiesCount + losesCount
                }
                console.log(changedEachPlayerObj);

                this.changedEachPlayerArr.push(changedEachPlayerObj);
            }
            console.log(this.changedEachPlayerArr);
        },


        sortedLeaderByValue: function (param) {

            console.log(param)
            this.calledRankData = param;
            console.log(this.calledRankData)

            if (this.calledRankData == "wins") {
                return this.changedEachPlayerArr.sort(function (a, b) {
                    return b.wins - a.wins;
                })
            } else if (this.calledRankData == "ties") {
                return this.changedEachPlayerArr.sort(function (a, b) {
                    return b.ties - a.ties;
                })
            } else if (this.calledRankData == "loses") {
                return this.changedEachPlayerArr.sort(function (a, b) {
                    return b.loses - a.loses;
                })
            } else if (this.calledRankData == "playTimes") {
                return this.changedEachPlayerArr.sort(function (a, b) {
                    return b.playTimes - a.playTimes;
                })
            } else {
                return this.changedEachPlayerArr.sort(function (a, b) {

                    if (b.totalScore == a.totalScore) {
                        return a.playTimes - b.playTimes;
                    }
                    return b.totalScore - a.totalScore;

                })
            }

        },


        login: function () {

            var json = {
                userName: this.userName,
                password: this.password
            }

            fetch("/api/login", {
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    method: 'POST',
                    body: this.getBody(json)
                })
                .then(function (data) {
                    console.log(data);
                    if (data.status == 200) {
                        app.makeSthActive("logout");
                        app.loginStatus = true;
                        alert("login success");
                        window.location.assign("http://localhost:8080/web/games.html")
                    }
                    if (data.status == 401) {
                        alert("Help!!!!!!!!!!!!!!!!!!!");
                    }


                })
                .catch(function (error) {
                    console.log('Request failure: ', error);
                });

        },


        getBody: function (json) {
            var body = [];
            for (var key in json) {
                var encKey = encodeURIComponent(key);
                var encVal = encodeURIComponent(json[key]);
                body.push(encKey + "=" + encVal);
            }
            return body.join("&");
        },


        logout: function () {

            fetch("/api/logout")
                .then(function (data) {

                    console.log('Request success:logout', data);
                    app.makeSthActive("login");
                })
                .catch(function (error) {
                    console.log('Request failure: ', error);


                });
        },

        makeSthActive: function (item) {
            this.activeSth = item;
            console.log(this.activeSth)
        },

        signup: function () {

            var json = {
                userName: this.userName,
                password: this.password
            }

            fetch("/api/players", {
                    credentials: 'include',

                    headers: {
                        'Content-Type': 'application/json'
                    },
                    method: 'POST',
                    body: JSON.stringify(json)

                })
                .then(function (data) {

                    return data.json();


                })
                .then(function (data) {
                    console.log(data);
                    if (data.error == "Missing data") {
                        alert("Missing data");
                    } else if (data.error == "Name already in use") {
                        alert("Name already in use");
                    } else {
                        alert("Signup success");
                        app.login();
                    }
                })
                .catch(function (error) {
                    console.log('Request failure: ', error);
                });

        }

    },

    computed: {

    },
    created: function () {
        this.getGameData();
    }
});




//        changePlayerData: function () {
//
//            for (var key in this.games) {
//                for (var i = 0; i < 2; i++) {
//                    if (this.games[key].gamePlayer[0] == null) {
//                        this.games[key].gamePlayer[0] = {
//                            player: {
//                                name: "join this game"   
//                            }
//                        };
//                    }
//                    if (this.games[key].gamePlayer[1] == null) {
//                        this.games[key].gamePlayer[1] = {
//                            player: {
//                                name: "join this game"
//                            }
//                        };
//                    }
//                }
//            }
//            console.log(this.games)
//        },
