(function () {

    // page components
    var missionDetails, missionsList, wizard, pageOrchestrator = new PageOrchestrator(); // main controller

    window.addEventListener("load", () => {
        pageOrchestrator.start(); // initialize the components
        pageOrchestrator.refresh(); // display initial content
    }, false);






    function PageOrchestrator() {
        var alertContainer = document.getElementById("id_alert");


        // FUNZIONI
        this.start = function() {
            personalMessage = new PersonalMessage(sessionStorage.getItem('username'),
                document.getElementById("id_username"));
            personalMessage.show();

            missionsList = new MissionsList(
                alertContainer,
                document.getElementById("id_listcontainer"),
                document.getElementById("id_listcontainerbody"));

            missionDetails = new MissionDetails({ // many parameters, wrap them in an
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

            wizard = new Wizard(document.getElementById("id_createmissionform"), alertContainer);
            wizard.registerEvents(this);
        };


        this.refresh = function(currentMission) {
            missionsList.reset();
            missionDetails.reset();
            missionsList.show(function() {
                missionsList.autoclick(currentMission);
            }); // closure preserves visibility of this
            wizard.reset();
        };
    }

})();