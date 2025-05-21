// src/pages/RefereeDashboard.jsx
import React from 'react';
import { Container, Card, CardContent, Typography, Grid, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import LogoutIcon from '@mui/icons-material/Logout';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import VisibilityIcon from '@mui/icons-material/Visibility';
import EditIcon from '@mui/icons-material/Edit';
import SearchIcon from '@mui/icons-material/Search';

function RefereeDashboard() {
    const navigate = useNavigate();

    const handleLogout = () => {
        localStorage.removeItem('user');
        navigate('/login');
    };
    const handleUpdateAccount = () => navigate('/update-account');
    const handleViewOwnProgram = () => navigate('/own-program');
    const handleManageScore = () => navigate('/manage-score');

    return (
        <Container sx={{ mt: 4 }}>
            <Card>
                <CardContent>
                    <Typography variant="h5" align="center" gutterBottom>
                        Referee Dashboard
                    </Typography>
                    <Grid container spacing={2} sx={{ mt: 2 }}>
                        <Grid item xs={12} sm={6}>
                            <Button variant="contained" fullWidth onClick={handleLogout} startIcon={<LogoutIcon />}>
                                Logout
                            </Button>
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <Button variant="outlined" fullWidth onClick={handleUpdateAccount} startIcon={<AccountCircleIcon />}>
                                Update Account
                            </Button>
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <Button variant="contained" color="primary" fullWidth onClick={handleViewOwnProgram} startIcon={<VisibilityIcon />}>
                                View Program
                            </Button>
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <Button variant="outlined" color="secondary" fullWidth onClick={handleManageScore} startIcon={<EditIcon />}>
                                Manage Score
                            </Button>
                        </Grid>
                            <Grid
                                item
                                xs={12} sm={6}
                                sx={{ mt: 2, mx: 'auto' }}
                            >
                                <Button
                                    variant="outlined"
                                    fullWidth
                                    onClick={() => navigate('/referee/filter-players')}
                                    startIcon={<SearchIcon />}
                                    >
                                      Filter Players
                                </Button>
                    </Grid>
                </Grid>
                </CardContent>
            </Card>
        </Container>
    );
}

export default RefereeDashboard;
