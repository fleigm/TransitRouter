import axios from "axios/index";
import Config from "../config";

export const http = axios.create( {
    baseURL: Config.apiEndpoint,
    crossdomain: true,
});

export default {
    install(Vue, options = {}) {
        console.log('install http plugin');

        Vue.prototype.$http = http;
    }
}