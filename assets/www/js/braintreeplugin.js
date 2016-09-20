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
module.exports = getToken;