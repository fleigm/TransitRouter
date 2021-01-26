import axios from "axios/index";

export const http = axios.create( {
    baseURL: 'http://localhost:8080',
    crossdomain: true,
});

export default {
    install(Vue, options = {}) {
        console.log('install http plugin');

        Vue.prototype.$http = http;
    }
}