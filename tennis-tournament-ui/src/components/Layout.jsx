// src/components/Layout.jsx
import React, { useEffect, useState } from 'react';
import { Container, Grid, Box, Fade } from '@mui/material';
import Header from './Header';
import Footer from './Footer';

function Layout({ children, mode, setMode }) {
    const [visible, setVisible] = useState(false);

    useEffect(() => {
        // Trigger fade-in animation when component mounts
        setVisible(true);
    }, []);

    return (
        <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
            <Header mode={mode} setMode={setMode} />
            <Fade in={visible} timeout={1000}>
                <Container sx={{ flex: 1, py: 4 }}>
                    <Grid container justifyContent="center">
                        {/* This Grid item centers the content and restricts its max width */}
                        <Grid item xs={12} md={8}>
                            {children}
                        </Grid>
                    </Grid>
                </Container>
            </Fade>
            <Footer />
        </Box>
    );
}

export default Layout;
