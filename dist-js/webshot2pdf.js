#!/usr/bin/env node
'use strict';

var puppeteer = require('puppeteer');
var fs = require('fs');
var minimist = require('minimist');

var argv = minimist(process.argv.slice(2));

var debug = argv['debug-javascript'] || argv['debug'] || false;
var width = argv['width'] || 1210;
var delay = argv['javascript-delay'] || argv['delay']  || 6000;
var url = argv['_'][0];
var fileName = argv['_'][1];

console.log('url' , url, ' fileName', fileName, '--width', width, '--delay', delay);
if (!url || !fileName) {
    console.error('ERROR - require url AND filename', argv);
    process.exit(-1);
}

// --crop-w 600 --crop-x 4

var browser;
var page;
puppeteer.launch({
    dumpio: true,
    headless: true,
    args: ['--window-size=' + width + ',1080'],
    defaultViewport: {
        width: width,
        height: 1080
    }
}).then(function(result) {
    console.log('new page');
    browser = result;
    return browser.newPage();
}).then(function(result) {
    console.log('load url', url);
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
    console.log('wait till all rendered', url);
    return page.waitForTimeout(delay);
}).then(function() {
    console.log('generate pdf', url);

    return page.pdf({format: 'a4'});
}).then(function(pdf) {
    console.log('save pdf', fileName);
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
    console.log('close browser');
    return browser.close();
}).then(value => {
    console.log('DONE - command finished:', value, argv);
    process.exit(0);
}).catch(function(reason) {
    console.error('ERROR - command failed:', reason, argv);
    if (browser) {
        browser.close();
    }

    process.exit(-1);
});

