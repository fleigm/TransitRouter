import axios from "axios/index";

export const http = axios.create();

export default {
    install(app, options = {}) {
        console.log('install http plugin');
        app.config.globalProperties.$http = http;
    }
}