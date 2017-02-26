import React from 'react';
import 'bootstrap/less/bootstrap.less';
import { Nav, NavItem, Navbar, MenuItem, NavDropdown } from 'react-bootstrap';
import { Link } from 'react-router';
import { IndexLinkContainer, LinkContainer } from 'react-router-bootstrap';
import * as reportsApi from '../../api/reports-api';

var AppLayout = React.createClass({

    getInitialState: function(){
        return {
            reports: []
        };
    },

    componentDidMount: function(){
        reportsApi.getReports(response => {
            console.log(response);
            this.setState({reports: response.data.rows});
        })
    },

    render: function() {
        return (
            <div>
                <div className="container-fluid">
                    <div className="row">
                        <Navbar inverse>
                            <Navbar.Header>
                                <IndexLinkContainer to="/main/system/birt-reporting/web/">
                                    <Navbar.Brand>
                                        Tamaki Reporting
                                    </Navbar.Brand>
                                </IndexLinkContainer>
                                <Navbar.Toggle />
                            </Navbar.Header>
                            <Navbar.Collapse>
                                <Nav>
                                    <NavDropdown eventKey="reports" title="Reports" id="nav-dropdown">
                                        {this.state.reports.map(report => {
                                            return(
                                                <LinkContainer to={"/reports/" + report[0]}>
                                                    <MenuItem eventKey={"reports" + report[0]}>
                                                        {report[2]}
                                                    </MenuItem>
                                                </LinkContainer>
                                            );
                                        })}
                                    </NavDropdown>
                                    <NavDropdown eventKey="1" title="Dropdown">
                                        <MenuItem eventKey="1.1">Action</MenuItem>
                                        <MenuItem eventKey="1.2">Another action</MenuItem>
                                        <MenuItem eventKey="1.3">Something else here</MenuItem>
                                        <MenuItem divider />
                                        <MenuItem eventKey="1.4">Separated link</MenuItem>
                                    </NavDropdown>
                                </Nav>
                            </Navbar.Collapse>
                        </Navbar>
                    </div>
                </div>
                <div className="main-content">
                    {this.props.children}
                </div>
            </div>
        );
    }
});

export default AppLayout;

/*
*
<ParameterModalNavItem eventKey={1} reportId={this.props.params.reportId}>
 <FontAwesome name="filter"/> Parameters
 </ParameterModalNavItem>
 <NavItem eventKey={2}>
 <FontAwesome name="print"/> Print
 </NavItem>
 <NavDropdown eventKey={3} title="Export" id="nav-dropdown">
 <ExportButtonContainer reportId={this.props.params.reportId}
 export="pdf"><FontAwesome name="file-pdf-o"/>
 PDF</ExportButtonContainer>
 <ExportButtonContainer reportId={this.props.params.reportId}
 export="xlsx"><FontAwesome name="file-excel-o"/>
 Excel</ExportButtonContainer>
 <ExportButtonContainer reportId={this.props.params.reportId}
 export="doc"><FontAwesome name="file-word-o"/>
 Word</ExportButtonContainer>
 </NavDropdown>
 */

/*
*
 {
 this.state.reports.map(report => {
 return(
 <NavDropdown eventKey={report.category} title={report.category}>
 {report.reports.map(report => {
 return(<MenuItem eventKey={report.id}>report.name</MenuItem>);
 })}
 </NavDropdown>
 );
 })
 }
* */