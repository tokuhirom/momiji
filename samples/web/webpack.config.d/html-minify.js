// webpack.config.d/html-plugin-config.js

const HtmlWebpackPlugin = require('html-webpack-plugin');
const path = require('path');

config.plugins.push(
  new HtmlWebpackPlugin({
    template: path.resolve(__dirname, '../../../../samples/web/src/jsMain/resources/index.html'),
    filename: 'index.html',
    inject: 'body'
  })
);
