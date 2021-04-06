import axios from "axios/index";

export const http = axios.create( {
    baseURL: process.env.VUE_APP_BACKEND_ENDPOINT,
    crossdomain: true,
});

export default {
    install(Vue, options = {}) {
        console.log('install http plugin');

        Vue.prototype.$http = http;
    }
}