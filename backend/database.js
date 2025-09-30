const mysql = require('mysql2');

let databaseMap = new Map();

function get(database, configJSON) {
    let databaseConnectionPresent = databaseMap.has(database);
    if (databaseConnectionPresent) {
        return databaseMap.get(database);
    } else {
        let databaseConnection = createDatabase(database, configJSON);
        databaseMap.set(database, databaseConnection);
        return databaseConnection;
    }
}

function endAll() {
    for (let databaseConnectionKey of databaseMap.keys()) {
        let databaseConnection = databaseMap.get(databaseConnectionKey);
        const name = databaseConnectionKey;
        databaseConnection.end((err) => {
            if (err) {
                console.error(`[${name}] Error closing database connection.`, err);
            } else {
                console.log(`[${name}] Database connection closed.`);
            }
        });
    }
}

function createDatabase(database, configJSON) {
    let connectionJSON = configJSON.sqlConnection;
    connectionJSON.database = database;
    return mysql.createPool(connectionJSON);
}

module.exports = {
    get,
    endAll
}