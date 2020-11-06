import axios from "axios/index";

export const http = axios.create();

export default {
    install(Vue, options = {}) {
        console.log('install http plugin');
        Vue.prototype.$http = http;
    }
}