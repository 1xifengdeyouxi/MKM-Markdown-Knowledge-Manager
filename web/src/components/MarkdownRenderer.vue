<template>
  <div class="markdown-body" v-html="rendered" />
</template>

<script setup>
import { computed } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

const props = defineProps({ content: { type: String, default: '' } })

const rendered = computed(() =>
  DOMPurify.sanitize(
    marked.parse(props.content, { gfm: true, breaks: true })
  )
)
</script>

<style>
.markdown-body {
  line-height: 1.7;
  font-size: 15px;
  word-wrap: break-word;
}
.markdown-body h1, .markdown-body h2, .markdown-body h3 {
  margin: 1em 0 0.5em;
}
.markdown-body code {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: monospace;
}
.markdown-body pre {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 6px;
  overflow-x: auto;
}
.markdown-body pre code {
  background: none;
  padding: 0;
}
.markdown-body blockquote {
  border-left: 4px solid #ccc;
  padding-left: 16px;
  color: #666;
  margin: 0;
}
.markdown-body table {
  border-collapse: collapse;
  width: 100%;
}
.markdown-body th, .markdown-body td {
  border: 1px solid #ddd;
  padding: 8px 12px;
}
.markdown-body img { max-width: 100%; }
</style>
