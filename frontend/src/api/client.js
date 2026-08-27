import axios from 'axios'

const client = axios.create({
  baseURL: '/api',
})

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('campusos_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      const path = window.location.pathname
      if (!path.startsWith('/login') && !path.startsWith('/register') && path !== '/') {
        localStorage.removeItem('campusos_token')
        localStorage.removeItem('campusos_refresh')
        localStorage.removeItem('campusos_user')
        window.location.href = '/login'
      }
    }
    return Promise.reject(err)
  }
)

export default client
