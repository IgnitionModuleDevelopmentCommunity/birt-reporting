/**
 * Created by cwarren on 10/16/16.
 */
import React from 'react';
import { Nav, NavItem, Navbar, MenuItem, NavDropdown, ButtonToolbar, Button, ButtonGroup } from 'react-bootstrap';
import { LinkContainer } from 'react-router-bootstrap';

var AppLayout = React.createClass({
    render: function () {
        return (
            <div>
                <header>
                    <Navbar className="navbar navbar-inverse navbar-fixed-top">
                        <Navbar.Header className="navbar-header">
                            <LinkContainer to="/">
                                <Navbar.Brand className="navbar-brand">
                                    Report Viewer
                                </Navbar.Brand>
                            </LinkContainer>
                            <Navbar.Toggle />
                        </Navbar.Header>
                        <Navbar.Collapse className="navbar-collapse collapse">
                            <Nav className="nav navbar-nav">
                                <ButtonToolbar className="btn-toolbar">
                                    <ButtonGroup>
                                        <Button className="btn">Parameters</Button>
                                        <Button>Save</Button>
                                        <Button>Print</Button>
                                    </ButtonGroup>
                                </ButtonToolbar>
                            </Nav>
                        </Navbar.Collapse>
                    </Navbar>
                </header>
                <div className="container-fluid">
                    <div className="row">
                        <div className="col-md-3 sidebar">
                            <Nav bsStyle="pills" stacked={true} activeKey={1} className="nav nav-sidebar nav-pills">
                                <LinkContainer to="/"><NavItem  eventKey={1}>Home</NavItem></LinkContainer>
                                <LinkContainer to="/about"><NavItem eventKey={2}>About</NavItem></LinkContainer>
                            </Nav>
                        </div>
                    </div>
                </div>
                <div>
                    {this.props.children}
                </div>
            </div>
        );
    }
});

export default AppLayout;
