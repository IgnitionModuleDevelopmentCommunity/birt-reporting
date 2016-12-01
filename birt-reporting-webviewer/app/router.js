import React from 'react';
import { Router, Route, browserHistory, IndexRoute } from 'react-router';
import store from './store';

// Layouts
import AppLayout from './components/layouts/app-layout';
import MainLayout from './components/layouts/main-layout';

// Pages
import Home from './components/pages/home';

// Container Components
import ReportViewerContainer from './components/containers/report-viewer-container';

const BASE_PATH = "/main/system/birt-reporting/web";

// Create an enhanced history that syncs navigation events with the store
// const history = syncHistoryWithStore(browserHistory, store);

export default (
    <Router history={browserHistory}>
        <Route path="/main/system/birt-reporting/web/" component={AppLayout} >
            <IndexRoute component={Home} />
            <Route path="reports">
                <Route path=":reportId" component={ReportViewerContainer}  />
            </Route>
        </Route>
    </Router>
);