import React from 'react';
import ReactDOM from 'react-dom';
import router from './router';
//import store from './store';
//import { Provider } from 'react-redux';

ReactDOM.render(
    //<Provider store={store}>{router}</Provider>,
    router,
    document.getElementById('root')
);
