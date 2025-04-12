// src/pages/TournamentRegistrationPage.jsx
import React, { useState, useEffect } from 'react';
import { Container, Card, CardContent, Typography, FormControl, InputLabel, Select, MenuItem, Button, Snackbar, Alert } from '@mui/material';
import axiosInstance from '../api/axiosConfig';
import HowToRegIcon from '@mui/icons-material/HowToReg';

function TournamentRegistrationPage() {
    const [tournaments, setTournaments] = useState([]);
    const [selectedTournament, setSelectedTournament] = useState('');
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });
    const storedUser = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;

    useEffect(() => {
        axiosInstance.get('/tournaments/all')
            .then(response => setTournaments(response.data))
            .catch(error => {
                setSnackbar({ open: true, message: 'Error fetching tournaments: ' + (error.response?.data || error.message), severity: 'error' });
            });
    }, []);

    const handleRegister = () => {
        if (!selectedTournament) {
            setSnackbar({ open: true, message: 'Please select a tournament.', severity: 'error' });
            return;
        }
        axiosInstance.post(`/tournaments/${selectedTournament}/register`, null, {
            params: { playerId: storedUser.id }
        })
            .then(() => setSnackbar({ open: true, message: 'Registered successfully!', severity: 'success' }))
            .catch(error => {
                setSnackbar({ open: true, message: 'Registration failed: ' + (error.response?.data || error.message), severity: 'error' });
            });
    };

    const handleCloseSnackbar = (event, reason) => {
        if (reason === 'clickaway') return;
        setSnackbar({ ...snackbar, open: false });
    };

    return (
        <Container sx={{ mt: 4 }}>
            <Card>
                <CardContent>
                    <Typography variant="h5" gutterBottom>
                        Tournament Registration
                    </Typography>
                    <FormControl fullWidth margin="normal">
                        <InputLabel>Select Tournament</InputLabel>
                        <Select
                            value={selectedTournament}
                            onChange={(e) => setSelectedTournament(e.target.value)}
                            label="Select Tournament"
                        >
                            <MenuItem value="">
                                <em>-- Select Tournament --</em>
                            </MenuItem>
                            {tournaments.map((t) => (
                                <MenuItem key={t.id} value={t.id}>{t.name}</MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                    <Button variant="contained" color="primary" onClick={handleRegister} startIcon={<HowToRegIcon />} sx={{ mt: 2 }}>
                        Register
                    </Button>
                </CardContent>
            </Card>
            <Snackbar
                open={snackbar.open}
                autoHideDuration={6000}
                onClose={handleCloseSnackbar}
                anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
            >
                <Alert onClose={handleCloseSnackbar} severity={snackbar.severity} sx={{ width: '100%' }}>
                    {snackbar.message}
                </Alert>
            </Snackbar>
        </Container>
    );
}

export default TournamentRegistrationPage;
