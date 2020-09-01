let mix = require('laravel-mix');
const tailwindcss = require('tailwindcss')
const resourceFolder = '..\\resources/META-INF/resources'
const assetFolder = '../resources/META-INF/resources'


mix.js('src/app.js', resourceFolder)
    .less('design/app.less', resourceFolder)
    .options({
        postCss: [
            tailwindcss('design/tailwind.config.js'),
        ]
    })
    .copy('index.html', assetFolder)
    .copyDirectory('fonts', assetFolder + '/fonts')
    .copyDirectory('images', assetFolder + '/images');

mix.disableNotifications();

