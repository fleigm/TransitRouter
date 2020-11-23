import mitt from 'mitt';

export default {
    install(app) {
        console.log('Install event plugin');
        app.config.globalProperties.$events = mitt();
    }
}