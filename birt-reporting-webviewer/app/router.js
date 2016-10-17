/**
 * Created by cwarren on 10/16/16.
 */
import React from 'react';
import {Router, Route, browserHistory, IndexRoute } from 'react-router';

// Layouts
import AppLayout from './components/layouts/app-layout';

// Pages
import Home from './components/pages/home';
import About from './components/pages/about';

export default (
    <Router history={browserHistory}>
        <Route component={AppLayout} >
            <Route path="/" component={Home} />
            <Route path="/about" component={About} />
        </Route>
    </Router>
);