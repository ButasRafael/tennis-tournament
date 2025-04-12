// src/pages/ExportMatchesPage.jsx
import React, { useState, useEffect } from 'react';
import { Container, Card, CardContent, Typography, FormControl, InputLabel, Select, MenuItem, Button, Grid, Snackbar, Alert } from '@mui/material';
import axiosInstance from '../api/axiosConfig';
import DownloadIcon from '@mui/icons-material/Download';

function ExportMatchesPage() {
    const [tournaments, setTournaments] = useState([]);
    const [selectedTournament, setSelectedTournament] = useState('');
    const [format, setFormat] = useState('csv');
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });
    const storedUser = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;

    useEffect(() => {
        axiosInstance.get('/tournaments/all')
            .then(response => setTournaments(response.data))
            .catch(error => {
                setSnackbar({ open: true, message: 'Error fetching tournaments: ' + (error.response?.data || error.message), severity: 'error' });
            });
    }, []);

    const handleExport = () => {
        if (!selectedTournament) {
            setSnackbar({ open: true, message: 'Please select a tournament.', severity: 'error' });
            return;
        }
        axiosInstance.get('/admin/export', {
            params: {
                format,
                tournamentId: selectedTournament,
                currentUserId: storedUser.id
            },
            responseType: 'blob'
        })
            .then(response => {
                const url = window.URL.createObjectURL(new Blob([response.data]));
                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', `matches.${format}`);
                document.body.appendChild(link);
                link.click();
            })
            .catch(error => {
                setSnackbar({ open: true, message: 'Export failed: ' + (error.response?.data || error.message), severity: 'error' });
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
                        Export Match Information
                    </Typography>
                    <Grid container spacing={2}>
                        <Grid item xs={12} sm={6}>
                            <FormControl fullWidth>
                                <InputLabel>Select Tournament</InputLabel>
                                <Select
                                    value={selectedTournament}
                                    onChange={(e) => setSelectedTournament(e.target.value)}
                                    label="Select Tournament"
                                >
                                    <MenuItem value="">
                                        <em>-- Select Tournament --</em>
                                    </MenuItem>
                                    {tournaments.map(t => (
                                        <MenuItem key={t.id} value={t.id}>{t.name}</MenuItem>
                                    ))}
                                </Select>
                            </FormControl>
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <FormControl fullWidth>
                                <InputLabel>Select Format</InputLabel>
                                <Select
                                    value={format}
                                    onChange={(e) => setFormat(e.target.value)}
                                    label="Select Format"
                                >
                                    <MenuItem value="csv">CSV</MenuItem>
                                    <MenuItem value="txt">TXT</MenuItem>
                                </Select>
                            </FormControl>
                        </Grid>
                    </Grid>
                    <Button variant="contained" color="primary" onClick={handleExport} startIcon={<DownloadIcon />} sx={{ mt: 2 }}>
                        Export Matches
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

export default ExportMatchesPage;
