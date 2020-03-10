var cordova = require('cordova');

var InstallAPK = {
    install: function (fileUrl, success, error) {
        cordova.exec(success, error, 'FileTransferOpener', 'install', [fileUrl]);
    }
};

module.exports = InstallAPK;
