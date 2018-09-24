const admin = require('firebase-admin');

exports.sendMessage = function (action, tokens) {
    var payload = {data: {
        actionType: action.actionType,
        id: String(action.id)
    }};
    return admin.messaging().sendToDevice(tokens, payload).then(()=>{
        console.log(`Message action ${action.actionType} sent to devices`)
        return Promise.resolve()
    })
}