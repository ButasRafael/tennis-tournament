// src/index.js
import 'bootstrap/dist/css/bootstrap.min.css';
import React, { useMemo, useState } from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import reportWebVitals from './reportWebVitals';
import * as Sentry from "@sentry/react";
import { ThemeProvider, CssBaseline } from '@mui/material';
import getTheme from './theme';

Sentry.init({
    dsn: "https://da6593296b24688cb5a6accabcbb4563@o4509111729520640.ingest.sentry.io/4509111748395088",
    integrations: [Sentry.browserTracingIntegration(), Sentry.replayIntegration()],
    tracesSampleRate: 1.0,
    replaysSessionSampleRate: 0.1,
    replaysOnErrorSampleRate: 1.0,
    environment: process.env.NODE_ENV,
    debug: process.env.NODE_ENV !== "production"
});

function Main() {
    const [mode, setMode] = useState('light');
    const theme = useMemo(() => getTheme(mode), [mode]);

    return (
        <ThemeProvider theme={theme}>
            <CssBaseline />
            <App mode={mode} setMode={setMode} />
        </ThemeProvider>
    );
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
    <React.StrictMode>
        <Main />
    </React.StrictMode>
);

reportWebVitals();
