/**
 * Created by cwarren on 10/16/16.
 */

import React from 'react';
import { Modal, ControlLabel, FormControl, FormGroup, Button } from 'react-bootstrap';


var ParameterModal = React.createClass({

    getInitialState: function(){
        return { showModial: false }
    },

    close(){
        return this.setState({ showModial: false });
    },

    open(){
        return this.setState({ showModial: true });
    },

    render: function(){

        return(
            <div>
            <Button bsSize="large" onClick={this.open}>Open</Button>

            <Modal show={this.state.showModial} onHide={this.close}>
                <Modal.Header closeButton>
                    <Modal.Title>Parameters</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <h4>Parameter Modial</h4>
                    <form>
                        <FormGroup>
                        <FormControl id="formControlsText" type="text" label="Text" placeholder="Enter Text" />
                        <ControlLabel>Select</ControlLabel>
                        <FormControl componentClass="select" placeholder="select">
                            <option value="parameter1">Parameter 1</option>
                            <option value="parameter2">Parameter 2</option>
                        </FormControl>
                        </FormGroup>
                        <Button type="submit">Submit</Button>
                    </form>
                </Modal.Body>
                <Modal.Footer>
                    <Button onClick={this.close}>Close</Button>
                </Modal.Footer>
            </Modal>
            </div>
        );

    }

});

export default ParameterModal;