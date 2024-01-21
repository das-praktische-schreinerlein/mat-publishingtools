#!/usr/bin/env node
'use strict';

var puppeteer = require('puppeteer');
var fs = require('fs');
var minimist = require('minimist');

var argv = minimist(process.argv.slice(2));
if (argv['_'].length < 2) {
    console.error('ERROR - require destFilename startPageNum', argv);
    process.exit(-1);
}

var url = argv['_'][0];
var fileName = argv['_'][1];
if (!url || !fileName) {
    console.error('ERROR - require url AND filename', argv);
    process.exit(-1);
}

var width = argv['width'] || 1210;
var delay = argv['javascript-delay'] || argv['delay']  || 10000;
var debug = argv['debug-javascript'] || argv['debug'] || false;
if (debug) {
    console.log('START - load url/fileName/width/delay:', url, fileName, width, delay);
}

var browser;
var page;
var windowDimension = ['--window-size=' + width + ',1080'];
var puppeteerOptions = {
    dumpio: true,
    headless: true,
    args: windowDimension,
    defaultViewport: {
        width: width,
        height: 1080
    }
};

return puppeteer.launch(puppeteerOptions).then(function(result) {
    if (debug) {
        console.log('new page w/h: ', windowDimension);
    }

    browser = result;
    return browser.newPage();
}).then(function(result) {
    if (debug) {
        console.log('load url:', url);
    }

    page = result;
    return page.setViewport({
        width: width,
        height: 1080
    }).then(function() {
        return page.goto(url, {
            waitUntil: 'domcontentloaded'
        });
    });
}).then(function() {
    if (debug) {
        console.log('wait till all rendered:', url, delay);
    }

    return page.waitForTimeout(delay);
}).then(function() {
    if (debug) {
        console.log('generate pdf:', url);
    }

    return page.pdf({format: 'a4'});
}).then(function(pdf) {
    if (debug) {
        console.log('save pdf:', fileName, url);
    }

    return new Promise(function(resolve, reject) {
        fs.writeFile(fileName, pdf, function (err, data) {
            if (err) {
                reject(err);
                return;
            }

            resolve(fileName);
            return;
        });
    });
}).then(function(file) {
    if (debug) {
        console.log('close browser');
    }

    return browser.close();
}).then(value => {
    if (debug) {
        console.log('DONE - command finished:', value, argv);
    }

    process.exit(0);
}).catch(function(reason) {
    console.error('ERROR - command failed:', reason, argv);
    if (browser) {
        browser.close();
    }

    process.exit(-1);
});

