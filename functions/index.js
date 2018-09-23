const functions = require('firebase-functions');
// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();
const dbOps = require('./db-ops')


// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
exports.createCollection = functions.https.onRequest((request, response) => {
    var message = {
        data: {
            id: '850'
        },
        topic: "COLLECTION_CREATED"
    };
    admin.messaging().send(message)
        .then((response) => {
            return {
                code: 200,
                messsage: "Successfully sent message: ${response}"
            }
        })
        .catch((error) => {
            return {
                code: 200,
                messsage: "Error: ${error}"
            }
        });
    response.status(200).send("OK")
});

exports.registerDevice = functions.https.onRequest((request, response) => {
    var username = request.query.username
    var token = request.query.token
    if (username && token) {
        dbOps.addTokenToGroupDevice(username, token)
            .then((value) => {
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
})

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
})

exports.obtainUserDevices = functions.https.onRequest((request, response) => {
    var username = request.query.username
    if(username){
        dbOps.getUser(username)
            .then(data => {
                return response.json(data)
            })
            .catch(error =>{
                return response.status(500).send(error.message)
            })
    } else {
        response.status(500).send("Username not provided")
    }
})

exports.clear = functions.https.onRequest((request, response) => {
    dbOps.clear()
    .then(()=>{
        return response.send("Database cleared")
    }).catch(err => {
        return response.send(err.message)
    })
})

