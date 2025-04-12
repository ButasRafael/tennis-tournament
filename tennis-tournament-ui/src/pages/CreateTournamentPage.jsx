// src/pages/CreateTournamentPage.jsx
import React, { useState } from 'react';
import { Container, Card, CardContent, Typography, TextField, Button, Grid, Snackbar, Alert } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosConfig';

function CreateTournamentPage() {
    const [name, setName] = useState('');
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [registrationDeadline, setRegistrationDeadline] = useState('');
    const [maxPlayers, setMaxPlayers] = useState(32);
    const [minPlayers, setMinPlayers] = useState(2);
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });
    const navigate = useNavigate();

    const storedUser = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;
    const currentUserId = storedUser ? storedUser.id : null;

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await axiosInstance.post('/tournaments/create', null, {
                params: {
                    name,
                    startDate,
                    endDate,
                    registrationDeadline,
                    maxPlayers,
                    minPlayers,
                    currentUserId,
                },
            });
            setSnackbar({ open: true, message: 'Tournament created successfully!', severity: 'success' });
            setName('');
            setStartDate('');
            setEndDate('');
            setRegistrationDeadline('');
            setMaxPlayers(32);
            setMinPlayers(2);
            navigate('/admin');
        } catch (error) {
            setSnackbar({
                open: true,
                message: 'Error creating tournament: ' + (error.response?.data || error.message),
                severity: 'error'
            });
        }
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
                        Create Tournament
                    </Typography>
                    <form onSubmit={handleSubmit}>
                        <Grid container spacing={2}>
                            <Grid item xs={12}>
                                <TextField
                                    label="Tournament Name"
                                    variant="outlined"
                                    fullWidth
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                    required
                                />
                            </Grid>
                            <Grid item xs={12} sm={6}>
                                <TextField
                                    label="Start Date"
                                    type="date"
                                    variant="outlined"
                                    fullWidth
                                    InputLabelProps={{ shrink: true }}
                                    value={startDate}
                                    onChange={(e) => setStartDate(e.target.value)}
                                    required
                                />
                            </Grid>
                            <Grid item xs={12} sm={6}>
                                <TextField
                                    label="End Date"
                                    type="date"
                                    variant="outlined"
                                    fullWidth
                                    InputLabelProps={{ shrink: true }}
                                    value={endDate}
                                    onChange={(e) => setEndDate(e.target.value)}
                                    required
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <TextField
                                    label="Registration Deadline"
                                    type="date"
                                    variant="outlined"
                                    fullWidth
                                    InputLabelProps={{ shrink: true }}
                                    value={registrationDeadline}
                                    onChange={(e) => setRegistrationDeadline(e.target.value)}
                                    required
                                />
                            </Grid>
                            <Grid item xs={12} sm={6}>
                                <TextField
                                    label="Max Players"
                                    type="number"
                                    variant="outlined"
                                    fullWidth
                                    value={maxPlayers}
                                    onChange={(e) => setMaxPlayers(parseInt(e.target.value))}
                                    required
                                />
                            </Grid>
                            <Grid item xs={12} sm={6}>
                                <TextField
                                    label="Min Players"
                                    type="number"
                                    variant="outlined"
                                    fullWidth
                                    value={minPlayers}
                                    onChange={(e) => setMinPlayers(parseInt(e.target.value))}
                                    required
                                />
                            </Grid>
                        </Grid>
                        <Button variant="contained" color="primary" type="submit" fullWidth sx={{ mt: 2 }}>
                            Create Tournament
                        </Button>
                    </form>
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

export default CreateTournamentPage;
