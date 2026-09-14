import axios from 'axios'

const client = axios.create({
  baseURL: '/api/v1',
})

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('wms_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('wms_token')
      localStorage.removeItem('wms_user')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export const authApi = {
  login: (username, password) => client.post('/auth/login', { username, password }),
}

export const warehouseApi = {
  getMap: () => client.get('/warehouse/map'),
  getLocation: (locationCode) => client.get(`/warehouse/location/${locationCode}`),
}

export const placementApi = {
  scanEngine: (engineBarcode) => client.post('/placement/scan-engine', { engineBarcode }),
  confirm: (engineBarcode, locationCode, operatorId) =>
    client.post('/placement/confirm', { engineBarcode, locationCode, operatorId }),
}

export const retrievalApi = {
  solveRelocation: (targetEngineNumber) =>
    client.post('/retrieval/solve-relocation', { targetEngineNumber }),
  uploadExcel: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return client.post('/retrieval/upload-excel', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}

export default client
