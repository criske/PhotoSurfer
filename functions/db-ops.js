const admin = require('firebase-admin');
const FCM_USER_GROUP_DEVICES = "fcm_user_group_devices"


const fcmCollectionRef = admin.database().ref(FCM_USER_GROUP_DEVICES)

var getUser = function (username) {
    return fcmCollectionRef.child(username)
        .once('value')
        .then((snap) => {
            let tokens = snap.val() || []
            let user = {
                username: username,
                tokens: tokens
            }
            return Promise.resolve(user)
        })
        .catch(() => {
            return Promise.reject(Error("User not found"))
        })
}

exports.addTokenToGroupDevice = function (username, token) {
    return getUser(username)
        .then(user => {
            var tokens = user.tokens
            if (tokens.indexOf(token) === -1) {
                tokens.push(token)
                return fcmCollectionRef.child(username).set(tokens)
            } else {
                return Promise.reject(Error("Token " + token + " already added"))
            }
        })
        .catch(e => {
            return fcmCollectionRef.child(username).set([token])
        })
}

exports.removeTokenFromGroupDevice = function (username, token) {
    return getUser(username)
        .then(user => {
            var tokens = user.tokens
            var index = tokens.indexOf(token)
            if (index !== -1) {
                tokens.splice(index, 1)
                return fcmCollectionRef.child(username).set(tokens)
            }else{
                return Promise.reject(Error("Token " + token + " doesn't exists"))
            }
        })
}

exports.getUser = getUser

exports.clear = function () {
    return fcmCollectionRef.set(null)
}

exports.root = fcmCollectionRef

exports.initRoot = function () {
    admin.database().goOffline()
}

exports.FCM_USER_GROUP_DEVICES = FCM_USER_GROUP_DEVICES;