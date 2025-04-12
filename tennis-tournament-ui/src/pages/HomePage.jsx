// src/pages/HomePage.jsx
import React from 'react';
import { Container, Card, CardMedia, CardContent, Typography, Button } from '@mui/material';
import { Link } from 'react-router-dom';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import { motion } from 'framer-motion';

function HomePage() {
    return (
        <Container
            maxWidth="md"
            sx={{
                mt: 4,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center'
            }}
        >
            <Card sx={{ width: '100%' }}>
                <CardMedia
                    component="img"
                    height="300"
                    image="/images/tennis.jpg" // Use locally hosted image
                    alt="Tennis Match"
                />
                <CardContent>
                    <Typography variant="h4" align="center" gutterBottom>
                        Welcome to Tennis Tournaments
                    </Typography>
                    <Typography variant="body1" align="center" sx={{ mb: 2 }}>
                        Experience the thrill of tennis on our professional platform.
                        Register, manage matches, and follow your favorite players—all in one place.
                    </Typography>
                    <motion.div
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                        style={{ display: 'flex', justifyContent: 'center' }} // Centers the button
                    >
                        <Button
                            variant="contained"
                            color="primary"
                            component={Link}
                            to="/login"
                            startIcon={<ArrowForwardIcon />}
                            size="large"
                            sx={{
                                px: 4,            // Increased horizontal padding
                                py: 2,            // Increased vertical padding
                                fontSize: '1.2rem', // Larger text size
                                borderRadius: 8,
                                mt: 2,            // Top margin for spacing
                            }}
                        >
                            Get Started
                        </Button>
                    </motion.div>
                </CardContent>
            </Card>
        </Container>
    );
}

export default HomePage;
