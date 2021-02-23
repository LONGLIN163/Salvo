/* eslint-env browser */
/* eslint "no-console": "off"  */
/* global$ */



//var GameViewUrl="http://localhost:8080/api/game_view/"+document.querySelector('[data-page]').getAttribute("data-page");

//console.log(GameViewUrl);




var app = new Vue({
    el: "#vue-gameView-app",
    data: {
        sound: null,
        GameViewDataUrl: "",
        realGameViewUrl: "",
        opponentGameViewDataUrl: null,
        tableColums: ["", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
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
        hitsOnOppent: {},
        hitsOnOppentClone: {},
        hitsOnOppentUpdateData: [],
        lastTurnHitOnOpp:{},
        lastTurnObj: {},
        lastTurn: "",
        hitsOnOppent: {},
        gameStatus: "",
        youScore: 0,
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

            this.getOpponentGameViewDataUrl();

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
                    app.gamePlayers = app.gameView.gamePlayers;
                    app.mySHips = app.gameView.ships;
                    app.gameStatus = app.gameView.gameStatus;
                    console.log("gameStatus-----------------", app.gameStatus)
                    app.youScore = app.gameView.score;

                    app.hitsOnYou = app.gameView.hitsOnYou;
                    app.hitsOnOppent = app.gameView.hitsOnOppent;

                    app.hitsOnYouClone = Object.assign({}, app.hitsOnYou);
                    app.hitsOnOppentClone = Object.assign({}, app.hitsOnOppent);

                    console.log(app.hitsOnYou);
                    console.log(app.hitsOnOppent);
                    console.log("colone1-------", app.hitsOnYouClone);
                    console.log("colone2-------", app.hitsOnOppentClone);



                    app.hitOnYouUpdateData = app.hitUpdate(app.hitsOnYouClone);
                    console.log("hit on you -----------------", app.hitOnYouUpdateData)
                    app.lastTurnHitOnYou = app.hitOnYouUpdateData[app.hitOnYouUpdateData.length - 1]
                    console.log("hit on you last -----------------", app.lastTurnHitOnYou)


                    app.hitsOnOppentUpdateData = app.hitUpdate(app.hitsOnOppentClone);
                    console.log("hit on opp-----------------", app.hitsOnOppentUpdateData)
                    app.lastTurnHitOnYou = app.hitOnYouUpdateData[app.hitOnYouUpdateData.length - 1]



                    console.log("ididididdidiididididi", app.currentGpId)




                    app.hitUpdateTabe();

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
                    //app.gameViewUpDate();


                })
                .catch(function (error) {
                    console.log("Request failed: " + error.message);
                });
        },

        getOpponentGameViewDataUrl: function () {

            for (var x = 0; x < this.gamePlayers.length; x++) {
                if (this.gamePlayers[x].id != this.currentGpId) {
                    var d = this.gamePlayers[x].id;
                    this.opponentGameViewDataUrl = "http://localhost:8080/api/game_view/" + d;
                }
            }
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

                        app.knowShipsReady();

                        if (app.yourShipsReady == true) {
                            setTimeout(function () {
                                window.location.reload(this.GameViewUrl);
                                this.createShipBottonStatus = "inactive";
                            }, 1500);
                        } else {
                            window.location.reload(this.GameViewUrl);
                        }


                        //                        window.location.reload(this.GameViewUrl);
                        //                        this.createShipBottonStatus = "inactive";


                    }
                })
                .catch(function (error) {
                    console.log('Request failure: ', error);
                });

        },

        knowShipsReady: function () {
            console.log("222222222222222222222----" + this.yourShips)
            if (this.yourShips.length == 5) {
                this.yourShipsReady = true;

            } else {
                this.yourShipsReady = false;
            }

            console.log("11111111111111111111111111111----" + this.yourShipsReady)

        },

        aiming: function (item) {
            this.aim = item;
        },

        salvosFire: function (gpId) {

            gpId = this.currentGpId;
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
                    } else if (data.success) {
                        console.log("Request success:", data.success)
                        console.log("33333333333333333333----" + app.yourSalvos)

                        app.knowSalvoReady();

                        console.log("666666666666666666666----" + app.yourSalvosReady)

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
            var overId = Array.from(id);

            var shipLocationTemp = [];

            for (var i = 0; i < this.ship.dimension; i++) {

                if (this.shipDirection == "horizontal") {
                    var idPlus = overId[0] + (parseInt(overId[1]) + i);
                    console.log("idPlus-click-A1:" + idPlus);
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

            console.log(arrTemp)

            return arrTemp;
        },

        hitUpdateTabe: function () {

            var lastTurnHitOnYou = this.hitOnYouUpdateData[this.hitOnYouUpdateData.length - 1]
            console.log("lassssssssssssssssssssssssst", lastTurnHitOnYou)
            for (var key in lastTurnHitOnYou) {
                if (lastTurnHitOnYou[key].hitsPosition != null) {
//                    var hitStatus = document.createElement("div");
//                    hitStatus.setAttribute("class", "hitStatusTest1");
                    //document.getElementById(el).appendChild(hitStatus);
                    lastTurnHitOnYou[key].hitsPosition.forEach(el => {

                        console.log("elllllllllllllllllllll---", el)
                        //                        var hitStatus = document.createElement("div");
                        //                        hitStatus.setAttribute("class", "hitStatusTest1");
                        //document.getElementById(el).appendChild(hitStatus);
                        document.getElementById(el).style.background = "/imgs/giphy.gif";
                        // document.getElementById(el).setAttribute("class", "hitStatusTest1");
                        //document.getElementById(el).classList.add("hitStatusTest1");


                    });
                }

            }



            var lastTurnHitOnOpp = this.hitsOnOppentUpdateData[this.hitsOnOppentUpdateData.length - 1]
            console.log("llllllllllllllllllllllllast", lastTurnHitOnOpp)
            for (var key in lastTurnHitOnOpp) {
                if (lastTurnHitOnOpp[key].hitsPosition != null) {
                    //var hitStatus2 = document.createElement("div");
                   // hitStatus2.setAttribute("class", "hitStatusTest1");

                    lastTurnHitOnOpp[key].hitsPosition.forEach(el => {
                        el = "PL" + el;
                        console.log("eeeeeeeeeeeeeeeeeeeeeeeel---", el)

                        //var hitStatus2 = document.createElement("div");
                        //                        hitStatus2.setAttribute("class", "hitStatusTest1");
                        //                        document.getElementById(el).appendChild(hitStatus2);
                        //document.getElementById(el).style.backgroundColor = "pink";
                        //document.getElementById(el).classList.add("hitStatusTest1");
                        document.getElementById(el).style.background = "/imgs/giphy.gif";
                    });
                }

            }
            
            //window.location.reload(this.GameViewUrl);
            
            
        },

        gameViewUpDate: function () {

            console.log("in counter gamestatus is vvvvvvvvvvvvvvvvvvvvvvv", this.gameStatus)
            console.log("in counter score is sssssssssssssssssssssssssssss", this.youScore)


            if (this.gamePlayers.length == 2) {

                if (this.gameStatus == "waiting") {
                    setTimeout(function () {
                        app.getGameViewData();
                    }, 2000);
                } else if (this.gameStatus == "yourTurn") {
                    setTimeout(function () {
                        app.salvosFire(this.currentGpId);

                    }, 15000);

                } else {

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
