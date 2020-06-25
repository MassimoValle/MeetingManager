/**
 * Login management
 */

(function() { // avoid variables ending up in the global scope

    document.getElementById("loginbutton").addEventListener('click', (e) => {
        e.preventDefault();

        var form = e.target.closest("form");
        if (form.checkValidity()) {
            makeCall("POST", 'CheckLogin', e.target.closest("form"),
                function(req) {
                    if (req.readyState === XMLHttpRequest.DONE) {

                        var message = req.responseText;     // risposta del server
                        switch (req.status) {
                            case 200:
                                sessionStorage.setItem('username', message);
                                window.location.href = "home.html";
                                break;
                            case 400: // bad request
                                document.getElementById("errorMessageLogin").textContent = message;
                                break;
                            case 401: // unauthorized
                                document.getElementById("errorMessageLogin").textContent = message;
                                break;
                            case 500: // server error
                                document.getElementById("errorMessageLogin").textContent = message;
                                break;
                        }
                    }
                }, false);
        } else {
            form.reportValidity();
        }
    });

})();
