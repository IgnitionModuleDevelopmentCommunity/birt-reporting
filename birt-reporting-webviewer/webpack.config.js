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
            {
                loader: 'babel',
                exclude: /node_modules/
            }
        ]
    }
}