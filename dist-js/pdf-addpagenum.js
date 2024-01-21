#!/usr/bin/env node
'use strict';

var minimist = require('minimist');
var {spawn} = require('child_process');

var argv = minimist(process.argv.slice(2));
if (argv['_'].length < 2) {
    console.error('ERROR - require destFilename startPageNum', argv);
    process.exit(-1);
}

var destFilename = argv['_'][0];
var startPageNum = argv['_'][1];
if (!destFilename || startPageNum < 1) {
    console.error('ERROR - require destFilename startPageNum', argv);
    process.exit(-1);
}

var debug = argv['debug-javascript'] || argv['debug'] || false;
if (debug) {
    console.log('START - addPageNum destFilename/startPageNum:', destFilename, startPageNum);
}


var projectDir = __dirname;
var command = 'java';
var commandArgs = [
    '-cp', projectDir + '/../target/matpublishingtools-1.0.0-SNAPSHOT-jar-with-dependencies.jar',
    '-Xmx512m',
    '-Xms128m',
    '-Dlog4j.configuration=file:' + projectDir + '/../config/log4j.properties',
    'de.mat.utils.pdftools.PdfAddPageNum',
    destFilename,
    destFilename,
    startPageNum
];

var stdoutHandler = function (buffer) {
    if (!buffer) {
        return;
    }

    if (debug) {
        console.log(buffer.toString());
    }
};

var stderrHandler = function (buffer) {
    if (!buffer) {
        return;
    }

    console.error(buffer.toString());
};

return new Promise(function (resolve, reject) {
    if (debug) {
        console.log('start java-process: ', command, commandArgs);
    }

    var process = spawn(command, commandArgs);
    if (stdoutHandler) {
        process.stdout.on('data', (chunk) => {
            // data from standard output is here as buffers
            stdoutHandler.call(this, chunk);
        });
    }

    if (stderrHandler) {
        process.stderr.on('data', (chunk) => {
            // data from standard output is here as buffers
            stderrHandler.call(this, chunk);
        });
    }

    process.on('close', function (code) {
        resolve(code);
    });
    process.on('error', function (err) {
        reject(err);
    });
}).then(code => {
    if (code !== 0) {
        var errMsg = 'FAILED - java-process "' + command + ' ' + commandArgs.join(' ') + '" failed returnCode:' + code;
        return Promise.reject(errMsg);
    }

    if (debug) {
        console.log('DONE - command finished:', code, argv);
    }

    process.exit(0);
}).catch(function(reason) {
    console.error('ERROR - command failed:', reason, argv);
    process.exit(-1);
});

