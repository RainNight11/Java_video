import { createApp } from 'vue';
import { createPinia } from 'pinia';

import App from './App.vue';
import router from './router';

// Import custom SASS/SCSS theme
import './assets/scss/main.scss';

// Import Bootstrap's JS
import 'bootstrap';

// Import Bootstrap Icons
import 'bootstrap-icons/font/bootstrap-icons.css';

const app = createApp(App);

app.use(createPinia());
app.use(router);

app.mount('#app');
