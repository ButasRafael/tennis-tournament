// src/pages/AdminDashboard.jsx
import React from 'react';
import { Container, Card, CardContent, Typography, Grid, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import LogoutIcon from '@mui/icons-material/Logout';
import EditIcon from '@mui/icons-material/Edit';
import GroupIcon from '@mui/icons-material/Group';
import DownloadIcon from '@mui/icons-material/Download';
import AddCircleIcon from '@mui/icons-material/AddCircle';
import SportsTennisIcon from '@mui/icons-material/SportsTennis';

function AdminDashboard() {
    const navigate = useNavigate();

    const handleLogout = () => {
        localStorage.removeItem('user');
        navigate('/login');
    };
    const handleUpdateAccount = () => navigate('/update-account');
    const handleManageUsers = () => navigate('/manage-users');
    const handleExportMatches = () => navigate('/export-matches');
    const handleCreateTournament = () => navigate('/create-tournament');
    const handleCreateMatch = () => navigate('/create-match');

    return (
        <Container sx={{ mt: 4 }}>
            <Card sx={{ mb: 4 }}>
                <CardContent>
                    <Typography variant="h5" gutterBottom>
                        Admin Dashboard
                    </Typography>
                    <Typography variant="body1">
                        Welcome, Admin! Use the options below to manage the application.
                    </Typography>
                </CardContent>
            </Card>
            <Grid container spacing={2}>
                <Grid item xs={12} sm={6} md={4}>
                    <Button variant="contained" fullWidth onClick={handleLogout} startIcon={<LogoutIcon />}>
                        Logout
                    </Button>
                </Grid>
                <Grid item xs={12} sm={6} md={4}>
                    <Button variant="outlined" fullWidth onClick={handleUpdateAccount} startIcon={<EditIcon />}>
                        Update Account
                    </Button>
                </Grid>
                <Grid item xs={12} sm={6} md={4}>
                    <Button variant="outlined" fullWidth onClick={handleManageUsers} startIcon={<GroupIcon />}>
                        Manage Users
                    </Button>
                </Grid>
                <Grid item xs={12} sm={6} md={4}>
                    <Button variant="contained" color="secondary" fullWidth onClick={handleExportMatches} startIcon={<DownloadIcon />}>
                        Export Matches
                    </Button>
                </Grid>
                <Grid item xs={12} sm={6} md={4}>
                    <Button variant="contained" color="success" fullWidth onClick={handleCreateTournament} startIcon={<AddCircleIcon />}>
                        Create Tournament
                    </Button>
                </Grid>
                <Grid item xs={12} sm={6} md={4}>
                    <Button variant="contained" color="warning" fullWidth onClick={handleCreateMatch} startIcon={<SportsTennisIcon />}>
                        Create Match
                    </Button>
                </Grid>
                <Grid item xs={12} sm={6} md={4}>
                    <Button
                        variant="outlined"
                        fullWidth
                        startIcon={<GroupIcon />}
                        onClick={() => navigate('/admin/requests')}
                        >
                            Registration-Requests
                    </Button>
                </Grid>
            </Grid>
        </Container>
    );
}

export default AdminDashboard;
