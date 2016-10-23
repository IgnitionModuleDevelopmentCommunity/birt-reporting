/**
 * Created by cwarren on 10/16/16.
 */
import React from 'react';
import ParameterModal from '../components/parameter-modal';

var Home = React.createClass({
    render: function(){
        return(
            <div>
                <h1>Tamaki Reporting</h1>

                <article>
                    Welcome to Tamaki Reporting!!!  To get started, choose one of the reports from the left.
                </article>

                <ParameterModal/>
            </div>
        );
    }
});

export default Home;