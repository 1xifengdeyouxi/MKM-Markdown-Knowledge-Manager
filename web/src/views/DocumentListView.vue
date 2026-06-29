<template>
  <div class="list-page">
    <nav>
      <span class="brand">MKM</span>
      <span class="user">{{ auth.username }}</span>
      <button class="btn-text" @click="logout">退出</button>
    </nav>
    <div class="toolbar">
      <h2>我的文档</h2>
      <button @click="$router.push('/documents/new')">新建</button>
    </div>
    <p v-if="store.loading" class="hint">加载中...</p>
    <p v-else-if="!store.documents.length" class="hint">暂无文档，点击新建创建第一个</p>
    <div v-else class="doc-list">
      <div
        v-for="doc in store.documents"
        :key="doc.id"
        class="doc-card"
        @click="$router.push(`/documents/${doc.id}`)"
      >
        <h3>{{ doc.title }}</h3>
        <p class="meta">{{ doc.ownerUsername }} · {{ formatDate(doc.updatedAt) }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useDocumentStore } from '@/stores/document'

const router = useRouter()
const auth = useAuthStore()
const store = useDocumentStore()

onMounted(() => store.fetchDocuments())

function logout() {
  auth.logout()
  router.push('/login')
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.list-page { max-width: 860px; margin: 0 auto; padding: 0 16px; }
nav { display: flex; align-items: center; gap: 12px; padding: 16px 0; border-bottom: 1px solid #eee; }
.brand { font-size: 20px; font-weight: bold; flex: 1; }
.btn-text { background: none; border: none; cursor: pointer; color: #3f51b5; }
.toolbar { display: flex; align-items: center; justify-content: space-between; margin: 16px 0; }
.doc-list { display: grid; gap: 12px; }
.doc-card {
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.doc-card:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
.doc-card h3 { margin: 0 0 4px; }
.meta { color: #777; font-size: 12px; margin: 0; }
.hint { color: #999; text-align: center; margin-top: 40px; }
</style>
