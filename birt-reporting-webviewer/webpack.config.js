/**
 * Created by cwarren on 10/16/16.
 */
var path = require('path');

module.exports = {
    entry: "./app/app.js",
    output: {
        filename: "public/js/bundle.js",
        sourceMapFilename: "public/js/bundle.map"
    },
    devtool: '#source-map',
    module: {
        loaders: [
            { test: /\.js/,   loader: 'babel', exclude: /node_modules/},
            { test: /\.less/, loader: 'style!css!less'},
            { test: /\.css/, loader: 'style!css' },
            { test: /\.(woff2|woff|ttf|svg|eot)$/, loader: 'file' }
        ]
    }
}