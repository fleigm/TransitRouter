import Vue from 'vue';

export const events = new Vue();

export default {
    install(Vue) {
        Vue.prototype.$events = events;
    }
}