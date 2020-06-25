(function () {

    // page components
    var pageOrchestrator;

    var meetingDetails;
    var myMeetingTable;
    var otherMeetingTable;

    var createMeetingForm;
    var chooseMeetingParticipants;
    var newMeetingParameters;


    // Arrays
    var myMeetings;
    var otherMeetings;

    // helper
    var attempts = 0;
    var selectedCell;


    // load event
    window.addEventListener("load", () => {
        pageOrchestrator = new PageOrchestrator();
        pageOrchestrator.start(); // initialize the components
        pageOrchestrator.refresh(); // display initial content
    }, false);


    // controller
    function PageOrchestrator() {

        let mainAlert = document.getElementById("id_mainAlert");
        let alertContainerMyMeetings = document.getElementById("id_alert_myMeetings");
        let alertContainerOtherMeetings = document.getElementById("id_alert_otherMeetings");


        // FUNZIONI
        this.start = function () {

            let username = sessionStorage.getItem('username');
            let message = document.getElementById("id_username");

            let personalMessage = new PersonalMessage(username, message);

            personalMessage.show();


            myMeetingTable = new MeetingsTable(
                alertContainerMyMeetings,
                document.getElementById("id_myMeetings"),
                document.getElementById("id_myMeetingsBody"));


            otherMeetingTable = new MeetingsTable(
                alertContainerOtherMeetings,
                document.getElementById("id_otherMeetings"),
                document.getElementById("id_otherMeetingsBody"));


            meetingList = new GetAllMeetings(mainAlert);


            let detailParameters = {
                detailcontainer: document.getElementById("id_meetingDetail"),
                title: document.getElementById("id_title"),
                date: document.getElementById("id_date"),
                hour: document.getElementById("id_hour"),
                duration: document.getElementById("id_duration"),
                participants: document.getElementById("id_participants")
            }


            meetingDetails = new MeetingDetails(detailParameters);

            createMeetingForm = new CreateMeetingForm(document.querySelector("#id_createMeetingForm form"));
            document.getElementById("id_chooseParticipants").hidden = true;
            document.getElementById("id_threeAttemptsDone").hidden = true;
        };


        this.refresh = function(currentMeeting) {
            myMeetingTable.reset();
            otherMeetingTable.reset();
            meetingDetails.reset();

            meetingList.getMeetings(currentMeeting);
        };
    }


    // constructors

    function GetAllMeetings(_alert) {
        this.alert = _alert;

        this.getMeetings = function(currentMeeting) {

            var self = this;

            makeCall("GET", "GetMeetings", null,
                function (req) {
                    if (req.readyState === 4) {
                        var message = req.responseText;
                        if (req.status === 200) {

                            self.splitResponse(req.responseText, currentMeeting);

                        } else {
                            self.alert.textContent = message;
                        }
                    }
                }
            );
        }

        this.splitResponse = function (res, currentMeeting) {
            let allMeetings = res.split('#');

            if(allMeetings[0] === "[]" && allMeetings[1] === "[]")
                meetingDetails.reset();   //nasconde la meeting details

            myMeetings = JSON.parse(allMeetings[0]);
            otherMeetings = JSON.parse(allMeetings[1]);

            this.show(currentMeeting);
        }

        this.show = function (currentMeeting) {

            myMeetingTable.show(myMeetings,function() {myMeetingTable.autoclick(currentMeeting);});

            if(myMeetings.length === 0)
                otherMeetingTable.show(otherMeetings,function() {otherMeetingTable.autoclick(currentMeeting);});

            else otherMeetingTable.show(otherMeetings);
        }
    }

    function PersonalMessage(_username, messagecontainer) {
        this.username = _username;
        this.show = function() {
            messagecontainer.textContent = this.username;
        }
    }

    function MeetingsTable(_alert, _listcontainer, _listcontainerbody) {
        this.alert = _alert;
        this.listcontainer = _listcontainer;
        this.listcontainerbody = _listcontainerbody;

        this.reset = function () {
            this.listcontainer.style.visibility = "hidden";
        }

        this.show = function (meetings, autoclick) {

            var self = this;

            if (meetings.length !== 0){
                self.update(meetings);
                if (autoclick) autoclick(); // show the first element of the list
            }
            else this.alert.textContent = "No meetings yet!";
        };

        this.update = function (arrayMeetings) {

            var l = arrayMeetings.length;
            var row, titleCell, dateCell, hourCell, linkcell, anchor;

            if (l === 0) {
                alert.textContent = "No meetings yet!";
            } else {
                this.listcontainerbody.innerHTML = ""; // empty the table body

                this.alert.textContent = "";

                var self = this;

                arrayMeetings.forEach(function (meeting) {

                    row = document.createElement("tr");

                    titleCell = document.createElement("td");
                    titleCell.textContent = meeting.title;
                    row.appendChild(titleCell);

                    dateCell = document.createElement("td");
                    dateCell.textContent = meeting.date;
                    row.appendChild(dateCell);

                    hourCell = document.createElement("td");
                    hourCell.textContent = meeting.hour;
                    row.appendChild(hourCell);

                    linkcell = document.createElement("td");
                    anchor = document.createElement("a");
                    linkcell.appendChild(anchor);
                    linkText = document.createTextNode("Show");
                    anchor.appendChild(linkText);

                    anchor.setAttribute('meetingId', meeting.idMeeting);

                    anchor.addEventListener("click", (e) => {

                        if(selectedCell !== undefined) selectedCell.className = "";

                        selectedCell = e.target.closest("td");
                        selectedCell.className = "detailSelected";
                        meetingDetails.show(e.target.getAttribute("meetingId")); // the list must know the details container
                    }, false);

                    anchor.href = "#";
                    row.appendChild(linkcell);

                    self.listcontainerbody.appendChild(row);
                });
                this.listcontainer.style.visibility = "visible";
            }
        }

        this.autoclick = function (meetingId) {
            var e = new Event("click");
            var selector = "a[meetingId='" + meetingId + "']";
            //var allanchors = this.listcontainerbody.querySelectorAll("a");
            //var myAnchor = document.querySelector(selector);
            var anchorToClick = (meetingId) ? document.querySelector(selector) : this.listcontainerbody.querySelectorAll("a")[0];
            anchorToClick.dispatchEvent(e);
        }

    }

    function MeetingDetails(options) {

        this.detailcontainer = options['detailcontainer'];

        this.title = options['title'];
        this.date = options['date'];
        this.hour = options['hour'];
        this.duration = options['duration'];
        this.participants = options['participants'];

        this.detailDiv = document.getElementById("id_meetingDetailDiv");


        this.show = function (missionid) {
            var self = this;

            /*if(typeof missionid === 'undefined')
                missionid = myMeetings[0].idMeeting;*/

            makeCall("GET", "GetMeetingDetails?meetingId=" + missionid, null,
                function (req) {
                    if (req.readyState === 4) {
                        var message = req.responseText;
                        if (req.status === 200) {
                            var meeting = JSON.parse(req.responseText);
                            self.update(meeting); // self is the object on which the function
                            // is applied
                            self.detailDiv.style.visibility = "visible";

                        } else {
                            self.reset();

                        }
                    }
                }
            );
        };


        this.reset = function () {
            this.detailDiv.style.visibility = "hidden";
        }

        this.update = function (m) {
            this.title.textContent = m.title; this.title.className = "detailSelected";
            this.date.textContent = m.date;
            this.hour.textContent = m.hour;
            this.duration.textContent = m.duration;
            this.participants.textContent = m.maxParticipantsNumber;
        }
    }


    function CreateMeetingForm(_form) {
        this.form = _form;

        // set the range of the inputs.
        let todayDate = new Date().toISOString().substring(0,10);
        this.form.querySelector('input[type="date"]').setAttribute("min", todayDate);
        this.form.querySelectorAll('input[type="number"]').forEach(element => {
            element.setAttribute("min", 0);
        })

        // adding listeners
        var button = this.form.querySelector('input[type="button"]');
        button.addEventListener("click", (event => this.inviaHandler(event)));

        // defining handlers
        this.inviaHandler = function (event) {
            event.preventDefault();

            if (this.form.checkValidity()){
                this.form.closest("div").hidden = true;

                // Create an object to store meetings parameters
                newMeetingParameters = new NewMeetingParameters(this.form, sessionStorage.getItem("username"));

                // SHOW PARTICIPANTS
                let self = this;
                makeCall("GET", "GetParticipants", null,
                    function (request) {
                        if (request.readyState === XMLHttpRequest.DONE){
                            switch (request.status) {
                                case 200:
                                    let usernamesParticipants = JSON.parse(request.responseText);
                                    self.form.querySelector('p.errorMessage').textContent = "";
                                    chooseMeetingParticipants = new ChooseMeetingParticipants(usernamesParticipants);
                                    chooseMeetingParticipants.showParticipants();
                                    break;
                                case 500:   //TODO da testare
                                    let errorMessage = request.responseText;
                                    self.form.hidden = false;
                                    self.form.reset();
                                    self.form.querySelector('p.errorMessage').textContent = errorMessage;
                                    break;
                            }
                        }
                    }, false);
            }
            else
                this.form.reportValidity();
        }

        this.reset = function () {
            document.getElementById("id_chooseParticipants").hidden = true;
            document.getElementById("id_threeAttemptsDone").hidden = true;
            this.form.reset();
            document.getElementById("id_createMeetingForm").hidden = false;
        }
    }

    function ChooseMeetingParticipants(_usernamesParticipants) {
        this.tableDiv = document.getElementById("id_chooseParticipants");
        this.usernamesParticipants = _usernamesParticipants;
        let self = this;

        //handlers
        this.selectHandler = function (event) {
            let td = event.target.closest("td");

            if (td.className === "userChosen")
                td.className = "";
            else
                td.className = "userChosen";
        }

        this.backButtonHandler = function () {
            document.getElementById("id_chooseParticipants").hidden = true;
            document.getElementById("id_createMeetingForm").hidden = false;
        }

        this.invitaButtonHandler = function (event) {
            attempts+=1;

            makeCall("GET", "IncrementAttempts", null,
                function (request) {
                    if (request.readyState === XMLHttpRequest.OPENED){
                        request.setRequestHeader("title", newMeetingParameters.title);
                    }
                    if (request.readyState === XMLHttpRequest.DONE){
                        if (request.status!==200)
                            document.querySelector("#id_chooseParticipants errorMessage")
                                .textContent = "There has been troubles with the server connection while incrementing attempts.";
                    }
                });

            let tbody = event.target.closest("tbody");

            Array.from(tbody.querySelectorAll("td.userChosen")).forEach(function (td) {
                let anchor = td.querySelector("a");
                let username = anchor.textContent;

                newMeetingParameters.participants.push(username);
            });

            if (newMeetingParameters.maxParticipantsNumber >= newMeetingParameters.participants.length){
                let json_newMeetingParameters = JSON.stringify(newMeetingParameters);

                let self = this;
                makeCall("GET", "CreateMeeting", null,
                    function (request) {
                        if (request.readyState === XMLHttpRequest.OPENED){
                            request.setRequestHeader("newMeetingParameters", json_newMeetingParameters);
                        }
                        if (request.readyState === XMLHttpRequest.DONE){
                            let message = request.responseText;
                            switch (request.status) {
                                case 200:
                                    createMeetingForm.reset();
                                    pageOrchestrator.refresh();
                                    break;
                                default:
                                    if (message.toString().localeCompare(("attempts").toString())===0){ //sono uguali i due messaggi
                                        let cancellazione = new Cancellazione();
                                        cancellazione.show();
                                    }
                                    document.querySelector("#id_chooseParticipants .errorMessage")
                                        .textContent = message;
                            }

                            self.tableDiv.querySelector(".errorMessage").textContent = "";
                            attempts = 0;
                        }
                    });
            }
            else {
                newMeetingParameters.participants = [];
                this.tableDiv.querySelector(".errorMessage").textContent =
                    "Troppi utenti selezionati, eliminane almeno "
                    + (newMeetingParameters.maxParticipantsNumber - newMeetingParameters.participants.length);

                if (attempts===3) {
                    this.tableDiv.querySelector(".errorMessage").textContent = "";
                    let cancellazione = new Cancellazione();
                    cancellazione.show();
                    attempts = 0;
                }
            }
        }

        //SHOW PARTICIPANTS
        this.showParticipants = function() {
            let length = self.usernamesParticipants.length;
            let tbody = document.querySelector("#id_chooseParticipants tbody");

            if (length <= 1){
                tbody.innerHTML = "";
                self.tableDiv.querySelector("h5.errorMessage").textContent = "There aren't users yet.";

                let tr = document.createElement("tr");
                let td = document.createElement("td");
                let anchor = document.createElement("a");

                anchor.textContent = "BACK";
                anchor.href = "#";

                td.appendChild(anchor);
                tr.appendChild(td);
                tbody.appendChild(tr);

                anchor.addEventListener("click", (() => this.backButtonHandler()));

                this.tableDiv.hidden = false;
            }
            else {
                let tr, td, anchor;

                tbody.innerHTML = "";

                Array.from(self.usernamesParticipants).forEach(function(usernameParticipant) { // self visible here, not this
                    // The user of the session is not displayed

                    if (usernameParticipant.toString().localeCompare(sessionStorage.getItem("username").toString())!==0) {
                        tr = document.createElement("tr");
                        td = document.createElement("td");

                        anchor = document.createElement("a");
                        anchor.textContent = usernameParticipant;
                        anchor.href = "#";

                        td.appendChild(anchor);
                        tr.appendChild(td);
                        tbody.appendChild(tr);

                        anchor.addEventListener("click", (event => self.selectHandler(event)));
                    }
                });

                tr = document.createElement("tr");

                td = document.createElement("td");
                anchor = document.createElement("a");
                anchor.textContent = "BACK";
                anchor.href = "#";
                anchor.addEventListener("click", (() => this.backButtonHandler()));

                td.appendChild(anchor);
                tr.appendChild(td);

                td = document.createElement("td");
                anchor = document.createElement("a");
                anchor.textContent = "INVITA";
                anchor.href = "#";
                anchor.addEventListener("click", (event => this.invitaButtonHandler(event)));

                td.appendChild(anchor);
                tr.appendChild(td);

                tbody.appendChild(tr);
            }

            this.tableDiv.hidden = false;
        }
    }

    function Cancellazione() {
        //handler
        this.backButtonHandler = function () {
            document.getElementById("id_threeAttemptsDone").hidden = true;
            pageOrchestrator.refresh();
        }

        this.show = function () {
            document.getElementById("id_createMeetingForm").hidden = true;
            document.getElementById("id_chooseParticipants").hidden = true;
            document.getElementById("id_threeAttemptsDone").hidden = false;

            let anchor = document.getElementById("id_back");
            anchor.addEventListener("click", () => this.backButtonHandler());
        }
    }
    
    function NewMeetingParameters(_form, _usernameCreator) {
        this.title = _form.querySelector('input[name="title"]').value;
        this.date = _form.querySelector('input[name="date"]').value;
        this.hour = _form.querySelector('input[name="hour"]').value;
        this.duration = _form.querySelector('input[name="duration"]').value;
        this.maxParticipantsNumber = _form.querySelector('input[name="maxParticipantsNumber"]').value;
        this.usernameCreator = _usernameCreator;

        this.participants = [];
    }

})();