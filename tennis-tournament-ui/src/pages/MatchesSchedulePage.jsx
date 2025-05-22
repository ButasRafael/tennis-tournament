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
import VisibilityIcon from '@mui/icons-material/Visibility';
import axiosInstance from '../api/axiosConfig';

export default function MatchesSchedulePage() {
    const [tournaments, setTournaments] = useState([]);
    const [selectedTournament, setSelectedTournament] = useState('');
    const [matches, setMatches] = useState([]);
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'error' });
    const storedUser = JSON.parse(localStorage.getItem('user') || 'null');
    const userId = storedUser?.id;

    // new: pull down **only approved** tournaments for this player
    useEffect(() => {
        const stored = JSON.parse(localStorage.getItem('user') || 'null');
        const myId = stored?.id;
        if (!myId) return;

        axiosInstance.get('/tournaments/approved', { params: { playerId: myId } })
            .then(res => setTournaments(res.data))
            .catch(err => setSnackbar({
                open: true,
                message: 'Error fetching tournaments: ' + (err.response?.data || err.message),
                severity: 'error'
            }));
    }, []);


    const fetchMatches = () => {
        if (!selectedTournament) {
            setSnackbar({ open: true, message: 'Please select a tournament.', severity: 'error' });
            return;
        }

        axiosInstance.get(`/matches/tournament/${selectedTournament}`)
            .then(res => {
                // filter by your own player-id fields
                const mine = res.data.filter(m =>
                    m.player1Id === userId || m.player2Id === userId
                );
                setMatches(mine);
            })
            .catch(err =>
                setSnackbar({
                    open: true,
                    message: 'Error fetching matches: ' + (err.response?.data || err.message),
                    severity: 'error'
                })
            );
    };

    const handleCloseSnackbar = (_, reason) => {
        if (reason === 'clickaway') return;
        setSnackbar(s => ({ ...s, open: false }));
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
                            onChange={e => setSelectedTournament(e.target.value)}
                            label="Select Tournament"
                        >
                            <MenuItem value="">
                                <em>-- Select Tournament --</em>
                            </MenuItem>
                            {tournaments.map(t => (
                                <MenuItem key={t.id} value={t.id}>
                                    {t.name}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>

                    <Button
                        variant="contained"
                        color="primary"
                        startIcon={<VisibilityIcon />}
                        sx={{ mt: 2 }}
                        onClick={fetchMatches}
                    >
                        View Schedule
                    </Button>

                    {matches.length === 0 ? (
                        <Typography variant="body1" sx={{ mt: 2 }}>
                            No matches found.
                        </Typography>
                    ) : (
                        <TableContainer sx={{ mt: 2 }}>
                            <Table>
                                <TableHead>
                                    <TableRow>
                                        <TableCell>Match ID</TableCell>
                                        <TableCell>Tournament</TableCell>
                                        <TableCell>Player 1</TableCell>
                                        <TableCell>Player 2</TableCell>
                                        <TableCell>Score</TableCell>
                                        <TableCell>Start Time</TableCell>
                                        <TableCell>End Time</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {matches.map(m => (
                                        <TableRow key={m.id}>
                                            <TableCell>{m.id}</TableCell>
                                            <TableCell>{m.tournamentName}</TableCell>
                                            <TableCell>{m.player1Username}</TableCell>
                                            <TableCell>{m.player2Username}</TableCell>
                                            <TableCell>{m.score || 'N/A'}</TableCell>
                                            <TableCell>{m.startTime}</TableCell>
                                            <TableCell>{m.endTime}</TableCell>
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
