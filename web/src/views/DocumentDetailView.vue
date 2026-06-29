<template>
  <div class="detail-page">
    <div class="toolbar">
      <button class="btn-text" @click="$router.back()">← 返回</button>
      <div class="actions">
        <button v-if="!editing" @click="editing = true">编辑</button>
        <button v-if="editing" @click="save">保存</button>
        <button v-if="editing" class="btn-cancel" @click="cancelEdit">取消</button>
      </div>
    </div>

    <p v-if="store.loading" class="hint">加载中...</p>

    <template v-else-if="!editing && store.current">
      <h1>{{ store.current.title }}</h1>
      <MarkdownRenderer :content="store.current.content" />
    </template>

    <template v-else>
      <input v-model="form.title" class="title-input" placeholder="文档标题" />
      <textarea v-model="form.content" class="content-input" placeholder="Markdown 内容" />
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDocumentStore } from '@/stores/document'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'

const route = useRoute()
const router = useRouter()
const store = useDocumentStore()

const editing = ref(route.params.id === 'new')
const form = reactive({ title: '', content: '' })

onMounted(async () => {
  if (route.params.id !== 'new') {
    await store.fetchDocument(route.params.id)
    form.title = store.current?.title || ''
    form.content = store.current?.content || ''
  }
})

function cancelEdit() {
  if (route.params.id === 'new') { router.back(); return }
  form.title = store.current?.title || ''
  form.content = store.current?.content || ''
  editing.value = false
}

async function save() {
  const payload = { ...form, isPublic: false }
  if (route.params.id !== 'new') payload.id = route.params.id
  try {
    await store.saveDocument(payload)
    if (route.params.id === 'new') { router.replace('/'); return }
    await store.fetchDocument(route.params.id)
    editing.value = false
  } catch (e) {
    alert(e.response?.data?.error || '保存失败')
  }
}
</script>

<style scoped>
.detail-page { max-width: 860px; margin: 0 auto; padding: 16px; }
.toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; }
.btn-text { background: none; border: none; cursor: pointer; color: #3f51b5; font-size: 14px; }
.btn-cancel { margin-left: 8px; background: #9e9e9e; }
.title-input {
  display: block;
  width: 100%;
  font-size: 24px;
  font-weight: bold;
  border: none;
  border-bottom: 2px solid #3f51b5;
  margin-bottom: 16px;
  padding: 8px 0;
  outline: none;
  box-sizing: border-box;
}
.content-input {
  width: 100%;
  min-height: 480px;
  font-family: monospace;
  font-size: 14px;
  padding: 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  resize: vertical;
  box-sizing: border-box;
}
.hint { color: #999; text-align: center; margin-top: 40px; }
</style>
