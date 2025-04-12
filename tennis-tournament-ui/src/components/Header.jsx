// src/components/Header.jsx
import React from 'react';
import { AppBar, Toolbar, Typography, Button, IconButton } from '@mui/material';
import { Link, useNavigate } from 'react-router-dom';

// Import Material Icons
import SportsTennisIcon from '@mui/icons-material/SportsTennis';
import HomeIcon from '@mui/icons-material/Home';
import LoginIcon from '@mui/icons-material/Login';
import HowToRegIcon from '@mui/icons-material/HowToReg';
import LogoutIcon from '@mui/icons-material/Logout';
import DashboardIcon from '@mui/icons-material/Dashboard';
import Brightness4Icon from '@mui/icons-material/Brightness4';
import Brightness7Icon from '@mui/icons-material/Brightness7';

function Header({ mode, setMode }) {
    const user = localStorage.getItem('user')
        ? JSON.parse(localStorage.getItem('user'))
        : null;
    const navigate = useNavigate();

    const handleLogout = () => {
        localStorage.removeItem('user');
        navigate('/login');
    };

    const toggleDarkMode = () => {
        setMode(mode === 'light' ? 'dark' : 'light');
    };

    return (
        <AppBar position="static" color="primary">
            <Toolbar>
                <Typography variant="h6" component="div" sx={{ flexGrow: 1, display: 'flex', alignItems: 'center' }}>
                    <SportsTennisIcon sx={{ mr: 1 }} />
                    Tennis Tournaments
                </Typography>
                <Button color="inherit" startIcon={<HomeIcon />} component={Link} to="/">
                    Home
                </Button>
                {user ? (
                    <>
                        <Button color="inherit" startIcon={<LogoutIcon />} onClick={handleLogout}>
                            Logout
                        </Button>
                        <Button
                            color="inherit"
                            startIcon={<DashboardIcon />}
                            component={Link}
                            to={
                                user?.role?.toUpperCase() === 'PLAYER'
                                    ? '/player'
                                    : user?.role?.toUpperCase() === 'ADMIN'
                                        ? '/admin'
                                        : '/referee'
                            }
                        >
                            Dashboard
                        </Button>

                    </>
                ) : (
                    <>
                        <Button color="inherit" startIcon={<LoginIcon />} component={Link} to="/login">
                            Login
                        </Button>
                        <Button color="inherit" startIcon={<HowToRegIcon />} component={Link} to="/register">
                            Register
                        </Button>
                    </>
                )}
                <IconButton color="inherit" onClick={toggleDarkMode}>
                    {mode === 'light' ? <Brightness4Icon /> : <Brightness7Icon />}
                </IconButton>
            </Toolbar>
        </AppBar>
    );
}

export default Header;
