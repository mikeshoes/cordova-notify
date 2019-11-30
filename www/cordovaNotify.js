var cordova = require('cordova'),
    exec = require('cordova/exec');

exports.init = function (arg0, success, error) {
    exec(success, error, 'CordovaNotify', 'init', [arg0]);
};

var RemoteMessage = function () {
    // Create new event handlers on the window (returns a channel instance)
    this.channels = {
        messageArrived: cordova.addWindowEventHandler('onmessagearrived'),
        noticeArrived: cordova.addWindowEventHandler('onnoticearrived')
    };

    for (var key in this.channels) {
        this.channels[key].onHasSubscribersChange = RemoteMessage.onHasSubscribersChange;
    }
};

function handlers () {
    return message.channels.messageArrived.numHandlers +
        message.channels.noticeArrived.numHandlers;
}

// 当有订阅时，就会调用这个方法。
RemoteMessage.onHasSubscribersChange = function () {
  // If we just registered the first handler, make sure native listener is started.
      //订阅数目为1时启动，为0时停止。
    if (this.numHandlers === 1 && handlers() === 1) {
        exec(message._status, message._error, 'CordovaNotify', 'registry', [{}]);
    } else if (handlers() === 0) {
        exec(null, null, 'CordovaNotify', 'deregistry', [{}]);
    }
};

/**
 * Callback for battery status
 *
 * @param {Object} info            keys: level, isPlugged
 */
RemoteMessage.prototype._status = function (info) {
    if (info) {
        if (info.type == 'message') {
            cordova.fireWindowEvent('onmessagearrived', info.msg);
        } else if (info.type="notice") {
            cordova.fireWindowEvent('onnoticearrived', info.msg);
        }
    }
};

/**
 * Error callback for battery start
 */
RemoteMessage.prototype._error = function (e) {
    console.log('Error initializing Battery: ' + e);
};

var message = new RemoteMessage(); // jshint ignore:line
exports.message = message;
