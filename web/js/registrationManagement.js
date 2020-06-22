(function(){

    document.getElementById("registrationButton").addEventListener("click", (event) => {
        event.preventDefault();
        var form = event.target.closest("form");

        var firstPassword = document.getElementById("registrationForm").elements["firstPassword"].value;
        var secondPassword = document.getElementById("registrationForm").elements["secondPassword"].value;

        if (firstPassword.localeCompare(secondPassword)===0 && form.checkValidity()){
            makeCall("POST", "SignUp", form,
                function (request) {
                    if (request.readyState === XMLHttpRequest.DONE){
                        var message = request.responseText;
                        switch (request.status) {
                            case 200:
                                sessionStorage.setItem("username", message);
                                window.location.href = "home.html";
                                break;
                            case 400:   //bad request
                                document.getElementById("errorMessageRegistration").textContent = message;
                                break;
                            case 500:   //server error
                                document.getElementById("errorMessageRegistration").textContent = message;
                                break;
                        }
                    }
                }, true);
        }
        else if (firstPassword.localeCompare(secondPassword)!==0)
            document.getElementById("errorMessageRegistration").textContent = "Passwords aren't equals, try again";
        else
            form.reportValidity();
    });
})();