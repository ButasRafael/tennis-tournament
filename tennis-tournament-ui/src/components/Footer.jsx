// src/components/Footer.jsx
import React from 'react';
import { Box, Typography } from '@mui/material';

function Footer() {
    return (
        <Box component="footer" sx={{ py: 2, backgroundColor: 'primary.main', color: 'white', mt: 'auto' }}>
            <Typography variant="body2" align="center">
                © {new Date().getFullYear()} Tennis Tournaments. All rights reserved.
            </Typography>
        </Box>
    );
}

export default Footer;
