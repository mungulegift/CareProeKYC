import React, { useState, useEffect } from 'react';
import logo from '../../webapp/carepromosip/src/logo.png';
import '../../webapp/carepromosip/src/App.css';

function App() {
    const [clientDetails, setClientDetails] = useState(null);
    const [loading, setLoading] = useState(false);

    const handleSubmit = (e) => {
        e.preventDefault();
        setLoading(true);
        fetch(`http://localhost:880/demo/api/clientDetails`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                // setClientDetails(data);
                // setLoading(false);
                console.log(data);
            })
            .catch(error => {
                console.error('Error fetching client details:', error);
                setLoading(false);
            });
    };
    

    useEffect(() => {
        if (clientDetails) {
            console.log("Client details:", clientDetails);
        }
    }, [clientDetails]);

    return (           
        <div className="App">
            <header className="App-header">
                <img src={logo} className="App-logo" alt="logo" />

                {loading && <p className="loader">Loading please wait...</p>}

                {clientDetails && (
                    <div>
                        <div className="row">
                            <div className="form-group">
                                <label className="label">First Name:</label>
                                <input className="input" type="text" value={clientDetails.response.givenName[0].value} disabled/>
                            </div>
                            <div className="form-group">
                                <label className="label">Surname:</label>
                                <input className="input" type="text" value={clientDetails.response.familyName[0].value} disabled/>
                            </div>
                        </div>

                        <div className="row">
                            <div className="form-group">
                                <label className="label">Sex:</label>
                                <input className="input" type="text" value={clientDetails.response.gender[0].value} disabled/>
                            </div>
                            <div className="form-group">
                                <label className="label">Date of Birth:</label>
                                <input className="input" type="text" value={clientDetails.response.dateOfBirth} disabled/>
                            </div>
                        </div>

                        <div className="row">
                            <div className="form-group">
                                <label className="label">NRC:</label>
                                <input className="input" type="text" value={clientDetails.response.individualId} disabled/>
                            </div>
                            <div className="form-group">
                                <label className="label">Cell Phone:</label>
                                <input className="input" type="text" value={clientDetails.response.phone} disabled/>
                            </div>
                        </div>
                    </div>
                )}
            </header>
        </div>
    );
}

export default App;
