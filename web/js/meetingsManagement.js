(function () {

    // page components
    var meetingDetails;
    var pageOrchestrator;
    var myMeetings;
    var otherMeetings;



    // load event
    window.addEventListener("load", () => {
        pageOrchestrator = new PageOrchestrator();
        pageOrchestrator.start(); // initialize the components
        pageOrchestrator.refresh(); // display initial content
    }, false);


    // controller
    function PageOrchestrator() {

        var alertContainer = document.getElementById("id_alert");


        // FUNZIONI
        this.start = function() {

            let username = sessionStorage.getItem('username');
            let message = document.getElementById("id_username");
            let personalMessage = new PersonalMessage(username, message);

            personalMessage.show();


            myMeetingList = new MeetingsTable(
                alertContainer,
                document.getElementById("id_myMeetings"),
                document.getElementById("id_myMeetingsBody"));


            otherMeetingList = new MeetingsTable(
                alertContainer,
                document.getElementById("id_otherMeetings"),
                document.getElementById("id_otherMeetingsBody"));


            meetingList = new getAllMeetings(alertContainer);


            let detailParameters = {
                alert: alertContainer,
                detailcontainer: document.getElementById("id_meetingDetail"),
                title: document.getElementById("id_title"),
                date: document.getElementById("id_date"),
                hour: document.getElementById("id_hour"),
                duration: document.getElementById("id_duration"),
                partecipants: document.getElementById("id_partecipants")
            }


            meetingDetails = new MeetingDetails(detailParameters);

        };


        this.refresh = function(currentMeeting) {
            myMeetingList.reset();
            otherMeetingList.reset();
            meetingDetails.reset();

            meetingList.getMeetings();

            myMeetingList.show(myMeetings,function() {myMeetingList.autoclick(currentMeeting);});
            otherMeetings.show(otherMeetings);
        };
    }



    // constructors

    function getAllMeetings(_alert) {
        this.alert = _alert;

        this.getMeetings = function() {
            makeCall("GET", "GetMeetings", null,
                function (req) {
                    if (req.readyState === 4) {
                        var message = req.responseText;
                        if (req.status === 200) {

                            self.splitResponse(req.responseText);

                        } else {
                            self.alert.textContent = message;
                        }
                    }
                }
            );
        }

        this.splitResponse = function (res) {
            let allMeetings = res.split('#');

            myMeetings = JSON.parse(allMeetings[0]);
            otherMeetings = JSON.parse(allMeetings[1]);
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

        this.reset = function() {
            this.listcontainer.style.visibility = "hidden";
        }

        this.show = function(meetings, autoclick) {

            var self = this;

            self.update(meetings);

            if (autoclick) autoclick(); // show the first element of the list

        };

        this.update = function(arrayMeetings) {

            var l = arrayMeetings.length;
            var row, titleCell, dateCell, hourCell, linkcell, anchor;

            if (l === 0) {
                alert.textContent = "No meetings yet!";
            } else {
                this.listcontainerbody.innerHTML = ""; // empty the table body

                var self = this;

                arrayMeetings.forEach(function(meeting) {

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
                        //linkcell.setAttribute("class", "detailSelected");
                        meetingDetails.show(e.target.getAttribute("meetingId")); // the list must know the details container
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
            var selector = "a[meetingId='" + meetingId + "']";
            //var allanchors = this.listcontainerbody.querySelectorAll("a");
            //var myAnchor = document.querySelector(selector);
            var anchorToClick = (meetingId) ? document.querySelector(selector) : this.listcontainerbody.querySelectorAll("a")[0];
            anchorToClick.dispatchEvent(e);
        }

    }

    function MeetingDetails(options) {

        this.alert = options['alert'];
        this.detailcontainer = options['detailcontainer'];

        this.title = options['title'];
        this.date = options['date'];
        this.hour = options['hour'];
        this.duration = options['duration'];
        this.partecipants = options['partecipants'];



        this.show = function(missionid) {
            var self = this;

            /*if(typeof missionid === 'undefined')
                missionid = myMeetings[0].idMeeting;*/

            makeCall("GET", "GetMeetingDetails?meetingId=" + missionid, null,
                function(req) {
                    if (req.readyState === 4) {
                        var message = req.responseText;
                        if (req.status === 200) {
                            var meeting = JSON.parse(req.responseText);
                            self.update(meeting); // self is the object on which the function
                            // is applied
                            self.detailcontainer.style.visibility = "visible";

                        } else {
                            self.alert.textContent = message;

                        }
                    }
                }
            );
        };


        this.reset = function() {
            this.detailcontainer.style.visibility = "hidden";
        }

        this.update = function(m) {
            this.title.textContent = m.title;
            this.date.textContent = m.date;
            this.hour.textContent = m.hour;
            this.duration.textContent = m.duration;
            this.partecipants.textContent = m.maxParticipantsNumber;
        }
    }

})();