import React from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import HomePage from '.HomePage';
import LandingPage from './LandingPage';

function App() {
    return (
        <Router>
            <Switch>
                <Route path="/landing" component={LandingPage} />
                <Route path="/" component={HomePage} />
            </Switch>
        </Router>
    );
}

export default App;
