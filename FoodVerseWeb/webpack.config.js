const modoDev = process.env.NODE_ENV !== 'production';
const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const TerserPlugin = require('terser-webpack-plugin');
const CssMinimizerPlugin = require('css-minimizer-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
    mode: modoDev ? 'development' : 'production',
    entry: './src/index.js',
    output: {
        filename: 'app.js',
        path: path.resolve(__dirname, 'build'),
        clean: true, // limpa a pasta build a cada build
    },
    devServer: {
        static: {
            directory: path.resolve(__dirname, 'src'), // serve a pasta onde está o header.html
        },
        port: 9000,
        open: true,
    },
    optimization: {
        minimize: !modoDev,
        minimizer: [
            new TerserPlugin({
                parallel: true,
                terserOptions: {
                    compress: true,
                },
            }),
            new CssMinimizerPlugin(),
        ],
    },
    plugins: [
        new MiniCssExtractPlugin({ filename: 'estilo.css' }),
        new CopyWebpackPlugin({
            patterns: [
                { from: 'src/**/*.html', to: '[name][ext]' },
                { from: 'src/imgs', to: 'imgs' },
            ],
        }),
    ],
    module: {
        rules: [
            {
                test: /\.s?[ac]ss$/i,
                use: [
                    MiniCssExtractPlugin.loader,
                    'css-loader',
                    'sass-loader',
                ],
            },
            {
                test: /\.(png|svg|jpg|gif)$/i,
                type: 'asset/resource',
            },
            {
                test: /\.(ttf|otf|eot|woff2?)$/i,
                type: 'asset/resource',
            },
        ],
    },
};
