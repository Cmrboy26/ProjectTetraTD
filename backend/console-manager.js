function initialize() {
    const readline = require('readline');
    const { exit } = require('process');
    const { stopAPI } = require('./app.js');

    const rl = readline.createInterface({
        input: process.stdin,
        output: process.stdout
    });
    
    rl.on('line', (input) => {
        switch (input) {
            case '?':
            case 'help':
                console.log(">------|------<");
                console.log(" - Commands:");
                console.log(" help - Display this message.");
                console.log(" stop - Stop the API.");
                console.log(">------|------<");
                break;
            case 'quit':
            case 'stop':
            case 'exit':
                rl.close();
                break;
            default:
                break;
        }
    });
    
    rl.on('close', () => {
        stopAPI(); // broken
    });    
}

module.exports = initialize;
