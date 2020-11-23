import Menubar from 'primevue/menubar';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import InputText from 'primevue/inputtext';
import OverlayPanel from 'primevue/overlaypanel';



export default {
    install(app) {
        app.component('Menubar', Menubar);
        app.component('Button', Button);
        app.component('Dialog', Dialog);
        app.component('InputText', InputText);
        app.component('OverlayPanel', OverlayPanel);
    }
}