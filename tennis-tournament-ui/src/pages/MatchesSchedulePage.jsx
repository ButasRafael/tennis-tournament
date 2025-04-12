// src/pages/MatchesSchedulePage.jsx
import React, { useState, useEffect } from 'react';
import {
    Container,
    Card,
    CardContent,
    Typography,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    Button,
    TableContainer,
    Table,
    TableHead,
    TableRow,
    TableCell,
    TableBody,
    Snackbar,
    Alert
} from '@mui/material';
import axiosInstance from '../api/axiosConfig';
import VisibilityIcon from '@mui/icons-material/Visibility';

function MatchesSchedulePage() {
    const [tournaments, setTournaments] = useState([]);
    const [selectedTournament, setSelectedTournament] = useState('');
    const [matches, setMatches] = useState([]);
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'error' });
    const storedUser = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;

    useEffect(() => {
        axiosInstance.get('/tournaments/all')
            .then(response => setTournaments(response.data))
            .catch(error => {
                setSnackbar({ open: true, message: 'Error fetching tournaments: ' + (error.response?.data || error.message), severity: 'error' });
            });
    }, []);

    const fetchMatches = () => {
        if (!selectedTournament) {
            setSnackbar({ open: true, message: 'Please select a tournament.', severity: 'error' });
            return;
        }
        axiosInstance.get(`/matches/tournament/${selectedTournament}`)
            .then(response => {
                const playerMatches = response.data.filter(match =>
                    (match.player1 && match.player1.id === storedUser.id) ||
                    (match.player2 && match.player2.id === storedUser.id)
                );
                setMatches(playerMatches);
            })
            .catch(error => {
                setSnackbar({ open: true, message: 'Error fetching matches: ' + (error.response?.data || error.message), severity: 'error' });
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
                        Matches Schedule & Scores
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
                    <Button variant="contained" color="primary" onClick={fetchMatches} startIcon={<VisibilityIcon />} sx={{ mt: 2 }}>
                        View Schedule
                    </Button>
                    {matches.length === 0 ? (
                        <Typography variant="body1" sx={{ mt: 2 }}>No matches found.</Typography>
                    ) : (
                        <TableContainer sx={{ mt: 2 }}>
                            <Table>
                                <TableHead>
                                    <TableRow>
                                        <TableCell>Match ID</TableCell>
                                        <TableCell>Player 1</TableCell>
                                        <TableCell>Player 2</TableCell>
                                        <TableCell>Score</TableCell>
                                        <TableCell>Start Time</TableCell>
                                        <TableCell>End Time</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {matches.map(match => (
                                        <TableRow key={match.id}>
                                            <TableCell>{match.id}</TableCell>
                                            <TableCell>{match.player1 ? match.player1.username : 'N/A'}</TableCell>
                                            <TableCell>{match.player2 ? match.player2.username : 'N/A'}</TableCell>
                                            <TableCell>{match.score || 'N/A'}</TableCell>
                                            <TableCell>{match.startTime}</TableCell>
                                            <TableCell>{match.endTime}</TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </TableContainer>
                    )}
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

export default MatchesSchedulePage;
