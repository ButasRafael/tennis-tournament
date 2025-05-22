// src/types/api.ts

/** A minimal user representation returned by every endpoint */
export interface UserDTO {
    id: number;
    username: string;
    email: string;
    role: 'PLAYER' | 'REFEREE' | 'ADMIN';
}

/** Tournament data returned by the API */
export interface TournamentDTO {
    id: number;
    name: string;
    startDate: string;             // ISO date, e.g. “2025-05-01”
    endDate: string;               // ISO date
    registrationDeadline: string;  // ISO date
    maxPlayers: number;
    minPlayers: number;
    cancelled: boolean;
}

/** Match data returned by the API as a flat DTO */
export interface TennisMatchDTO {
    id: number;
    tournamentId: number;
    tournamentName: string;
    player1Id: number;
    player1Username: string;
    player2Id: number;
    player2Username: string;
    refereeId: number;
    refereeUsername?: string;
    score?: string;
    startTime: string;  // ISO datetime, e.g. “2025-05-01T10:00:00”
    endTime?: string;   // ISO datetime
}

/** Registration‐Request DTO returned by the API */
export interface RegistrationRequestDTO {
    id: number;
    playerId: number;
    playerUsername: string;
    tournamentId: number;
    tournamentName: string;
    status: 'PENDING' | 'APPROVED' | 'DENIED';
    createdAt: string;  // ISO datetime
}

/** Authentication response when logging in or refreshing tokens */
export interface AuthResponseDTO {
    accessToken: string;
    refreshToken: string;
    user: UserDTO;
}
