const section = require('../section.js');
const database = require('../database.js');

const appInitializer = (app, configJSON) => {
    const connection = database.get('pttd_feedback', configJSON);

    app.get('/feedback', section.limits.LOW_STRENGTH, (req, res) => {
        new Promise((resolve, reject) => {
            connection.query('SELECT * FROM Form', (error, results, fields) => {
                if (error) {
                    console.error(error);
                    reject(error);
                    return;
                }
                resolve(results);
            });
        }).then((results) => {
            const lastForm = results[results.length - 1];
            res.status(200).send(lastForm);
        }).catch((error) => {
            res.status(500).send("Internal server error.");
        });
    });

    app.post('/feedback', section.limits.MAX_STRENGTH, (req, res) => {
        const formId = req.body.formId;
        const responseData = JSON.stringify(req.body.responseData);
        const submitterEmail = req.body.submitterEmail;
        new Promise((resolve, reject) => {
            connection.query('INSERT INTO Response (formId, responseData, submitterEmail) VALUES (?, ?, ?)', [formId, responseData, submitterEmail], (error, results, fields) => {
                if (error) {
                    console.error(error);
                    reject(error);
                    return;
                }
                resolve(results);
            });
        }).then((results) => {
            const lastForm = results[results.length - 1];
            res.status(200).send(lastForm);
            console.log(`[pttd_feedback] Form submitted from ${submitterEmail} with ID ${formId}.`);
        }).catch((error) => {
            res.status(500).send("Internal server error.");
            console.error("[pttd_feedback] Form submission failed.", error);
        });
    });
}

module.exports = appInitializer;