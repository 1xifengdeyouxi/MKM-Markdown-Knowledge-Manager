import client from './client'

export function listDocuments() {
  return client.get('/documents')
}

export function getDocument(id) {
  return client.get(`/documents/${id}`)
}

export function createDocument(payload) {
  return client.post('/documents', payload)
}

export function updateDocument(id, payload) {
  return client.put(`/documents/${id}`, payload)
}

export function deleteDocument(id) {
  return client.delete(`/documents/${id}`)
}
