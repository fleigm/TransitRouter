let mix = require('laravel-mix');
const tailwindcss = require('tailwindcss')

const backendResourceFolder = '..\\backend/src/main/resources/META-INF/resources'
const backendAssetFolder = '../backend/src/main/resources/META-INF/resources';
const dist = 'dist';

mix.setPublicPath('dist/')
    .js('src/app.js', 'app.js')
    .less('design/app.less', 'app.css')
    .options({
        postCss: [
            tailwindcss('design/tailwind.config.js'),
        ]
    })
    .copy('index.html', dist)
    .copyDirectory('fonts', dist + '/fonts')
    .copyDirectory('images', dist + '/images');

mix.browserSync({
    proxy: 'localhost:9000',
    injectChanges: true,
    files: [
        'dist/app.css',
    ]
}).options({
    hmrOptions: {
        host: 'localhost',
        port: '9000'
    }
});

Mix.listen('configReady', (webpackConfig) => {
    if (Mix.isUsing('hmr')) {
        // Remove leading '/' from entry keys
        webpackConfig.entry = Object.keys(webpackConfig.entry).reduce((entries, entry) => {
            entries[entry.replace(/^\//, '')] = webpackConfig.entry[entry];
            return entries;
        }, {});

        // Remove leading '/' from ExtractTextPlugin instances
        webpackConfig.plugins.forEach((plugin) => {
            if (plugin.constructor.name === 'ExtractTextPlugin') {
                plugin.filename = plugin.filename.replace(/^\//, '');
            }
        });
    }
});


mix.disableNotifications();

