import React from 'react';
import { Router, Route, browserHistory, IndexRoute } from 'react-router';

// Layouts
import AppLayout from './components/layouts/app-layout';

// Pages
import Home from './components/pages/home';
import ReportViewerContainer from './components/containers/report-viewer-container';

export default (
    <Router history={browserHistory}>
        <Route path="/main/system/birt-reporting/web/" component={AppLayout} >
            <IndexRoute component={Home} />
            <Route path="reports/">
                <IndexRoute component={ReportViewerContainer} />
                <Route path=":id" component={ReportViewerContainer} />
            </Route>
        </Route>
        <Route path="/" component={AppLayout} >
            <IndexRoute component={Home} />
            <Route path="reports">
                <Route path=":id" component={ReportViewerContainer} />
            </Route>
        </Route>
    </Router>
);