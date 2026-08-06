/**
 * Base URL of the backend API. Every service builds its endpoint URLs from
 * this constant instead of hardcoding "http://localhost:8080" separately,
 * so there's a single place to change when the backend moves (e.g. a real
 * production domain instead of localhost).
 */
export const API_BASE_URL = 'http://localhost:8080/api/v1';
