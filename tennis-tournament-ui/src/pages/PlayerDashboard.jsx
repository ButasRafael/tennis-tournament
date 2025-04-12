// src/pages/PlayerDashboard.jsx
import React from 'react';
import { Container, Card, CardContent, Typography, Grid, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import LogoutIcon from '@mui/icons-material/Logout';
import EditIcon from '@mui/icons-material/Edit';
import HowToRegIcon from '@mui/icons-material/HowToReg';
import ScheduleIcon from '@mui/icons-material/Schedule';

function PlayerDashboard() {
    const navigate = useNavigate();

    const handleLogout = () => {
        localStorage.removeItem('user');
        navigate('/login');
    };

    const handleUpdateAccount = () => navigate('/update-account');
    const handleTournamentRegistration = () => navigate('/tournament-registration');
    const handleViewMatchesSchedule = () => navigate('/matches-schedule');

    return (
        <Container sx={{ mt: 4 }}>
            <Card>
                <CardContent>
                    <Typography variant="h5" align="center" gutterBottom>
                        Player Dashboard
                    </Typography>
                    <Grid container spacing={2} sx={{ mt: 2 }}>
                        <Grid item xs={12} sm={6}>
                            <Button variant="contained" color="primary" fullWidth onClick={handleLogout} startIcon={<LogoutIcon />}>
                                Logout
                            </Button>
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <Button variant="outlined" fullWidth onClick={handleUpdateAccount} startIcon={<EditIcon />}>
                                Update Account
                            </Button>
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <Button variant="contained" color="success" fullWidth onClick={handleTournamentRegistration} startIcon={<HowToRegIcon />}>
                                Register at Tournament
                            </Button>
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <Button variant="outlined" color="info" fullWidth onClick={handleViewMatchesSchedule} startIcon={<ScheduleIcon />}>
                                View Schedule
                            </Button>
                        </Grid>
                    </Grid>
                </CardContent>
            </Card>
        </Container>
    );
}

export default PlayerDashboard;
