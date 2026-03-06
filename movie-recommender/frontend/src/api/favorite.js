import http from './http'
export const getFavorites = () => http.get('/favorites')
export const addFavorite = (movieId) => http.post(`/favorites?movieId=${movieId}`)
export const removeFavorite = (movieId) => http.delete(`/favorites/${movieId}`)
