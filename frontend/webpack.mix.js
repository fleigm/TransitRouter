let mix = require('laravel-mix');
const tailwindcss = require('tailwindcss')

const backendResourceFolder = '..\\backend/src/main/resources/META-INF/resources'
const backendAssetFolder = '../backend/src/main/resources/META-INF/resources';


mix.js('src/app.js', backendResourceFolder)
    .less('design/app.less', backendResourceFolder)
    .options({
        postCss: [
            tailwindcss('design/tailwind.config.js'),
        ]
    })
    .copy('index.html', backendAssetFolder)
    .copyDirectory('fonts', backendAssetFolder + '/fonts')
    .copyDirectory('images', backendAssetFolder + '/images');


mix.disableNotifications();

