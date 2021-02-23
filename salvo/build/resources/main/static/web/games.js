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
        currentPlayerGpId: "",
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
                    console.log(app.currentPlayerGpId)
                    console.log("currentUser--------------", app.currentPlayer.id);
                    if (json.player.id) {
                        app.makeSthActive("logout");
                        app.loginStatus = true;
                    }
                    app.games = json.games;
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
                    if (data.fail) {
                        alert(data.fail);
                    } else {
                        this.currentPlayerGpId = data.gp1;
                        console.log(this.currentPlayerGpId)
                        alert("create game success");
                        window.location.assign("http://localhost:8080/web/games.html")

                    }

                })
                .catch(function (error) {
                    console.log('Request failure: ', error);
                });

        },


        joinGame: function (gId, pId, gpId) {

            console.log("gId:" + gId);
            console.log("pId:" + pId);
            console.log("gpId:" + gpId);
            console.log(this.currentPlayer.name + "----" + this.currentPlayer.id)
            var currentGame = {};
            console.log(this.games)
            for (var x = 0; x < this.games.length; x++) {
                if (gId == this.games[x].id) {
                    currentGame = this.games[x];
                    break;
                }
            }


            if (currentGame.gamePlayer.length < 2 && (pId != null && pId != currentGame.gamePlayer[0].player.id)) {
                alert("you can not cheat-------------por favor!!!")

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
                            console.log(data.tip);
                        } else if (data.currentUserGpID1) {
                            if (data.currentUserGpID1 == gpId) {
                                window.location.assign("/web/game.html?gp=" + data.currentUserGpID1)
                            } else {
                                alert("you can not cheat,por favor!!!")
                            }
                        } else if (data.sorry) {
                            alert(data.sorry);
                        } else if (data.currentUserGpID2) {
                            if (data.currentUserGpID2 == gpId) {
                                window.location.assign("/web/game.html?gp=" + data.currentUserGpID2)
                            } else {
                                alert("you can not play with yourself,por favor!!!")
                            }

                        } else {
                            if (gpId == null) {
                                console.log(data.gp2Id);
                                alert("where come to this game!!!!");
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


