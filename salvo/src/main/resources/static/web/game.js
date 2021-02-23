/* eslint-env browser */
/* eslint "no-console": "off"  */
/* global$ */


var app = new Vue({
    el: "#vue-gameView-app",
    data: {
        sound: null,
        GameViewDataUrl: "",
        realGameViewUrl: "",
        opponentGameViewDataUrl: null,
        tableColums: ["XYZ", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
        tableColumsForIDs: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
        tableRows: ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"],
        mySHips: [],
        gameView: {},
        allSalvos: [],
        mySalvos: {},
        allSalvosDone: [],
        opponentSlavos: {},
        ships: ["Submarine", "PatrolBoat", "Destroyer"],
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
        yourShipsReady: false,
        yourShipsFromBackLength: 0,
        oppShipsFromBackLength: 0,
        createShipBottonStatus: "active",
        CarrierIsSelected: false,
        BattleshipIsSelected: false,
        SubmarineIsSelected: false,
        DestroyerIsSelected: false,
        PatrolBoatIsSelected: false,
        salvo: {},
        yourSalvos: [],
        yourSalvosReady: false,
        aim: false,
        gamePlayers: [],
        hitsOnYou: {},
        hitsOnYouClone: {},
        hitOnYouUpdateData: [],
        lastTurnHitOnYou: {},
        lastTurnHitOnYouArr: [],
        hitsOnOppent: {},
        hitsOnOppentClone: {},
        hitsOnOppentUpdateData: [],
        lastTurnHitOnOpp: {},
        lastTurnHitOnOppArr: [],
        lastTurnObj: {},
        lastTurn: "",
        hitsOnOppent: {},
        gameStatus: "yourTurn",
        gameReadyStatus: "",
        currentTurn: 1,
        youScore: 0,
        json: []
    },

    methods: {

        getGameViewData: function () {

            var url_string = window.location.href
            var url = new URL(url_string);
            var c = url.searchParams.get("gp");
            this.GameViewUrl = "http://localhost:8080/api/game_view/" + c;
            this.currentGpId = c;

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
                    app.gamePlayers = app.gameView.gamePlayers;
                    app.mySHips = app.gameView.ships;

                    app.yourShipsFromBackLength = app.gameView.ships.length;
                    app.oppShipsFromBackLength = app.gameView.oppShipsFromBackLength;
                    console.log("yourShipsFromBackLength-----------------", app.yourShipsFromBackLength)
                    console.log("oppShipsFromBackLength-----------------", app.oppShipsFromBackLength)


                    app.gameStatus = app.gameView.gameStatus;
                    console.log("gameStatus-----------------", app.gameStatus)

                    //                    if (app.gameStatus == "yourTurn") {
                    //                        app.countdown();
                    //                    }

                    app.countdown();

                    app.youScore = app.gameView.score;

                    app.hitsOnYou = app.gameView.hitsOnYou;
                    app.hitsOnOppent = app.gameView.hitsOnOppent;

                    app.hitsOnYouClone = Object.assign({}, app.hitsOnYou);
                    app.hitsOnOppentClone = Object.assign({}, app.hitsOnOppent);

                    app.hitOnYouUpdateData = app.hitUpdate(app.hitsOnYouClone);
                    app.lastTurnHitOnYou = app.hitOnYouUpdateData[app.hitOnYouUpdateData.length - 1]

                    app.hitsOnOppentUpdateData = app.hitUpdate(app.hitsOnOppentClone);
                    app.lastTurnHitOnOpp = app.hitsOnOppentUpdateData[app.hitsOnOppentUpdateData.length - 1]

                    app.currentTurn = app.hitsOnOppentUpdateData.length + 1;
                    console.log("currentTurn-----------------", app.currentTurn)


                    app.getCurrentPlayers();

                    app.allSalvos = app.gameView.salvos;
                    console.log("allSalvos-----------------", app.allSalvos)


                    app.getCurrentPlayersSalvos();

                    app.myShipsTable();

                    if (app.yourShips.length != 0) {
                        app.CarrierIsSelected = true;
                        app.BattleshipIsSelected = true;
                        app.SubmarineIsSelected = true;
                        app.DestroyerIsSelected = true;
                        ap.PatrolBoatIsSelected = true;
                    }

                    app.gameViewUpDate();
                    app.mySalvosmiss();
                    app.hitUpdateTabe();
                    //app.countdown();



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
            for (var key in this.allSalvos) {
                for (var key2 in this.allSalvos[key]) {
                    if (Object.keys(this.allSalvos[key][key2]) == this.currentGpId) {
                        this.mySalvos = this.allSalvos[key];
                    } else {
                        this.opponentSlavos = this.allSalvos[key];
                    }
                }
            }
        },

        placeShips: function (gpId) {

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

                        app.knowShipsReady();

                        if (app.yourShipsReady == true) {
                            setTimeout(function () {
                                window.location.reload(this.GameViewUrl);
                                this.createShipBottonStatus = "inactive";
                            }, 1500);
                        } else {
                            window.location.reload(this.GameViewUrl);
                        }

                    }
                })
                .catch(function (error) {
                    console.log('Request failure: ', error);
                });

        },

        knowShipsReady: function () {
            if (this.yourShips.length == 5) {
                this.yourShipsReady = true;
            } else {
                this.yourShipsReady = false;
            }
        },

        aiming: function (item) {
            this.aim = item;
        },

        salvosFire: function (gpId) {
            gpId = this.currentGpId;

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
                    } else if (data.success) {

                        app.knowSalvoReady();

                        if (app.yourSalvosReady == true) {
                            setTimeout(function () {
                                window.location.reload(this.GameViewUrl);
                            }, 1000);
                        } else {
                            window.location.reload(this.GameViewUrl);
                        }



                    } else {

                        alert(data.finish)
                        window.location.reload(this.GameViewUrl);

                    }
                })
                .catch(function (error) {
                    console.log('Request failure: ', error);
                });

        },

        knowSalvoReady: function () {
            if (this.yourSalvos.length == 5) {
                this.yourSalvosReady = true;
            } else {
                this.yourSalvosReady = false;
            }
        },

        mouseover: function (id) {
            var overId = Array.from(id);
            //console.log(overId);
            //console.log(overId[0]);
            if (overId[0] == "P") {

                if (this.aim == true) {
                    var salvosLocation = document.getElementById(id);
                    if (salvosLocation == null) {

                    } else {
                        if (this.yourSalvos.includes(id)) {

                        } else if (this.allSalvosDone.includes(id)) {


                        } else {
                            salvosLocation.style.backgroundColor = "red";
                            //console.log(overId);
                        }

                    }
                } else {

                }

            } else {
                for (var i = 0; i < this.ship.dimension; i++) {
                    if (this.shipDirection == "horizontal") {
                        var idPlus = overId[0] + (parseInt(overId[1]) + i);
                        //console.log("idPlus-in-1:-----------------" + idPlus);
                    }
                    if (this.shipDirection == "vertical") {
                        var idPlus = this.tableRows[this.tableRows.indexOf(overId[0]) + i] + overId[1];
                        //console.log("idPlus-in-2:-----------------" + idPlus);
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
            var overId = Array.from(id);
            if (overId[0] == "P") {
                if (this.aim == true) {
                    var salvosLocation = document.getElementById(id);
                    if (salvosLocation == null) {} else {
                        if (this.yourSalvos.includes(id)) {

                        } else if (this.allSalvosDone.includes(id)) {


                        } else {
                            salvosLocation.style.backgroundColor = "";
                            //console.log(overId);
                        }

                    }
                } else {

                }
            } else {
                if (this.yourShips.length == 0) {
                    for (var i = 0; i < this.ship.dimension; i++) {

                        if (this.shipDirection == "horizontal") {
                            var idPlus = overId[0] + (parseInt(overId[1]) + i);
                            //console.log("idPlus-0ut-1:" + idPlus);
                        }


                        if (this.shipDirection == "vertical") {
                            var idPlus = this.tableRows[this.tableRows.indexOf(overId[0]) + i] + overId[1];
                            //console.log("idPlus-in-2:-----------------" + idPlus);
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
                            //console.log("idPlus-0ut-2:" + idPlus);
                        }

                        if (this.shipDirection == "vertical") {
                            var idPlus = this.tableRows[this.tableRows.indexOf(overId[0]) + i] + overId[1];
                            //console.log("idPlus-out-2:-----------------" + idPlus);
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
            var arrTempOpponentSalvosS = [];
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
                    if (Array.from(carriersIds[x])[0] == Array.from(carriersIds[x + 1])[0]) {
                        document.getElementById(carriersIds[x]).setAttribute("class", "carrierImgH")
                    } else {
                        document.getElementById(carriersIds[x]).setAttribute("class", "carrierImgV")
                    }
                }
            }

            if (BattleshipsIds != []) {
                for (var x = 0; x < BattleshipsIds.length; x += 4) {
                    if (Array.from(BattleshipsIds[x])[0] == Array.from(BattleshipsIds[x + 1])[0]) {
                        document.getElementById(BattleshipsIds[x]).setAttribute("class", "battleshipH")
                    } else {
                        document.getElementById(BattleshipsIds[x]).setAttribute("class", "battleshipV")
                    }
                }
            }

            if (SubmarinesIds != []) {
                for (var x = 0; x < SubmarinesIds.length; x += 3) {
                    if (Array.from(SubmarinesIds[x])[0] == Array.from(SubmarinesIds[x + 1])[0]) {
                        document.getElementById(SubmarinesIds[x]).setAttribute("class", "submarinesH")
                    } else {
                        document.getElementById(SubmarinesIds[x]).setAttribute("class", "submarinesV")
                    }
                }
            }


            if (PatrolBoatsIds != []) {
                for (var x = 0; x < PatrolBoatsIds.length; x += 2) {
                    if (Array.from(PatrolBoatsIds[x])[0] == Array.from(PatrolBoatsIds[x + 1])[0]) {
                        document.getElementById(PatrolBoatsIds[x]).setAttribute("class", "patrolboatH")
                    } else {
                        document.getElementById(PatrolBoatsIds[x]).setAttribute("class", "patrolboatV")
                    }
                }
            }

            if (DestroyersIds != []) {
                for (var x = 0; x < DestroyersIds.length; x += 3) {
                    if (Array.from(DestroyersIds[x])[0] == Array.from(DestroyersIds[x + 1])[0]) {
                        document.getElementById(DestroyersIds[x]).setAttribute("class", "destroyerH")
                    } else {
                        document.getElementById(DestroyersIds[x]).setAttribute("class", "destroyerV")
                    }
                }
            }

        },

        creatShips: function (shipType, dimension, color) {
            this.ship = {
                "shipType": shipType,
                "locations": [],
                "dimension": dimension,
                "color": color
            }
        },

        creatSalvos: function (turn, gp) {
            this.salvo = {
                "turn": turn,
                "fireLocations": [],
                "gamePlayer": gp,
            }
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
            var overId = Array.from(id);
            var shipLocationTemp = [];
            for (var i = 0; i < this.ship.dimension; i++) {
                if (this.shipDirection == "horizontal") {
                    var idPlus = overId[0] + (parseInt(overId[1]) + i);
                }

                if (this.shipDirection == "vertical") {
                    var idPlus = this.tableRows[this.tableRows.indexOf(overId[0]) + i] + overId[1];
                }

                var shipLocation = document.getElementById(idPlus);
                if (shipLocation == null) {

                }
                if (shipLocation != null) {
                    shipLocationTemp.push(idPlus);
                }
            }
            if (shipLocationTemp.length < this.ship.dimension) {
                alert("you r placing you ship on the beach now!!!")
                return;
            }
            if (shipLocationTemp.length == this.ship.dimension) {
                this.ship.locations = shipLocationTemp;
                if (this.yourShips.length != 0) {
                    var status = true;
                    for (var key in this.yourShips) {
                        for (var i = 0; i < this.ship.locations.length; i++) {
                            if (this.yourShips[key].locations.includes(this.ship.locations[i])) {
                                alert("there is a ship place here!!!")
                                status = false;
                                break;
                            }
                        }
                    }
                    if (status == true) {
                        for (var i = 0; i < this.ship.dimension; i++) {

                            if (this.shipDirection == "horizontal") {
                                var idPlus = overId[0] + (parseInt(overId[1]) + i);
                            }
                            if (this.shipDirection == "vertical") {
                                var idPlus = this.tableRows[this.tableRows.indexOf(overId[0]) + i] + overId[1];
                            }
                            var shipLocation = document.getElementById(idPlus);
                            shipLocation.style.backgroundColor = this.ship.color;
                            shipLocation.innerHTML = idPlus;
                        }
                        this.yourShips.push(this.ship);
                    } else {
                        return;
                    }

                } else {
                    for (var i = 0; i < this.ship.dimension; i++) {

                        if (this.shipDirection == "horizontal") {
                            var idPlus = overId[0] + (parseInt(overId[1]) + i);
                        }
                        if (this.shipDirection == "vertical") {
                            var idPlus = this.tableRows[this.tableRows.indexOf(overId[0]) + i] + overId[1];
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
                var overId = Array.from(id);
                var salvosLocation = document.getElementById(id);
                if (salvosLocation == null) {} else {
                    if (this.yourSalvos.includes(id)) {
                        this.yourSalvos.slice(this.yourSalvos.indexOf(id), 1);
                        salvosLocation.style.backgroundColor = "";
                    } else {
                        this.yourSalvos.push(id);
                        salvosLocation.style.backgroundColor = "blue";
                    }
                }
            } else {
                alert("you need to click aim!!!!")
            }
        },

        hitUpdate: function (obj) {
            var arr = [];
            var arrTemp = [];
            for (var key in obj) {
                arr.push(obj[key]);
            }
            var arrTemp = Array.from(arr);
            for (var x = 1; x < arrTemp.length; x++) {
                for (var key in arrTemp[x]) {
                    arrTemp[x][key].hitTimes = arrTemp[x][key].hitTimes + arrTemp[x - 1][key].hitTimes;
                    arrTemp[x][key].hitsPosition = arrTemp[x][key].hitsPosition.concat(arrTemp[x - 1][key].hitsPosition);
                }
            }
            for (var y = 0; y < arrTemp.length; y++) {
                var shipLeft = 5;
                for (var key in arrTemp[y]) {
                    if (arrTemp[y][key].hitTimes == arrTemp[y][key].shipLength) {
                        arrTemp[y][key]['shipStatus'] = "sunk";
                        shipLeft--;

                    } else if (arrTemp[y][key].hitTimes == 0) {
                        arrTemp[y][key]['shipStatus'] = "safe";
                    } else {
                        arrTemp[y][key]['shipStatus'] = "sink";
                    }
                }
                arrTemp[y]['shipLeft'] = shipLeft;
                arrTemp[y]['turn'] = 'turn' + (y + 1);
            }
            return arrTemp;
        },

        mySalvosmiss: function () {

            var lastTurnHitOnYou = this.hitOnYouUpdateData[this.hitOnYouUpdateData.length - 1];

            for (var key in lastTurnHitOnYou) {
                if (lastTurnHitOnYou[key].hitsPosition != null) {
                    lastTurnHitOnYou[key].hitsPosition.forEach(el => {
                        for (var x = 0; x < this.allSalvos.length; x++) {
                            for (var y = 0; y < this.allSalvos[x].fireLocations.length; y++) {
                                var salvoId = this.allSalvos[x].fireLocations[y];
                                if (salvoId != el) {
                                    document.getElementById(salvoId).setAttribute("class", "missingPoint");
                                } else {

                                }
                            }
                        }
                    });
                }
            }

            //hitUpdateTabe();

        },

        hitUpdateTabe: function () {
            var lastTurnHitOnYou = this.hitOnYouUpdateData[this.hitOnYouUpdateData.length - 1]
            for (var key in lastTurnHitOnYou) {
                if (lastTurnHitOnYou[key].hitsPosition != null) {

                    lastTurnHitOnYou[key].hitsPosition.forEach(el => {
                        //console.log("elllllllllllllllllllll---", el)
                        var hitStatus1 = document.createElement("div");
                        hitStatus1.setAttribute("class", "hitStatusTest1");
                        if (document.getElementById(el).firstChild != null) {
                            document.getElementById(el).removeChild();
                            document.getElementById(el).appendChild(hitStatus1);
                        } else {
                            document.getElementById(el).appendChild(hitStatus1);
                        }


                    });

                }
            }


            var lastTurnHitOnOpp = this.hitsOnOppentUpdateData[this.hitsOnOppentUpdateData.length - 1]
            for (var key in lastTurnHitOnOpp) {
                if (lastTurnHitOnOpp[key].hitsPosition != null) {
                    lastTurnHitOnOpp[key].hitsPosition.forEach(el => {
                        el = "PL" + el;
                        //console.log("eeeeeeeeeeeeeeeeeeeeeeeel---", el)
                        var hitStatus2 = document.createElement("div");
                        hitStatus2.setAttribute("class", "hitStatusTest1");
                        if (document.getElementById(el).firstChild != null) {
                            document.getElementById(el).removeChild();
                            document.getElementById(el).appendChild(hitStatus2);
                        } else {
                            document.getElementById(el).appendChild(hitStatus2);
                        }

                    });
                }
            }

        },

        gameViewUpDate: function () {

            console.log("in counter gamestatus is vvvvvvvvvvvvvvvvvvvvvvv", this.gameStatus)
            console.log("in counter score is sssssssssssssssssssssssssssss", this.youScore)


            if (this.gameStatus == "waiting") {

                if (this.gamePlayers.length = 2) {
                    if (this.yourShipsFromBackLength == 5 && this.oppShipsFromBackLength == 5) {
                        this.gameReadyStatus = "Launch Attack";
                        setTimeout(function () {
                            app.getGameViewData();
                        }, 3000);

                    } else if (this.yourShipsFromBackLength == 0 && this.oppShipsFromBackLength == 0) {
                        this.gameReadyStatus = "Deploy the fleet";
                        setTimeout(function () {
                            app.getGameViewData();
                        }, 60000);

                    } else {
                        this.gameReadyStatus = "Waiting for ready";
                        setTimeout(function () {
                            app.getGameViewData();
                        }, 30000);
                    }

                } else {
                    this.gameReadyStatus = "Waiting joiner";
                    setTimeout(function () {
                        app.getGameViewData();
                    }, 3000);
                }



            } else if (this.gameStatus == "yourTurn") {

                //window.location.reload(this.GameViewUrl);
                this.gameReadyStatus = this.currentTurn;
                setTimeout(function () {
                    app.salvosFire(this.currentGpId);

                }, 30000);

            } else {
                this.gameReadyStatus = "Game over";
                this.hitUpdateTabe();
            }

            console.log("in counter status is sssssssssssssssssssssssssssss", this.gameReadyStatus)

        },

        goToLeaderBoard: function () {
            window.location.assign("http://localhost:8080/web/games.html")
        },

        countdown: function () {

            if (this.gameStatus == "yourTurn") {

                var countDownDate = new Date().getTime() + 31000;
                var x = setInterval(function () {

                    var now = new Date().getTime();
                    var distance = countDownDate - now;
                    var seconds = Math.floor((distance % (1000 * 60)) / 1000);
                    document.getElementById("countTime").innerHTML = seconds + "s ";
                    if (distance < 0) {
                        clearInterval(x);
                        document.getElementById("countTime").innerHTML = "EXPIRED";
                    }
                }, 1000);


            } else {
                clearInterval(x);
            }
        }
    },

    computed: {

    },
    created: function () {
        this.getGameViewData();
        //this.countdown();
    }

});
