const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();
const dbOps = require('./db-ops');
const messageOps = require('./message-ops');

var queueActionOperationTrigger = (change) => {
    var val = change.after.val();
    return dbOps.getUser(val.username)
        .then(user => {
            var action = {
                actionType: val.actionType,
                id: val.id,
            }
            var index = user.tokens.indexOf(val.token);
            console.log(`Username: ${user.username} tokens: ${user.tokens} . Action holder token: ${val.token} i: ${index}`);
            if (index !== -1) {
                user.tokens.splice(index, 1) //exclude the action holder token
            } else {
                return Promise.reject(Error("Invalid FCM token"))
            }
            return user.tokens.length > 0
                ? messageOps.sendMessage(action, user.tokens)
                : Promise.reject(Error("There are no other devices connected beside the action holder"));
        }).catch(err => {
            var msg = `Queue action operation '${val.actionType}' not executed for user '${val.username}'. Cause: ${err}`
            return Promise.reject(Error(msg));
        })
};

var sendQueueActionOperation = (request, response) => {
    var username = request.query.username;
    var token = request.query.token;
    var action = request.body;
    if (username === null || token === null || action.actionType === null || action.id === null) {
        response.status(500).send(`Invalid data sent: username:${username} ; token:${token}; action: ${action.type} id:${action.id}`)
    }
    dbOps.queueAction(username, token, action.type, action.id)
        .then(() => {
            var msg = `Action queued: username:${username} ; token:${token}; action: ${action.actionType} id:${action.id}`;
            console.log(msg);
            return response.status(200).send(msg)
        })
        .catch(err => {
            console.log(`Error Action queing: ${action}. Cause: $`);
            return response.status(500).send(err)
        })
};

exports.queueActionOperationTriggerWrite = functions.database.ref(dbOps.QUEUE)
    .onWrite(queueActionOperationTrigger);

exports.createCollection = functions.https.onRequest(sendQueueActionOperation);

exports.registerDevice = functions.https.onRequest((request, response) => {
    var username = request.query.username;
    var token = request.query.token;
    if (username && token) {
        dbOps.addTokenToGroupDevice(username, token)
            .then(() => {
                return response.json({
                    username: username,
                    token: token
                })
            })
            .catch(error => {
                return response.status(500).send("Error " + error)
            })
    } else {
        response.status(500).send("Username or fcm token not provided")
    }
});

exports.unregisterDevice = functions.https.onRequest((request, response) => {
    var username = request.query.username
    var token = request.query.token
    if (username && token) {
        dbOps.removeTokenFromGroupDevice(username, token)
            .then((value) => {
                return response.send(`Removed device-token "${token}" for user "${username}" `)
            })
            .catch(error => {
                return response.status(500).send("Error " + error)
            })
    } else {
        response.status(500).send("Username or fcm token not provided")
    }
});

exports.obtainUserDevices = functions.https.onRequest((request, response) => {
    var username = request.query.username
    if (username) {
        dbOps.getUser(username)
            .then(data => {
                return response.json(data)
            })
            .catch(error => {
                return response.status(500).send(error.message)
            })
    } else {
        response.status(500).send("Username not provided")
    }
});

exports.clear = functions.https.onRequest((request, response) => {
    dbOps.clear()
        .then(() => {
            return response.send("Database cleared")
        }).catch(err => {
            return response.send(err.message)
        })
});

