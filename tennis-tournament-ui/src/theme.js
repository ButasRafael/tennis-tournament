// src/theme.js
import { createTheme } from '@mui/material/styles';

const getDesignTokens = (mode) => ({
    palette: {
        mode,
        ...(mode === 'light'
            ? {
                // Light mode colors
                primary: { main: '#2e7d32', contrastText: '#ffffff' },
                secondary: { main: '#ffeb3b', contrastText: '#000000' },
                background: { default: '#f4f6f8', paper: '#ffffff' },
                // Custom gradient for key sections
                customGradient: 'linear-gradient(45deg, #2e7d32, #a5d6a7)',
            }
            : {
                // Dark mode colors with improved contrast
                primary: { main: '#4caf50', contrastText: '#ffffff' },
                secondary: { main: '#ffc107', contrastText: '#000000' },
                background: { default: '#121212', paper: '#1d1d1d' },
                customGradient: 'linear-gradient(45deg, #4caf50, #81c784)',
            }),
    },
    typography: {
        fontFamily: '"Montserrat", "Roboto", "Helvetica", "Arial", sans-serif',
        h1: { fontWeight: 700, fontSize: '2.5rem', lineHeight: 1.2 },
        h2: { fontWeight: 700, fontSize: '2rem', lineHeight: 1.3 },
        h3: { fontWeight: 600, fontSize: '1.75rem', lineHeight: 1.4 },
        h4: { fontWeight: 600, fontSize: '1.5rem', lineHeight: 1.4 },
        h5: { fontWeight: 600, fontSize: '1.25rem', lineHeight: 1.4 },
        h6: { fontWeight: 500, fontSize: '1rem', lineHeight: 1.5 },
        body1: { fontSize: '1rem', lineHeight: 1.6 },
        body2: { fontSize: '0.875rem', lineHeight: 1.6 },
    },
    spacing: 8,
    components: {
        MuiCard: {
            styleOverrides: {
                root: {
                    borderRadius: 16,
                    boxShadow: '0 4px 20px rgba(0, 0, 0, 0.1)',
                    transition: 'box-shadow 0.3s ease-in-out, transform 0.3s ease',
                    '&:hover': {
                        boxShadow: '0 6px 30px rgba(0, 0, 0, 0.15)',
                        transform: 'scale(1.02)',
                    },
                },
            },
        },
        MuiButton: {
            styleOverrides: {
                root: {
                    borderRadius: 12,
                    textTransform: 'none',
                    boxShadow: 'none',
                    transition: 'transform 0.2s ease, background-color 0.3s ease, box-shadow 0.3s ease',
                    '&:hover': {
                        transform: 'scale(1.05)',
                        backgroundColor: 'rgba(46, 125, 50, 0.85)',
                        boxShadow: '0 4px 20px rgba(0, 0, 0, 0.15)',
                    },
                },
            },
        },
        MuiTextField: {
            styleOverrides: {
                root: {
                    transition: 'border-color 0.3s ease-in-out',
                    '& .MuiOutlinedInput-root': {
                        '&:hover fieldset': {
                            borderColor: '#2e7d32',
                        },
                        '&.Mui-focused fieldset': {
                            borderColor: '#2e7d32',
                        },
                    },
                },
            },
        },
        MuiLink: {
            styleOverrides: {
                root: {
                    transition: 'color 0.3s ease',
                    '&:hover': {
                        color: '#2e7d32',
                    },
                },
            },
        },
    },
});

const getTheme = (mode) => createTheme(getDesignTokens(mode));

export default getTheme;
