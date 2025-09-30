const fs = require('fs');
const express = require('express');
const database = require('./database.js');

const config = fs.readFileSync('config.json');
const configJSON = JSON.parse(config);

// Initialize the app

const port = configJSON.port;
const app = express();
app.use(express.json());

app.set('port', port)
app.set('trust proxy', 1);

console.log(`[API] Initializing ${configJSON.activeSections.length} section${configJSON.activeSections.length > 1 ? 's' : ''}...`);
var sectionCount = 1;
var total = configJSON.activeSections.length;
for (const section of configJSON.activeSections) {
    const sectionModule = require(`./sections/${section}.js`);
    sectionModule(app, configJSON);
    console.log(`[API] [${sectionCount++}/${total}] Section '${section}' initialized.`);
}

// Start the API
const useHTTPS = configJSON.useHTTPS;

if (!useHTTPS) {
    const http = require('http');
    http.createServer(app).listen(port, function () {
        console.log(`[API] API listening on port '${port}' and using HTTP.`);
    });
} else {
    const https = require('https');

    var privateKey = fs.readFileSync('https_cert/key.pem');
    var certificate = fs.readFileSync('https_cert/cert.pem');
    
    const options = {
        key: privateKey,
        cert: certificate
    };

    https.createServer(options, app).listen(port, function () {
        console.log(`[API] API listening on port '${port}' and using HTTPS.`);
    });
}

function stopAPI() {
    console.log("[API] Stopping API...");
    // Close the connection when done
    database.endAll();
    process.exit(1);
};

module.exports = {
    app: app,
    stopAPI: stopAPI
}

process.on('SIGINT', function() {
    stopAPI();
});

require('./console-manager.js')();

