import React from 'react';
import 'bootstrap/less/bootstrap.less';
import { Nav, NavItem, Navbar, MenuItem, NavDropdown } from 'react-bootstrap';
import { Link } from 'react-router';
import { IndexLinkContainer } from 'react-router-bootstrap';
import ReportListContainer from '../containers/report-list-container';
import Sidebar from 'react-sidebar';
import FontAwesome from 'react-fontawesome';
import ParameterModalNavItem from '../components/parameter-modal-navitem';
import ExportButtonContainer from '../containers/export-button-container';

var AppLayout = React.createClass({

    getInitialState(){
        return {
            sidebarOpened: false,
            sidebarDocked: false
        }
    },

    componentWillMount: function() {
        var mql = window.matchMedia(`(min-width: 945px)`);
        mql.addListener(this.mediaQueryChanged);
        this.setState({mql: mql, sidebarDocked: mql.matches});
    },

    componentWillUnmount: function() {
        this.state.mql.removeListener(this.mediaQueryChanged);
    },

    mediaQueryChanged: function() {
        this.setState({sidebarDocked: this.state.mql.matches});
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
                    <div className="container-fluid">
                        <div className="row">
                            <Navbar inverse>
                                <Navbar.Header>
                                    { !this.state.sidebarDocked ?
                                        <Nav className="pull-left">
                                            <NavItem onClick={this.toggleSidebar}>
                                                <FontAwesome name="chevron-right"/>
                                            </NavItem>
                                        </Nav>
                                        : null
                                    }
                                    <IndexLinkContainer to="/main/system/birt-reporting/web/">
                                        <Navbar.Brand>
                                            Tamaki Reporting
                                        </Navbar.Brand>
                                    </IndexLinkContainer>
                                    <Navbar.Toggle />3
                                </Navbar.Header>
                                <Navbar.Collapse>
                                    <Nav>
                                        <ParameterModalNavItem eventKey={1} reportId={this.props.params.reportId}>
                                            <FontAwesome name="filter" /> Parameters
                                        </ParameterModalNavItem>
                                        <NavItem eventKey={2}>
                                            <FontAwesome name="print" /> Print
                                        </NavItem>
                                        <NavDropdown eventKey={3} title="Export" id="nav-dropdown">
                                            <ExportButtonContainer reportId={this.props.params.reportId} export="pdf"><FontAwesome name="file-pdf-o"/> PDF</ExportButtonContainer>
                                            <ExportButtonContainer reportId={this.props.params.reportId} export="xlsx"><FontAwesome name="file-excel-o"/> Excel</ExportButtonContainer>
                                            <ExportButtonContainer reportId={this.props.params.reportId} export="doc"><FontAwesome name="file-word-o"/> Word</ExportButtonContainer>
                                        </NavDropdown>
                                    </Nav>
                                </Navbar.Collapse>
                            </Navbar>
                        </div>
                    </div>
                    <div className="main-content">
                        {this.props.children}
                    </div>
                </Sidebar>
            </div>
        );
    }
});

export default AppLayout;
