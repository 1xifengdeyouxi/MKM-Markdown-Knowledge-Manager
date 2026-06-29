<template>
  <div class="login-container">
    <h1>MKM</h1>
    <div class="form">
      <input v-model="username" placeholder="用户名" @keyup.enter="submit" />
      <input v-model="password" type="password" placeholder="密码" @keyup.enter="submit" />
      <p v-if="error" class="error">{{ error }}</p>
      <button :disabled="loading" @click="doLogin">登录</button>
      <button :disabled="loading" @click="doRegister">注册</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function doLogin() {
  error.value = ''
  loading.value = true
  try {
    await auth.login(username.value, password.value)
    router.push('/')
  } catch (e) {
    error.value = e.response?.data?.error || '登录失败'
  } finally {
    loading.value = false
  }
}

async function doRegister() {
  error.value = ''
  loading.value = true
  try {
    await auth.register(username.value, password.value)
    router.push('/')
  } catch (e) {
    error.value = e.response?.data?.error || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  max-width: 360px;
  margin: 100px auto;
  text-align: center;
}
.form {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 24px;
}
input {
  padding: 10px;
  font-size: 14px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
button {
  padding: 10px;
  font-size: 14px;
  cursor: pointer;
  border: none;
  border-radius: 4px;
  background: #3f51b5;
  color: #fff;
}
button:disabled { opacity: 0.6; cursor: not-allowed; }
.error { color: #d32f2f; font-size: 12px; }
</style>
