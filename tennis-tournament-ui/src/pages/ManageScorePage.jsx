// src/pages/ManageScorePage.jsx
import React, { useState, useEffect } from 'react';
import {
    Container,
    Card,
    CardContent,
    Typography,
    TableContainer,
    Table,
    TableHead,
    TableRow,
    TableCell,
    TableBody,
    Button,
    Snackbar,
    Alert,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogContentText,
    DialogActions,
    TextField
} from '@mui/material';
import axiosInstance from '../api/axiosConfig';
import EditIcon from '@mui/icons-material/Edit';

function ManageScorePage() {
    const [matches, setMatches] = useState([]);
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });
    const [dialogOpen, setDialogOpen] = useState(false);
    const [selectedMatch, setSelectedMatch] = useState(null);
    const [newScore, setNewScore] = useState('');
    const storedUser = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;

    useEffect(() => {
        if (storedUser && storedUser.role.toUpperCase() === 'REFEREE') {
            fetchMatches();
        }
    }, [storedUser]);

    const fetchMatches = () => {
        axiosInstance.get(`/matches/referee/${storedUser.id}`)
            .then(response => setMatches(response.data))
            .catch(error =>
                setSnackbar({
                    open: true,
                    message: 'Error fetching matches: ' + (error.response?.data || error.message),
                    severity: 'error'
                })
            );
    };

    const handleUpdateClick = (match) => {
        setSelectedMatch(match);
        setNewScore(match.score || '');
        setDialogOpen(true);
    };

    const handleDialogClose = () => {
        setDialogOpen(false);
        setSelectedMatch(null);
        setNewScore('');
    };

    const handleScoreSave = () => {
        axiosInstance.put(`/matches/${selectedMatch.id}/score`, null, {
            params: { newScore, currentUserId: storedUser.id }
        })
            .then(() => {
                setSnackbar({ open: true, message: 'Score updated successfully!', severity: 'success' });
                fetchMatches();
                handleDialogClose();
            })
            .catch(error => {
                setSnackbar({
                    open: true,
                    message: 'Error updating score: ' + (error.response?.data || error.message),
                    severity: 'error'
                });
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
                        Manage Match Scores
                    </Typography>

                    {matches.length === 0 ? (
                        <Typography>No matches assigned to you.</Typography>
                    ) : (
                        <TableContainer>
                            <Table>
                                <TableHead>
                                    <TableRow>
                                        <TableCell>Match ID</TableCell>
                                        <TableCell>Tournament</TableCell>
                                        <TableCell>Player 1</TableCell>
                                        <TableCell>Player 2</TableCell>
                                        <TableCell>Current Score</TableCell>
                                        <TableCell>Actions</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {matches.map(match => (
                                        <TableRow key={match.id}>
                                            <TableCell>{match.id}</TableCell>
                                            <TableCell>{match.tournamentName || 'N/A'}</TableCell>
                                            <TableCell>{match.player1Username || 'N/A'}</TableCell>
                                            <TableCell>{match.player2Username || 'N/A'}</TableCell>
                                            <TableCell>{match.score || 'N/A'}</TableCell>
                                            <TableCell>
                                                <Button
                                                    variant="outlined"
                                                    onClick={() => handleUpdateClick(match)}
                                                    startIcon={<EditIcon />}
                                                >
                                                    Update Score
                                                </Button>
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </TableContainer>
                    )}
                </CardContent>
            </Card>

            <Dialog open={dialogOpen} onClose={handleDialogClose}>
                <DialogTitle>Update Score</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        Enter the new score (e.g., 6-4,3-6,7-5):
                    </DialogContentText>
                    <TextField
                        autoFocus
                        margin="dense"
                        label="New Score"
                        type="text"
                        fullWidth
                        variant="outlined"
                        value={newScore}
                        onChange={(e) => setNewScore(e.target.value)}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleDialogClose}>Cancel</Button>
                    <Button onClick={handleScoreSave} variant="contained" color="primary">
                        Save
                    </Button>
                </DialogActions>
            </Dialog>

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

export default ManageScorePage;
