var cordova = require('cordova');

var InstallAPK = {
    install: function (fileUrl, error, success) {
        cordova.exec(success, error, 'FileTransferOpener', 'install', [fileUrl]);
    }
};

module.exports = InstallAPK;
