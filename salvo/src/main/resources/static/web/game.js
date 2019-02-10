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
        gameView: {},
        allSalvos: [],
        mySalvos: {},
        allSalvosDone: [],
        opponentSlavos: {},
        ships: ["Submarine", "Patrol Boat", "Destroyer"],
        currentPlayerName: "",
        opponentPlayerName: "",
        currentGpId: "",
        shipHeadId: null,
        shipName: null,
        shipBodyIds: [],
        yourShipsBodysIds: [],
        ship: {},
        shipDirection: "",
        yourShips: [],
        createShipBottonStatus: "active",
        CarrierIsSelected: false,
        BattleshipIsSelected: false,
        SubmarineIsSelected: false,
        DestroyerIsSelected: false,
        PatrolBoatIsSelected: false,
        salvo: {},
        yourSalvos: [],
        aim: false,
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
                    console.log(json)
                    app.gameView = json.success;
                    app.mySHips = app.gameView.ships;

                    console.log(app.gameView);
                    console.log(app.mySHips);

                    app.getCurrentPlayers();
                    console.log(app.currentPlayerName);

                    app.allSalvos = app.gameView.salvos;
                    console.log(app.allSalvos);

                    app.getCurrentPlayersSalvos();
                    console.log(app.mySalvos);
                    console.log(app.opponentSlavos);

                    app.myShipsTable();
                    console.log(app.yourShips.length);
                    if (app.yourShips.length != 0) {
                        app.CarrierIsSelected = true;
                        app.BattleshipIsSelected = true;
                        app.SubmarineIsSelected = true;
                        app.DestroyerIsSelected = true;
                        ap.PatrolBoatIsSelected = true;
                    }

                    console.log(app.mySalvos);
                    app.mySalvosTable();
                    app.opponentSlavosTable();

                })
                .catch(function (error) {
                    console.log("Request failed: " + error.message);
                });
        },

        getCurrentPlayers: function () {

            for (var key in this.gameView.gamePlayers) {
                if (this.currentGpId == this.gameView.gamePlayers[key].id) {
                    this.currentPlayerName = this.gameView.gamePlayers[key].player.name;
                }
            }
        },

        getCurrentPlayersSalvos: function () {
            console.log(this.allSalvos);
            for (var key in this.allSalvos) {
                console.log(this.allSalvos[key])
                for (var key2 in this.allSalvos[key]) {
                    console.log(Object.keys(this.allSalvos[key][key2]));
                    if (Object.keys(this.allSalvos[key][key2]) == this.currentGpId) {
                        this.mySalvos = this.allSalvos[key];
                    } else {
                        this.opponentSlavos = this.allSalvos[key];
                    }
                }
            }
        },

        placeShips: function (gpId) {
            console.log(gpId);

            fetch("/api/games/players/" + gpId + "/ships", {
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    method: 'POST',
                    body: JSON.stringify(this.yourShips)

                })
                .then(function (data) {

                    return data.json();

                })
                .then(function (data) {
                    console.log(data);
                    if (data.no) {
                        alert(data.no)
                        window.location.reload(this.GameViewUrl);
                    } else if (data.cross) {
                        alert(data.cross)
                        window.location.reload(this.GameViewUrl);
                    } else if (data.insufficient) {
                        alert(data.insufficient)
                        window.location.reload(this.GameViewUrl);
                    } else if (data.overlap) {
                        alert(data.overlap)
                        window.location.reload(this.GameViewUrl);
                    } else {
                        window.location.reload(this.GameViewUrl);
                        this.createShipBottonStatus = "inactive";
                    }
                })
                .catch(function (error) {
                    console.log('Request failure: ', error);
                });

        },

        aiming: function (item) {
            this.aim = item;
        },

        salvosFire: function (gpId) {
            console.log(gpId);
            console.log("Ron", JSON.stringify({
                "fireLocations": this.yourSalvos
            }))
            fetch("/api/games/players/" + gpId + "/salvos", {
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    method: 'POST',
                    body: JSON.stringify({
                        "fireLocations": this.yourSalvos
                    })

                })
                .then(function (data) {
                    return data.json();
                })
                .then(function (data) {
                    console.log(data);
                    if (data.no) {
                        alert(data.no)
                        window.location.reload(this.GameViewUrl);
                    } else if (data.sorry) {
                        alert(data.sorry)
                        window.location.reload(this.GameViewUrl);
                    } else if (data.insufficient) {
                        alert(data.insufficient)
                        window.location.reload(this.GameViewUrl);
                    } else if (data.cross) {
                        alert(data.cross)
                        window.location.reload(this.GameViewUrl);
                    } else if (data.finish) {
                        alert(data.finish)
                        window.location.reload(this.GameViewUrl);
                    } else {
                        console.log("Request success:", data.success)
                        window.location.reload(this.GameViewUrl);
                    }
                })
                .catch(function (error) {
                    console.log('Request failure: ', error);
                });

        },

        mouseover: function (id) {
            console.log('mouse in');
            var overId = Array.from(id);
            console.log(overId);
            console.log(overId[0]);
            if (overId[0] == "P") {

                if (this.aim == true) {
                    var salvosLocation = document.getElementById(id);
                    if (salvosLocation == null) {

                    } else {
                        if (this.yourSalvos.includes(id)) {

                        } else if (this.allSalvosDone.includes(id)) {


                        } else {
                            salvosLocation.style.backgroundColor = "red";
                            console.log(overId);
                        }

                    }
                } else {

                }

            } else {
                for (var i = 0; i < this.ship.dimension; i++) {
                    if (this.shipDirection == "horizontal") {
                        var idPlus = overId[0] + (parseInt(overId[1]) + i);
                        console.log("idPlus-in-1:-----------------" + idPlus);
                    }
                    if (this.shipDirection == "vertical") {
                        var idPlus = this.tableRows[this.tableRows.indexOf(overId[0]) + i] + overId[1];
                        console.log("idPlus-in-2:-----------------" + idPlus);
                    }

                    var shipLocation = document.getElementById(idPlus);

                    if (shipLocation == null) {

                    } else {
                        shipLocation.style.backgroundColor = "yellow";
                    }
                }
            }
        },

        mouseout: function (id) {
            console.log('mouse out');
            var overId = Array.from(id);
            if (overId[0] == "P") {
                if (this.aim == true) {
                    var salvosLocation = document.getElementById(id);
                    if (salvosLocation == null) {} else {
                        if (this.yourSalvos.includes(id)) {

                        } else if (this.allSalvosDone.includes(id)) {


                        } else {
                            salvosLocation.style.backgroundColor = "";
                            console.log(overId);
                        }

                    }
                } else {

                }
            } else {
                if (this.yourShips.length == 0) {
                    for (var i = 0; i < this.ship.dimension; i++) {

                        if (this.shipDirection == "horizontal") {
                            var idPlus = overId[0] + (parseInt(overId[1]) + i);
                            console.log("idPlus-0ut-1:" + idPlus);
                        }


                        if (this.shipDirection == "vertical") {
                            var idPlus = this.tableRows[this.tableRows.indexOf(overId[0]) + i] + overId[1];
                            console.log("idPlus-in-2:-----------------" + idPlus);
                        }

                        var shipLocation = document.getElementById(idPlus);

                        if (shipLocation == null) {

                        } else {
                            shipLocation.style.backgroundColor = "";
                        }
                    }
                }

                if (this.yourShips.length != 0) {

                    for (var i = 0; i < this.ship.dimension; i++) {

                        if (this.shipDirection == "horizontal") {
                            var idPlus = overId[0] + (parseInt(overId[1]) + i);
                            console.log("idPlus-0ut-2:" + idPlus);
                        }

                        if (this.shipDirection == "vertical") {
                            var idPlus = this.tableRows[this.tableRows.indexOf(overId[0]) + i] + overId[1];
                            console.log("idPlus-out-2:-----------------" + idPlus);
                        }

                        var shipLocation = document.getElementById(idPlus);

                        if (shipLocation == null) {

                        } else {
                            shipLocation.style.backgroundColor = "";
                        }
                    }

                    for (var key in this.yourShips) {
                        for (var i = 0; i < this.yourShips[key].locations.length; i++) {
                            document.getElementById(this.yourShips[key].locations[i]).style.backgroundColor =
                                this.yourShips[key].color;
                        }
                    }
                }
            }
        },

        myShipsTable: function (ships) {
            console.log(ships)
            console.log(this.mySHips)
            var arrTempOpponentSalvosS = [];

            console.log(arrTempOpponentSalvosS)

            var tablebody = document.getElementById("myShipsTb");
            var tableRows = tablebody.rows;
            var carriersIds = [];
            var BattleshipsIds = [];
            var SubmarinesIds = [];
            var PatrolBoatsIds = [];
            var DestroyersIds = [];


            for (var i = 0; i < this.mySHips.length; i++) {
                for (var j = 0; j < tableRows.length; j++) {
                    for (var n = 0; n < tableRows[j].childNodes.length; n++) {

                        if (this.mySHips[i].locations.indexOf(tableRows[j].childNodes[n].id) > -1 && this.mySHips[i].shipType == "Carrier") {
                            carriersIds.push(tableRows[j].childNodes[n].id);
                        }

                        if (this.mySHips[i].locations.indexOf(tableRows[j].childNodes[n].id) > -1 && this.mySHips[i].shipType == "Battleship") {
                            BattleshipsIds.push(tableRows[j].childNodes[n].id);
                        }

                        if (this.mySHips[i].locations.indexOf(tableRows[j].childNodes[n].id) > -1 && this.mySHips[i].shipType == "Submarine") {
                            SubmarinesIds.push(tableRows[j].childNodes[n].id);
                        }

                        if (this.mySHips[i].locations.indexOf(tableRows[j].childNodes[n].id) > -1 && this.mySHips[i].shipType == "PatrolBoat") {
                            PatrolBoatsIds.push(tableRows[j].childNodes[n].id);
                        }

                        if (this.mySHips[i].locations.indexOf(tableRows[j].childNodes[n].id) > -1 && this.mySHips[i].shipType == "Destroyer") {
                            DestroyersIds.push(tableRows[j].childNodes[n].id);
                        }
                    }
                }
            }

            console.log("carriersIds:----" + carriersIds)
            console.log("BattleshipsIds:----" + BattleshipsIds)
            console.log("SubmarinesIds:----" + SubmarinesIds)
            console.log("PatrolBoatsIds:----" + PatrolBoatsIds)
            console.log("DestroyersIds:----" + DestroyersIds)

            if (carriersIds != []) {
                for (var x = 0; x < carriersIds.length; x += 5) {
                    console.log("every carriersIds:----" + carriersIds[x])
                    if (Array.from(carriersIds[x])[0] == Array.from(carriersIds[x + 1])[0]) {
                        document.getElementById(carriersIds[x]).setAttribute("class", "carrierImgH")
                    } else {
                        document.getElementById(carriersIds[x]).setAttribute("class", "carrierImgV")
                    }
                }
            }

            if (BattleshipsIds != []) {
                for (var x = 0; x < BattleshipsIds.length; x += 4) {
                    console.log("every BattleshipsIds:----" + BattleshipsIds[x])
                    if (Array.from(BattleshipsIds[x])[0] == Array.from(BattleshipsIds[x + 1])[0]) {
                        document.getElementById(BattleshipsIds[x]).setAttribute("class", "battleshipH")
                    } else {
                        document.getElementById(BattleshipsIds[x]).setAttribute("class", "battleshipV")
                    }
                }
            }

            if (SubmarinesIds != []) {
                for (var x = 0; x < SubmarinesIds.length; x += 3) {
                    console.log("every SubmarinesIds:----" + SubmarinesIds[x])
                    if (Array.from(SubmarinesIds[x])[0] == Array.from(SubmarinesIds[x + 1])[0]) {
                        document.getElementById(SubmarinesIds[x]).setAttribute("class", "submarinesH")
                    } else {
                        document.getElementById(SubmarinesIds[x]).setAttribute("class", "submarinesV")
                    }
                }
            }


            if (PatrolBoatsIds != []) {
                for (var x = 0; x < PatrolBoatsIds.length; x += 2) {
                    console.log("every PatrolBoatsIds:----" + PatrolBoatsIds[x])
                    if (Array.from(PatrolBoatsIds[x])[0] == Array.from(PatrolBoatsIds[x + 1])[0]) {
                        document.getElementById(PatrolBoatsIds[x]).setAttribute("class", "patrolboatH")
                    } else {
                        document.getElementById(PatrolBoatsIds[x]).setAttribute("class", "patrolboatV")
                    }
                }
            }

            if (DestroyersIds != []) {
                for (var x = 0; x < DestroyersIds.length; x += 3) {
                    console.log("every DestroyersIds:----" + DestroyersIds[x])
                    if (Array.from(DestroyersIds[x])[0] == Array.from(DestroyersIds[x + 1])[0]) {
                        document.getElementById(DestroyersIds[x]).setAttribute("class", "destroyerH")
                    } else {
                        document.getElementById(DestroyersIds[x]).setAttribute("class", "destroyerV")
                    }
                }
            }

        },

        /*-------------------place different ship---------------------*/

        creatShips: function (shipType, dimension, color) {
            this.ship = {
                "shipType": shipType,
                "locations": [],
                "dimension": dimension,
                "color": color
            }
            console.log(this.ship)
        },

        creatSalvos: function (turn, gp) {
            this.salvo = {
                "turn": turn,
                "fireLocations": [],
                "gamePlayer": gp,
            }
            console.log(this.salvo)
        },

        changeDirection: function (shipDirection) {
            this.shipDirection = shipDirection;
        },

        createShipBottonSwitch: function (stauts) {
            this.createShipBottonStatus == status;
        },

        selecteButton: function (selectedShip) {
            if (selectedShip == "Carrier") {
                this.CarrierIsSelected = true;
            } else if (selectedShip == "Battleship") {
                this.BattleshipIsSelected = true;
            } else if (selectedShip == "Submarine") {
                this.SubmarineIsSelected = true;
            } else if (selectedShip == "Destroyer") {
                this.DestroyerIsSelected = true;
            } else if (selectedShip == "PatrolBoat") {
                this.PatrolBoatIsSelected = true;
            } else {
                return false;
            }
        },

        shipPlace: function (id) {

            console.log('click:' + id);
            console.log(this.yourShips)

            var shipLocationTemp = [];

            for (var i = 0; i < this.ship.dimension; i++) {

                if (this.shipDirection == "horizontal") {
                    var idPlus = overId[0] + (parseInt(overId[1]) + i);
                    console.log("idPlus-click-A1:" + idPlus);
                }

                if (this.shipDirection == "vertical") {
                    var idPlus = this.tableRows[this.tableRows.indexOf(overId[0]) + i] + overId[1];
                    console.log("idPlus-click-A2:-----------------" + idPlus);
                }

                var shipLocation = document.getElementById(idPlus);
                if (shipLocation == null) {

                }
                if (shipLocation != null) {
                    shipLocationTemp.push(idPlus);
                }
            }

            console.log("shipLocationTemp:----" + shipLocationTemp);
            console.log("this.ship.dimension:----" + this.ship.dimension);


            if (shipLocationTemp.length < this.ship.dimension) {
                alert("you r placing you ship on the beach now!!!")
                return;
            }

            if (shipLocationTemp.length == this.ship.dimension) {
                this.ship.locations = shipLocationTemp;
                if (this.yourShips.length != 0) {

                    var status = true;

                    console.log(this.yourShips);
                    for (var key in this.yourShips) {
                        for (var i = 0; i < this.ship.locations.length; i++) {
                            if (this.yourShips[key].locations.includes(this.ship.locations[i])) {
                                alert("there is a ship place here!!!")
                                status = false;
                                break;
                            }
                        }
                    }

                    console.log(status);

                    if (status == true) {
                        for (var i = 0; i < this.ship.dimension; i++) {

                            if (this.shipDirection == "horizontal") {
                                var idPlus = overId[0] + (parseInt(overId[1]) + i);
                                console.log("idPlus-click-B1:" + idPlus);
                            }

                            if (this.shipDirection == "vertical") {
                                var idPlus = this.tableRows[this.tableRows.indexOf(overId[0]) + i] + overId[1];
                                console.log("idPlus-click-B2:-----------------" + idPlus);
                            }

                            var shipLocation = document.getElementById(idPlus);
                            shipLocation.style.backgroundColor = this.ship.color;
                            shipLocation.innerHTML = idPlus;
                        }
                        this.yourShips.push(this.ship);
                    } else {
                        return;
                    }


                    console.log(this.yourShips);


                } else {
                    for (var i = 0; i < this.ship.dimension; i++) {

                        if (this.shipDirection == "horizontal") {
                            var idPlus = overId[0] + (parseInt(overId[1]) + i);
                            console.log("idPlus-click-C1:------------------" + idPlus);
                        }

                        if (this.shipDirection == "vertical") {
                            var idPlus = this.tableRows[this.tableRows.indexOf(overId[0]) + i] + overId[1];
                            console.log("idPlus-click-C2:-----------------" + idPlus);
                        }

                        var shipLocation = document.getElementById(idPlus);
                        shipLocation.style.backgroundColor = this.ship.color;
                        shipLocation.innerHTML = idPlus;
                    }
                    this.yourShips.push(this.ship);
                }
            }

            this.ship = {};
        },

        salvosAim: function (id) {
            if (this.aim == true) {
                console.log('click************************************:' + id);

                var overId = Array.from(id);

                var salvosLocation = document.getElementById(id);
                if (salvosLocation == null) {

                } else {
                    if (this.yourSalvos.includes(id)) {
                        this.yourSalvos.slice(this.yourSalvos.indexOf(id), 1);
                        salvosLocation.style.backgroundColor = "";

                    } else {
                        this.yourSalvos.push(id);
                        salvosLocation.style.backgroundColor = "blue";
                    }
                }

                console.log("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&7" + this.yourSalvos)
            } else {
                alert("you need to click aim!!!!")
            }
        },

        /*-------------------change table cell color---------------------*/

        mySalvosTable: function () {
            console.log("---------------", this.mySalvos)
            for (var key in this.mySalvos) {
                console.log(this.mySalvos[key])
                for (var key2 in this.mySalvos[key]) {
                    console.log(this.mySalvos[key][key2])
                    this.mySalvos[key][key2].forEach(el => {
                        console.log(el)
                        document.getElementById(el).style.backgroundColor = "black";
                        this.allSalvosDone.push(el);
                    })
                }
            }
        },
        opponentSlavosTable: function () {
            console.log("---------------", this.opponentSlavos)
            for (var key in this.opponentSlavos) {
                console.log(this.opponentSlavos[key])
                for (var key2 in this.opponentSlavos[key]) {
                    console.log(this.opponentSlavos[key][key2])
                    this.opponentSlavos[key][key2].forEach(el => {
                        console.log(el.replace("PL", ""))
                        var someDiv = document.createElement("div");
                        someDiv.setAttribute("class", "opponentFireLocation");
                        document.getElementById(el.replace("PL", "")).append(someDiv);
                    })
                }
            }
        }
    },

    computed: {

    },
    created: function () {
        this.getGameViewData();
    }

});
