(function () {

    // page components
    var missionDetails, missionsList, createMeetingForm, pageOrchestrator;// = new PageOrchestrator(); // main controller

    window.addEventListener("load", () => {
        pageOrchestrator = new PageOrchestrator();
        pageOrchestrator.start(); // initialize the components
        pageOrchestrator.refresh(); // display initial content
    }, false);


    function PersonalMessage(_username, messagecontainer) {
        this.username = _username;
        this.show = function() {
            messagecontainer.textContent = this.username;
        }
    }

    function MeetingsList(_alert, _listcontainer, _listcontainerbody) {
        this.alert = _alert;
        this.listcontainer = _listcontainer;
        this.listcontainerbody = _listcontainerbody;

        this.reset = function() {
            this.listcontainer.style.visibility = "hidden";
        }

        this.show = function(next) {
            var self = this;
            makeCall("GET", "GetMeetings", null,
                function(req) {
                    if (req.readyState == 4) {
                        var message = req.responseText;
                        if (req.status == 200) {
                            self.splitResponse(req.responseText);
                            //self.update(JSON.parse(req.responseText)); // self visible by
                            // closure
                            if (next) next(); // show the first element of the list
                        } else {
                            self.alert.textContent = message;
                        }
                    }
                }
            );
        };


        this.splitResponse = function (res) {
            meetings = res.split('#');

            myMeetings = meetings[0];
            otherMeetings = meetings[1];

            self.update(JSON.parse(myMeetings));

        }


        this.update = function(arrayMeetings) {
            var l = arrayMeetings.length,
                elem, i, row, titleCell, dateCell, hourCell, linkcell, anchor;
            if (l == 0) {
                alert.textContent = "No meetings yet!";
            } else {
                this.listcontainerbody.innerHTML = ""; // empty the table body
                // build updated list
                var self = this;
                arrayMeetings.forEach(function(meeting) { // self visible here, not this
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
                    //anchor.missionid = meeting.id; // make list item clickable
                    anchor.setAttribute('meetingid', meeting.id); // set a custom HTML attribute
                    anchor.addEventListener("click", (e) => {
                        // dependency via module parameter
                        missionDetails.show(e.target.getAttribute("meetingid")); // the list must know the details container
                    }, false);
                    anchor.href = "#";
                    row.appendChild(linkcell);

                    self.listcontainerbody.appendChild(row);
                });
                this.listcontainer.style.visibility = "visible";
            }
        }

        this.autoclick = function(meetingId) {
            var e = new Event("click");
            var selector = "a[meetingid='" + meetingId + "']";
            var allanchors = this.listcontainerbody.querySelectorAll("a");
            var myAnchor = document.querySelector(selector);
            var anchorToClick =
                (meetingId) ? document.querySelector(selector) : this.listcontainerbody.querySelectorAll("a")[0];
            anchorToClick.dispatchEvent(e);
        }

    }





    function PageOrchestrator() {
        var alertContainer = document.getElementById("id_alert");


        // FUNZIONI
        this.start = function() {

            let username = sessionStorage.getItem('username');
            let message = document.getElementById("id_username");
            let personalMessage = new PersonalMessage(username, message);

            personalMessage.show();


            meetingList = new MeetingsList(
                alertContainer,
                document.getElementById("id_myMeetings"),
                document.getElementById("id_myMeetingsBody"));




            wizard = new Wizard({ // many parameters, wrap them in an
                // object
                alert: alertContainer,
                detailcontainer: document.getElementById("id_detailcontainer"),
                expensecontainer: document.getElementById("id_expensecontainer"),
                expenseform: document.getElementById("id_expenseform"),
                closeform: document.getElementById("id_closeform"),
                date: document.getElementById("id_date"),
                destination: document.getElementById("id_destination"),
                status: document.getElementById("id_status"),
                description: document.getElementById("id_description"),
                country: document.getElementById("id_country"),
                province: document.getElementById("id_province"),
                city: document.getElementById("id_city"),
                fund: document.getElementById("id_fund"),
                food: document.getElementById("id_food"),
                accomodation: document.getElementById("id_accomodation"),
                transportation: document.getElementById("id_transportation")
            });
            missionDetails.registerEvents(this);

            //wizard = new Wizard(document.getElementById("id_createmissionform"), alertContainer);
            //wizard.registerEvents(this);
        };


        this.refresh = function(currentMeeting) {
            meetingList.reset();
            //missionDetails.reset();
            meetingList.show(function() {
                meetingList.autoclick(currentMeeting);
            }); // closure preserves visibility of this
            //wizard.reset();
        };
    }

    function CreateMeetingForm(_form) {
        this.form = _form;

        // set the range of the inputs.
        let todayDate = new Date().toISOString().substring(0,10);
        this.form.querySelector('input[type="date"]').setAttribute("min", todayDate);
        this.form.querySelectorAll('input[type="number"]').forEach(element => {
            element.setAttribute("min", 0); //todo da testare
        })

        // adding listeners
        let button = this.form.querySelector('input[type="button"]');
        button.addEventListener("submit", (event => {
            event.preventDefault();

            if (this.form.checkValidity()){
                this.form.hidden = true;

                //TODO crea la nuova pagina con tutti gli utenti
            }
            else
                this.form.reportValidity();
        }));

        this.showParticipants = function () {
            makeCall("GET", "GetParticipants", null,
                function (request) {
                    if (request.readyState === XMLHttpRequest.DONE){

                    }
                }, false);

            let chooseMeetingParticipants = new ChooseMeetingParticipants(_participants);
        }
    }

    function ChooseMeetingParticipants(_participants) {

    }

})();