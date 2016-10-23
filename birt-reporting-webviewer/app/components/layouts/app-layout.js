import React from 'react';
import { Nav, NavItem, Navbar, MenuItem, NavDropdown } from 'react-bootstrap';
import { Link } from 'react-router';
import { IndexLinkContainer } from 'react-router-bootstrap';
import 'bootstrap/less/bootstrap.less';
import ReportListContainer from '../containers/report-list-container';
import * as reportApi from '../../api/report-api';
import { jsonToQueryString } from '../../util/index';
import Sidebar from 'react-sidebar';
import FontAwesome from 'react-fontawesome';
import ParameterModalNavItem from '../components/parameter-modal-navitem';

var AppLayout = React.createClass({

    getInitialState(){
        return {
            sidebarOpened: false,
            sidebarDocked: false
        }
    },

    componentWillMount: function() {
        var mql = window.matchMedia(`(min-width: 800px)`);
        mql.addListener(this.mediaQueryChanged);
        this.setState({mql: mql, sidebarDocked: mql.matches});
    },

    componentWillUnmount: function() {
        this.state.mql.removeListener(this.mediaQueryChanged);
    },

    mediaQueryChanged: function() {
        this.setState({sidebarDocked: this.state.mql.matches});
    },

    handleExport: function(selectedKey){
        var args = {
            reportId: 8,
            outputFormat: "pdf",
            parameters: {
                RunId: 161012112,
                Date: '2016-04-15'
            }
        };
        reportApi.getReport(args);
    },

    getReportURL: function(outputFormat){

        var args = {
            reportId: 8,
            outputFormat: outputFormat,
            parameters: {
                RunId: 161012112
            }
        };
        var queryString = jsonToQueryString(args);
        return "http://192.168.0.10:8088/main/system/birt-reporting/api/run-and-render" + queryString;
    },

    onSetSidebarOpen: function(open) {
        this.setState({
            sidebarOpened: open,
            sidebarDocked: this.state.sidebarDocked
        });
    },

    toggleSidebar: function(){
        this.onSetSidebarOpen(!this.state.sidebarOpened);
    },

    getSidebarContent: function(){
        return(
            <ReportListContainer/>
        );
    },

    getSidebarStyles: function(){
        return {
            root: {
                position: 'absolute',
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                overflow: 'hidden',
            },
            sidebar: {
                zIndex: 2,
                position: 'absolute',
                top: 0,
                bottom: 0,
                transition: 'transform .3s ease-out',
                WebkitTransition: '-webkit-transform .3s ease-out',
                willChange: 'transform',
                overflowY: 'auto',
                backgroundColor: 'rgba(248,248,248,1)'
            },
            content: {
                position: 'absolute',
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                overflow: 'auto',
                transition: 'left .3s ease-out, right .3s ease-out',
            },
            overlay: {
                zIndex: 1,
                position: 'fixed',
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                opacity: 0,
                visibility: 'hidden',
                transition: 'opacity .3s ease-out',
                backgroundColor: 'rgba(0,0,0,.3)',
            },
            dragHandle: {
                zIndex: 1,
                position: 'fixed',
                top: 0,
                bottom: 0,
            },
        };
    },

    render: function () {
        return (
            <div>
                <Sidebar sidebar={this.getSidebarContent()}
                         open={this.state.sidebarOpened}
                         docked={this.state.sidebarDocked}
                         styles={this.getSidebarStyles()}
                         onSetOpen={this.onSetSidebarOpen}>
                    <Navbar inverse>
                        <Navbar.Header>
                            { !this.state.sidebarDocked ?
                                <Nav className="pull-left">
                                    <NavItem onClick={this.toggleSidebar}>
                                        <FontAwesome name="chevron-right" />
                                    </NavItem>
                                </Nav>
                                : null
                            }
                            <IndexLinkContainer to="/">
                                <Navbar.Brand>
                                    Tamaki Reporting
                                </Navbar.Brand>
                            </IndexLinkContainer>
                            <Navbar.Toggle />
                        </Navbar.Header>
                        <Navbar.Collapse>
                            <Nav>
                                <ParameterModalNavItem eventKey={1}>Parameters</ParameterModalNavItem>
                                <NavItem eventKey={2}>Print</NavItem>
                                <NavDropdown eventKey={3} title="Export" id="nav-dropdown">
                                    <MenuItem eventKey="EXPORT_PDF" href={this.getReportURL("pdf")}>PDF</MenuItem>
                                    <MenuItem eventKey="EXPORT_XLS" href={this.getReportURL("xls")}>Excel</MenuItem>
                                    <MenuItem eventKey="EXPORT_WORD" href={this.getReportURL("doc")}>Google</MenuItem>
                                </NavDropdown>
                            </Nav>
                        </Navbar.Collapse>
                    </Navbar>
                    <div className="container-fluid">
                        <div className="row">
                            <div className="container">
                                {this.props.children}
                            </div>
                        </div>
                    </div>
                </Sidebar>
            </div>
        );
    }
});

export default AppLayout;
