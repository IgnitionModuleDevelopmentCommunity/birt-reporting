/**
 * Created by cwarren on 10/16/16.
 */
import React from 'react';
import { Nav, NavItem, Navbar, MenuItem, NavDropdown, ButtonToolbar, Button, ButtonGroup } from 'react-bootstrap';
import { LinkContainer } from 'react-router-bootstrap';
import 'bootstrap/less/bootstrap.less';

var AppLayout = React.createClass({
    render: function () {
        return (
            <div>
                <Navbar inverse>
                    <Navbar.Header>
                        <Navbar.Brand>
                            <a href="#">React-Bootstrap</a>
                        </Navbar.Brand>
                        <Navbar.Toggle />
                    </Navbar.Header>
                    <Navbar.Collapse>
                        <Nav>
                            <NavItem eventKey={1}>Parameters</NavItem>
                            <NavItem eventKey={2}>Export</NavItem>
                            <NavItem eventKey={3}>Print</NavItem>
                        </Nav>
                    </Navbar.Collapse>
                </Navbar>
                <div className="col-xs-4 sidebar">
                    <div className="sidebar-content">
                        <section>
                            <Nav bsStyle="pills" stacked activeKey={1}>
                                <NavItem eventKey={1} href="/home">NavItem 1 content</NavItem>
                                <NavItem eventKey={2} title="Item">NavItem 2 content</NavItem>
                                <NavItem eventKey={3} disabled>NavItem 3 content</NavItem>
                            </Nav>
                        </section>
                    </div>
                </div>

                <div className="col-xs-8">
                    {this.props.children}
                </div>

            </div>
        );
    }
});

export default AppLayout;
