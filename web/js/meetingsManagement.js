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
    var myMeetings;     // array che contiene i meetings che ho creato
    var otherMeetings;  // array che contiene i meetings a cui partecipo

    // helper
    var attempts = 0;   // nr di tentativo di invito dei partecipanti
    var selectedCell;   // è la cella gialla che corrisponde al meetingDetails


    // load event
    window.addEventListener("load", () => {
        pageOrchestrator = new PageOrchestrator();
        pageOrchestrator.start(); // inizializza i componenti
        pageOrchestrator.refresh(); // mostra i componenti
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
        };


        this.refresh = function(currentMeeting) {
            myMeetingTable.reset();
            otherMeetingTable.reset();
            meetingDetails.reset();
            createMeetingForm.init();

            meetingList.getMeetings(currentMeeting);
        };
    }


    // constructors

    function GetAllMeetings(_alert) {
        this.alert = _alert;

        // richiede al server tutti i meeting, sia quelli che ho creato che quelli a cui partecipo
        this.getMeetings = function(currentMeeting) {

            var self = this;

            makeCall("GET", "GetMeetings", null,
                function (req) {
                    if (req.readyState === XMLHttpRequest.DONE) {

                        let message = req.responseText;

                        if (req.status === 200) {

                            // message contiene: json_myMeetings + "#" + json_othersMeetings
                            self.splitResponse(message, currentMeeting);

                        } else {
                            self.alert.textContent = message;
                        }
                    }
                }
            );
        }

        this.splitResponse = function (res, currentMeeting) {
            let allMeetings = res.split('#');

            // se json_myMeetings e json_othersMeetings sono vuote..
            if(allMeetings[0] === "[]" && allMeetings[1] === "[]")
                meetingDetails.reset();   //nasconde la meeting details

            myMeetings = JSON.parse(allMeetings[0]);
            otherMeetings = JSON.parse(allMeetings[1]);

            this.show(currentMeeting);
        }

        // chiama la show di myMeetingTable e di otherMeetingTable
        this.show = function (currentMeeting) {

            // questi if else servono solo per fare l'autoclick nella tabella otherMeetingTable
            // nel caso in cui la tabella myMeetingTable sia vuota
            // (PS: anche se chiamo la show con l'autoclick, se è vuota non lo esegue)

            myMeetingTable.show(myMeetings,function() {myMeetingTable.autoclick(currentMeeting);});

            // se non ho meeting fatti da me...
            if(myMeetings.length === 0)
                otherMeetingTable.show(otherMeetings,function() {otherMeetingTable.autoclick(currentMeeting);});

            else otherMeetingTable.show(otherMeetings);
        }
    }

    function PersonalMessage(_username, messagecontainer) {
        this.username = _username;
        this.show = function() {
            // stampa all'inizio della home il nome dell'utente
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

        // chiama la update() se meeting non è vuota, altrimenti stampa l'alert
        this.show = function (meetings, autoclick) {

            var self = this;

            // se meeting non è vuota...
            if (meetings.length !== 0){

                self.update(meetings);
                if (autoclick) autoclick(); // mostra il primo meeting della tabella

            }
            else this.alert.textContent = "No meetings yet!";
        };

        // compila la tabella con i meetings che il server gli fornisce
        this.update = function (arrayMeetings) {

            var l = arrayMeetings.length;
            let row, titleCell, dateCell, hourCell, linkcell, anchor;

            if (l === 0) {  // controllo inutile ma per sicurezza XD
                alert.textContent = "No meetings yet!";

            } else {
                this.listcontainerbody.innerHTML = ""; // svuota il body della tabella
                this.alert.textContent = "";

                var self = this;

                arrayMeetings.forEach(function (meeting) {

                    row = document.createElement("tr");

                    // prima cella della riga (titolo)
                    titleCell = document.createElement("td");
                    titleCell.textContent = meeting.title;
                    row.appendChild(titleCell);

                    // seconda cella della riga (data)
                    dateCell = document.createElement("td");
                    dateCell.textContent = meeting.date;
                    row.appendChild(dateCell);

                    // terza cella della riga (ora)
                    hourCell = document.createElement("td");
                    hourCell.textContent = meeting.hour;
                    row.appendChild(hourCell);

                    // quarta cella della riga (ancora x dettagli)
                    linkcell = document.createElement("td");

                    anchor = document.createElement("a");
                    let linkText = document.createTextNode("Show");
                    anchor.appendChild(linkText);
                    anchor.setAttribute('meetingId', meeting.idMeeting);

                    anchor.addEventListener("click", (e) => {

                        e.preventDefault();
					
                        if(selectedCell !== undefined) selectedCell.className = ""; // entra solo la prima volta

                        selectedCell = e.target.closest("td");
                        selectedCell.className = "detailSelected";  // colora di giallo la cella

                        meetingDetails.show(e.target.getAttribute("meetingId")); // the list must know the details container
                    }, false);

                    anchor.href = "#";

                    linkcell.appendChild(anchor);

                    row.appendChild(linkcell);

                    self.listcontainerbody.appendChild(row);
                });
                this.listcontainer.style.visibility = "visible";
            }
        }

        this.autoclick = function (meetingId) {

            var e = new Event("click"); // crea l'evento
            var selector = "a[meetingId='" + meetingId + "']";

            var anchorToClick = (meetingId) // se meetingId != undefined..
                ? document.querySelector(selector)  // ..allora prendi l'ancora relariva al meetingId
                : this.listcontainerbody.querySelectorAll("a")[0];  // ..altrimenti prendi la prima ancora della tabella

            anchorToClick.dispatchEvent(e); // avvia l'evento
        }

    }

    function MeetingDetails(options) {

        // option è un array che contiene gli id ( detailParameters nella PageOrchestrator.start() )

        this.detailcontainer = options['detailcontainer'];

        this.title = options['title'];
        this.date = options['date'];
        this.hour = options['hour'];
        this.duration = options['duration'];
        this.participants = options['participants'];

        this.detailDiv = document.getElementById("id_meetingDetailDiv");

        // richiede al server i dettagli del meeting selezionato
        this.show = function (missionid) {

            var self = this;

            makeCall("GET", "GetMeetingDetails?meetingId=" + missionid, null,
                function (req) {
                    if (req.readyState === XMLHttpRequest.DONE) {
                        let message = req.responseText;

                        if (req.status === 200) {

                            let meeting = JSON.parse(message);
                            self.update(meeting);
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

        // compila la tabella con i dettagli del meeting che il server gli fornisce
        this.update = function (m) {
                                                // colora di giallo il titolo della riunione
            this.title.textContent = m.title;   this.title.className = "detailSelected";
            this.date.textContent = m.date;
            this.hour.textContent = m.hour;
            this.duration.textContent = m.duration;
            this.participants.textContent = m.maxParticipantsNumber;

        }
    }


    //FORM FOR CREATING A NEW MEETING.
    function CreateMeetingForm(_form) {
        this.form = _form;

        // defining handlers
        // invia passa al modulo di scelta dei partecipanti
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
                                    chooseMeetingParticipants = new ChooseMeetingParticipants(usernamesParticipants);
                                    chooseMeetingParticipants.showParticipants();
                                    break;
                                case 500:
                                    let errorMessage = request.responseText;
                                    self.reset();
                                    self.form.querySelector('p.errorMessage').textContent = errorMessage;
                                    break;
                            }
                        }
                    }, false);
            }
            else
                this.form.reportValidity();
        }

        // defining functions
        this.init = function () {
            // set the range of the inputs.
            let todayDate = new Date().toISOString().substring(0,10);
            this.form.querySelector('input[type="date"]').setAttribute("min", todayDate);
            this.form.querySelectorAll('input[type="number"]').forEach(element => {
                element.setAttribute("min", 0);
            })
            // set the required inputs
            this.form.querySelectorAll("input").forEach(function (input) {
                input.required = true;
            })

            // adding listeners
            let button = this.form.querySelector('input[type="button"]');
            button.addEventListener("click", (event => this.inviaHandler(event)));

            this.reset(); 
        }

        // reset the module of the creating new form
        this.reset = function () {
            document.getElementById("id_chooseParticipants").hidden = true;
            document.getElementById("id_threeAttemptsDone").hidden = true;

            this.form.reset();
            // delete every error message
            Array.from(document.querySelectorAll("#id_divWizard .errorMessage")).forEach(
                function (errorContenitor) {
                    errorContenitor.textContent = "";
                }
            )
            document.getElementById("id_createMeetingForm").hidden = false;
        }
    }

    // Oggetto che rappresenta la scelta dei partecipanti
    function ChooseMeetingParticipants(_usernamesParticipants) {

        this.tableDiv = document.getElementById("id_chooseParticipants");
        this.usernamesParticipants = _usernamesParticipants;

        let self = this;

        //HANDLERS
        // selezionando un utente viene modificata la sua classe in "userChose"

        // fa switchare l'utente da selezionato a non
        this.selectHandler = function (event) {

            event.preventDefault();
            let td = event.target.closest("td");

            if (td.className === "userChosen")
                td.className = "";
            else
                td.className = "userChosen";
        }
        // torna alla form principale, mantenendo gli stessi dati che c'erano prima
        this.backButtonHandler = function (event) {
            event.preventDefault();

            document.getElementById("id_chooseParticipants").hidden = true;
            let form = document.getElementById("id_createMeetingForm");

            form.hidden = false;    // faccio ricomparire la form
            form.querySelector('p.errorMessage').textContent = "";  // resetto il messaggio di errore
        }
        // vengono invitati gli utenti precedentemente selezionati
        this.invitaButtonHandler = function (event) {
            event.preventDefault();
            // i tentativi vengono incrementati ad ogni "invita"
            attempts+=1;

            makeCall("GET", "IncrementAttempts", null,
                function (request) {
                    let json_newMeetingParameters = JSON.stringify(newMeetingParameters);

                    if (request.readyState === XMLHttpRequest.OPENED){
                        // gli passo i parametri del meeting perché il server deve verificare che non siano corrotti
                        request.setRequestHeader("newMeetingParameters", json_newMeetingParameters);
                    }
                    if (request.readyState === XMLHttpRequest.DONE){
                        if (request.status!==200) {
                            document.querySelector("#id_chooseParticipants errorMessage")
                                .textContent = "There has been troubles with the server connection while incrementing attempts.";

                            // qualcosa è andato storto -> tentativi posti a 0
                            attempts = 0;
                        }
                    }
                });

            let tbody = document.querySelector("#id_chooseParticipants tbody");
            // vengono aggiunti all'oggetto newMeetingForm gli usernames scelti
            Array.from(tbody.querySelectorAll("td.userChosen")).forEach(function (td) {
                let username = td.querySelector("a").textContent;

                newMeetingParameters.participants.push(username);
            });
            // se il numero di partecipanti è legale, allora viene mandata la GET
            if (newMeetingParameters.maxParticipantsNumber >= newMeetingParameters.participants.length){
                let json_newMeetingParameters = JSON.stringify(newMeetingParameters);

                makeCall("GET", "CreateMeeting", null,
                    function (request) {
                        if (request.readyState === XMLHttpRequest.OPENED){
                            request.setRequestHeader("newMeetingParameters", json_newMeetingParameters);
                        }
                        if (request.readyState === XMLHttpRequest.DONE){
                            let message = request.responseText;
                            switch (request.status) {
                                case 200:
                                    // se tutto va bene, viene ricaricata la pagina
                                    pageOrchestrator.refresh();
                                    break;
                                default:
                                    // se il motivo della bad request sono i tentativi, allora viene inviato un messaggio di allerta
                                    // e viene mostrata la "pagina" di cancellazione
                                    if (message.toString().localeCompare(("attempts").toString())===0){ //sono uguali i due messaggi
                                        let cancellazione = new Cancellazione();
                                        document.getElementById("id_chooseParticipants").hidden = true;
                                        cancellazione.show();

                                        document.querySelector("#id_chooseParticipants .errorMessage")
                                            .textContent = message;
                                    }
                                    else {
                                        // altrimenti ricarica la pagina e mostra cosa è successo
                                        pageOrchestrator.refresh();

                                        document.querySelector("#id_createMeetingForm .errorMessage")
                                            .textContent = message;
                                    }
                            }

                            // se viene effettuata la richiesta, i tentativi vengono resettati
                            attempts = 0;
                        }
                    });
            }
            else {
                // se non va a buon fine la richiesta, viene mostrato il messaggio di errore e i partecipanti vengono resettati
                this.tableDiv.querySelector(".errorMessage").textContent =
                    "Troppi utenti selezionati, eliminane almeno "
                    + (newMeetingParameters.participants.length - newMeetingParameters.maxParticipantsNumber);

                newMeetingParameters.participants = [];

                // se i tentativi sono stati troppi, viene mostrata "cancellazione"
                if (attempts===3) {
                    let cancellazione = new Cancellazione();
                    document.getElementById("id_chooseParticipants").hidden = true;
                    cancellazione.show();
                    attempts = 0;
                }
            }
        }

        //SHOW PARTICIPANTS
        this.showParticipants = function() {
            let length = self.usernamesParticipants.length;
            let tbody = document.querySelector("#id_chooseParticipants tbody");

            // se non ci sono users nel database (al di fuori di sé stesso), viene mostrato con un messaggio
            if (length <= 1){
                tbody.closest("table").innerHTML = "";
                self.tableDiv.querySelector("h5.errorMessage").textContent = "There aren't users yet.";

                let anchor = document.createElement("a");

                anchor.textContent = "BACK";
                anchor.href = "#";

                anchor.addEventListener("click", (event => this.backButtonHandler(event)));

                this.tableDiv.appendChild(anchor);
                this.tableDiv.hidden = false;
            }
            else { // se ci sono invece, viene creata una tabella per poterli selezionare
                let tr, td, anchor;

                tbody.innerHTML = "";

                Array.from(self.usernamesParticipants).forEach(function(usernameParticipant) { // self visible here, not this
                    // The user of the session is not displayed
                    let sessionUser = sessionStorage.getItem("username");

                    if (usernameParticipant.toString().localeCompare(sessionUser.toString()) !== 0) {
                        tr = document.createElement("tr");
                        td = document.createElement("td");

                        anchor = document.createElement("a");
                        anchor.textContent = usernameParticipant.toString();
                        anchor.href = "#";

                        td.appendChild(anchor);
                        tr.appendChild(td);
                        tbody.appendChild(tr);

                        anchor.addEventListener("click", (event => self.selectHandler(event)));
                    }
                });

                let buttonDiv = document.querySelector("#id_chooseParticipants .buttonDiv");
                buttonDiv.innerHTML = "";

                let buttonLeft = document.createElement("button");
                buttonLeft.className = "buttonLeft";

                let buttonRight = document.createElement("button");
                buttonRight.className = "buttonRight";

                buttonDiv.appendChild(buttonRight);
                buttonDiv.appendChild(buttonLeft);

                buttonLeft.textContent = "BACK";
                buttonLeft.addEventListener("click", (event => this.backButtonHandler(event)));

                buttonRight.textContent = "INVITA";
                buttonRight.addEventListener("click", (event => this.invitaButtonHandler(event)));
            }

            // viene mostrato tutto
            this.tableDiv.hidden = false;
        }
    }

    function Cancellazione() {
        //handler
        // torna alla home page iniziale.
        this.backButtonHandler = function (event) {
            event.preventDefault();

            document.getElementById("id_threeAttemptsDone").hidden = true;
            pageOrchestrator.refresh();
        }

        // mostra cancellazione e assegna gli handler
        this.show = function () {
            document.getElementById("id_threeAttemptsDone").hidden = false;

            let anchor = document.getElementById("id_back");
            anchor.addEventListener("click", event => this.backButtonHandler(event));
            document.querySelector("#id_threeAttemptsDone h4").textContent
                = "Tre tentativi di definire una riunione con troppi partecipanti, la riunione non sarà creata."
        }
    }

    // oggetto per collezionare i parametri del meeting da creare che verranno passati al server
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
