/**
 * AJAX call management
 */

function makeCall(method, url, formElement, cback, reset) {
    var req = new XMLHttpRequest(); // visible by closure

    // ogni volta che lo stato cambia viene triggerata la callback
    req.onreadystatechange = function() {
        cback(req)
    }; // closure


    req.open(method, url);  // viene aperta la richiesta

    if (formElement == null) {
        req.send(); // invio semplice
    } else {
        req.send(new FormData(formElement));    // invio di una form
    }


    if (formElement !== null && reset === true) {   // se gli viene passata la form e reset==true
        formElement.reset();
    }
}