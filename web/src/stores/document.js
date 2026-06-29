import { defineStore } from 'pinia'
import { listDocuments, getDocument, createDocument, updateDocument } from '@/api/document'

export const useDocumentStore = defineStore('document', {
  state: () => ({
    documents: [],
    current: null,
    loading: false
  }),
  actions: {
    async fetchDocuments() {
      this.loading = true
      try {
        const { data } = await listDocuments()
        this.documents = data
      } finally {
        this.loading = false
      }
    },
    async fetchDocument(id) {
      this.loading = true
      try {
        const { data } = await getDocument(id)
        this.current = data
      } finally {
        this.loading = false
      }
    },
    async saveDocument(payload) {
      if (payload.id) {
        return updateDocument(payload.id, payload)
      }
      return createDocument(payload)
    }
  }
})
