/* eslint-env browser */
/* eslint "no-console": "off"  */
/* global$ */



//var GameViewUrl="http://localhost:8080/api/game_view/"+document.querySelector('[data-page]').getAttribute("data-page");

//console.log(GameViewUrl);




var app = new Vue({
    el: "#vue-gameView-app",
    data: {
        GameViewDataUrl: "",
        realGameViewUrl: "",
        tableColums: ["", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
        tableColumsForIDs: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
        tableRows: ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"],
        mySHips: [],
        gameView: [],
        mySlavos: {},
        opponentSlavos: {},
        ships: ["Submarine", "Patrol Boat", "Destroyer"],
        currentPlayers1: "",
        currentPlayers2: "",
        currentGpId: "",
        json: []
    },

    methods: {

        getGameViewData: function () {

            var url_string = window.location.href
            var url = new URL(url_string);
            var c = url.searchParams.get("gp");
            console.log(c);
            this.GameViewUrl = "http://localhost:8080/api/game_view/" + c;
            this.currentGpId = c;
            console.log(this.GameViewUrl);

            fetch(this.GameViewUrl, {
                    method: "GET",
                })
                .then(function (response) {
                    if (response.ok) {
                        return response.json();
                    }
                    throw new Error(response.statusText);
                })
                .then(function (json) {
                    app.gameView = json.success;
                    app.mySHips = app.gameView.ships;

                    console.log(app.gameView);
                    console.log(app.mySHips);

                    app.currentPlayers1 = app.gameView.gamePlayers[0].player.name;
                    app.currentPlayers2 = app.gameView.gamePlayers[1].player.name;

                    app.mySlavos = app.gameView.salvos[0];
                    console.log(app.mySlavos);
                    app.opponentSlavos = app.gameView.salvos[1];
                    console.log(app.opponentSlavos);

                    app.changeTableColor();
                    app.mainPlayerSalvos();

                })
                .catch(function (error) {
                    console.log("Request failed: " + error.message);
                });
        },

        creatShips: function (shipType) {

            var ship1 = {
                shipType: "Submarine",
                locations: ["A1", "A2", "A3"]
            }

            var ship2 = {
                shipType: "Patrol Boat",
                locations: ["B5", "B6"]
            }

            var ship3 = {
                shipType: "Destroyer",
                locations: ["C1", "C2", "C3"]
            }


            if (shipType == "Submarine") {
                this.json.push(ship1);
            } else if (shipType == "Patrol Boat") {
                this.json.push(ship2);
            } else {
                this.json.push(ship3);
            }


        },

        placeShips: function (gpId) {


            console.log(gpId);
            //console.log(shipType);


//            var ship1 = {
//                shipType: "Submarine",
//                locations: ["A1", "A2", "A3"]
//            }
//
//            var ship2 = {
//                shipType: "Patrol Boat",
//                locations: ["B5", "B6"]
//            }
//
//            var ship3 = {
//                shipType: "Destroyer",
//                locations: ["C1", "C2", "C3"]
//            }
//
//
//            if (shipType == "Submarine") {
//                this.json.push(ship1);
//            } else if (shipType == "Patrol Boat") {
//                this.json.push(ship2);
//            } else {
//                this.json.push(ship3);
//            }



            fetch("/api/games/players/" + gpId + "/ships", {
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    method: 'POST',
                    body: JSON.stringify(this.json)

                })
                .then(function (data) {

                    return data.json();

                })
                .then(function (data) {
                    console.log(data);
                    if (data.whoops) {
                        alert(data.whoops)
                    } else {
                        window.location.reload();
                    }
                })
                .catch(function (error) {
                    console.log('Request failure: ', error);
                });



        },


        changeTableColor: function () {
            var arrTempOpponentSalvosS = [];
            for (var key in this.opponentSlavos) {
                for (var key2 in this.opponentSlavos[key]) {
                    this.opponentSlavos[key][key2].forEach(el => arrTempOpponentSalvosS.push(el));
                }
            }

            console.log(arrTempOpponentSalvosS)

            var tablebody = document.getElementById("someGameTb");
            var tableRows = tablebody.rows;


            for (var i = 0; i < this.mySHips.length; i++) {
                for (var j = 0; j < tableRows.length; j++) {
                    for (var n = 0; n < tableRows[j].childNodes.length; n++) {
                        if (this.mySHips[i].locations.indexOf(tableRows[j].childNodes[n].id) > -1 && this.mySHips[i].shipType == "Submarine") {
                            tableRows[j].childNodes[n].style.background = "url(https://i.pinimg.com/originals/dc/88/be/dc88be75c82194e96ab01c0d33fe9175.jpg)";
                            tableRows[j].childNodes[n].style.backgroundSize = "cover";
                        }

                        if (this.mySHips[i].locations.indexOf(tableRows[j].childNodes[n].id) > -1 && this.mySHips[i].shipType == "Patrol Boat") {
                            tableRows[j].childNodes[n].style.background = "url(https://i.pinimg.com/originals/e1/b2/a5/e1b2a52e6118cd9ac43296d6b9c40cd5.png)";
                            tableRows[j].childNodes[n].style.backgroundSize = "cover";
                        }

                        if (this.mySHips[i].locations.indexOf(tableRows[j].childNodes[n].id) > -1 && this.mySHips[i].shipType == "Destroyer") {
                            tableRows[j].childNodes[n].style.background = "url(https://cdn.dribbble.com/users/1008697/screenshots/2650264/postboat_1x.jpg)";
                            tableRows[j].childNodes[n].style.backgroundSize = "cover";
                        }

                        if (this.mySHips[i].locations.indexOf(tableRows[j].childNodes[n].id) > -1 && this.ships.includes(this.mySHips[i].shipType) && arrTempOpponentSalvosS.includes(tableRows[j].childNodes[n].id)) {
                            tableRows[j].childNodes[n].style.background = "red";
                            tableRows[j].childNodes[n].innerHTML = tableRows[j].childNodes[n].id;
                        }
                    }
                }
            }
        },


        mainPlayerSalvos: function () {

            var arrTempMySalvos = [];

            for (var key in this.mySlavos) {
                for (var key2 in this.mySlavos[key]) {
                    this.mySlavos[key][key2].forEach(el => {
                        document.getElementById("PL" + el).style.background = "blue";
                        document.getElementById("PL" + el).innerHTML = "PL" + el;

                        arrTempMySalvos.push(el)

                    });
                }
            }
        }

    },

    computed: {

    },
    created: function () {
        //this.getGameUrl();
        this.getGameViewData();

    }

});



//            var json = [{
//                    shipType: "Submarine",
//                    locations: ["A1", "A2", "A3"]
//                        }, {
//                    shipType: "Patrol Boat",
//                    locations: ["B5", "B6"]
//                        }, {
//                    shipType: "Destroyer",
//                    locations: ["C1", "C2", "C3"]
//                        }
//                       ]
