import {createApp} from 'vue';
import router from './router';
import 'primevue/resources/themes/saga-blue/theme.css';
import 'primevue/resources/primevue.min.css';
import 'primeicons/primeicons.css';
import LeafletPlugin from "./plugins/LeafletPlugin";
import HttpPlugin from "./plugins/HttpPlugin";
import EventPlugin from "./plugins/EventPlugin";
import Components from './components';
import App from './App';
import PrimeComponents from "./plugins/PrimeComponents";


const app = createApp(App);

app.use(router)
    .use(HttpPlugin)
    .use(EventPlugin)
    .use(LeafletPlugin)
    .use(Components)
    .use(PrimeComponents)

app.mount('app');

window.app = app;