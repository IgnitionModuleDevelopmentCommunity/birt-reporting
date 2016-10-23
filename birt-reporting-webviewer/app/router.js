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

// Container Components
import ReportViewerContainer from './components/containers/report-viewer-container';

const BASE_PATH = "";//"/main/system/birt-reporting/web";

export default (
    <Router history={browserHistory}>
        <Route component={AppLayout} >
            <Route path={BASE_PATH + "/"} component={Home} />
            <Route path={BASE_PATH + "/about"} component={About} />
            <Route path={BASE_PATH + "/reports"}>
                <Route path=":reportId" component={ReportViewerContainer}  />
            </Route>
        </Route>
    </Router>
);