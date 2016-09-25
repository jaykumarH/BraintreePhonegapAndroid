var getToken = {
    getClientToken: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'BraintreePlugin', // mapped to our native Java class called "Calendar"
            'getToken', // with this action name
            [                  // and this array of custom arguments to create our entr
            ]
        );
     }
}

var getNonce = {
    getNonceFromBT: function(amount,cardNo,expirationDate,cvc,successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'BraintreePlugin', // mapped to our native Java class called "Calendar"
            'getNonce', // with this action name
            [{
            "amount":amount,
            "cardNo":cardNo,
            "expirationDate":expirationDate,
            "cvc":cvc
               }               // and this array of custom arguments to create our entr
            ]
        );
     }
}
module.exports = getToken;
module.exports = getNonce;