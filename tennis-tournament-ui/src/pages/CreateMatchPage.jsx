// src/pages/CreateMatchPage.jsx
import React, { useState } from 'react';
import { Container, Card, CardContent, Typography, TextField, Button, Grid, Snackbar, Alert } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosConfig';

function CreateMatchPage() {
    const [tournamentId, setTournamentId] = useState('');
    const [player1Id, setPlayer1Id] = useState('');
    const [player2Id, setPlayer2Id] = useState('');
    const [refereeId, setRefereeId] = useState('');
    const [startTime, setStartTime] = useState('');
    const [endTime, setEndTime] = useState('');
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });
    const navigate = useNavigate();

    const storedUser = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;
    const currentUserId = storedUser ? storedUser.id : null;

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            let formattedStartTime = startTime.length === 16 ? startTime + ':00' : startTime;
            let formattedEndTime = endTime.length === 16 ? endTime + ':00' : endTime;
            await axiosInstance.post('/matches/create', null, {
                params: {
                    tournamentId,
                    player1Id,
                    player2Id,
                    refereeId,
                    startTime: formattedStartTime,
                    endTime: formattedEndTime,
                    currentUserId,
                },
            });
            setSnackbar({ open: true, message: 'Match created successfully!', severity: 'success' });
            setTournamentId('');
            setPlayer1Id('');
            setPlayer2Id('');
            setRefereeId('');
            setStartTime('');
            setEndTime('');
            navigate('/admin');
        } catch (error) {
            setSnackbar({
                open: true,
                message: 'Error creating match: ' + (error.response?.data || error.message),
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
                        Create Match
                    </Typography>
                    <form onSubmit={handleSubmit}>
                        <Grid container spacing={2}>
                            <Grid item xs={12}>
                                <TextField
                                    label="Tournament ID"
                                    type="number"
                                    variant="outlined"
                                    fullWidth
                                    value={tournamentId}
                                    onChange={(e) => setTournamentId(e.target.value)}
                                    required
                                />
                            </Grid>
                            <Grid item xs={12} sm={6}>
                                <TextField
                                    label="Player 1 ID"
                                    type="number"
                                    variant="outlined"
                                    fullWidth
                                    value={player1Id}
                                    onChange={(e) => setPlayer1Id(e.target.value)}
                                    required
                                />
                            </Grid>
                            <Grid item xs={12} sm={6}>
                                <TextField
                                    label="Player 2 ID"
                                    type="number"
                                    variant="outlined"
                                    fullWidth
                                    value={player2Id}
                                    onChange={(e) => setPlayer2Id(e.target.value)}
                                    required
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <TextField
                                    label="Referee ID"
                                    type="number"
                                    variant="outlined"
                                    fullWidth
                                    value={refereeId}
                                    onChange={(e) => setRefereeId(e.target.value)}
                                    required
                                />
                            </Grid>
                            <Grid item xs={12} sm={6}>
                                <TextField
                                    label="Start Time"
                                    type="datetime-local"
                                    variant="outlined"
                                    fullWidth
                                    InputLabelProps={{ shrink: true }}
                                    value={startTime}
                                    onChange={(e) => setStartTime(e.target.value)}
                                    required
                                />
                            </Grid>
                            <Grid item xs={12} sm={6}>
                                <TextField
                                    label="End Time"
                                    type="datetime-local"
                                    variant="outlined"
                                    fullWidth
                                    InputLabelProps={{ shrink: true }}
                                    value={endTime}
                                    onChange={(e) => setEndTime(e.target.value)}
                                    required
                                />
                            </Grid>
                        </Grid>
                        <Button variant="contained" color="primary" type="submit" fullWidth sx={{ mt: 2 }}>
                            Create Match
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

export default CreateMatchPage;
