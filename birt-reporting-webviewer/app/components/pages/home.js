/**
 * Created by cwarren on 10/16/16.
 */
import React from 'react';
import ParameterModal from '../components/parameter-modal';

var Home = React.createClass({
    render: function(){
        return(
            <div>
                <h1>main</h1>
                <p>Hello World</p>
                <ParameterModal/>
            </div>
        );
    }
});

export default Home;