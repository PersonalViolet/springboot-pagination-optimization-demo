<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'

interface NewsArticle {
  id: number
  title: string
  summary: string
  source: string
  author: string
  publishTime: string
}

interface PageResult {
  page: number
  size: number
  total: number
  totalPages: number
  elapsedMs: number
  records: NewsArticle[]
}

const page = ref(1)
const size = ref(10)
const total = ref(0)
const totalPages = ref(0)
const records = ref<NewsArticle[]>([])
const loading = ref(false)
const jumpPage = ref('')

const serverMs = ref(0)
const clientMs = ref(0)

const visiblePages = computed(() => {
  const pages: number[] = []
  const end = Math.min(page.value + 5, totalPages.value)
  for (let i = page.value; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

const speedLevel = computed(() => {
  if (serverMs.value < 100) return 'fast'
  if (serverMs.value < 500) return 'medium'
  return 'slow'
})

const offsetDesc = computed(() => {
  const offset = (page.value - 1) * size.value
  if (offset < 10000) return '浅层'
  if (offset < 100000) return '中层'
  if (offset < 1000000) return '深层'
  return '极深'
})

async function fetchData() {
  loading.value = true
  const t0 = performance.now()
  try {
    const res = await axios.get<PageResult>('http://localhost:8080/api/news/page', {
      params: { page: page.value, size: size.value },
    })
    clientMs.value = Math.round(performance.now() - t0)
    const data = res.data
    page.value = data.page
    total.value = data.total
    totalPages.value = data.totalPages
    serverMs.value = data.elapsedMs
    records.value = data.records
  } finally {
    loading.value = false
  }
}

function goPage(p: number) {
  if (p < 1 || p > totalPages.value) return
  page.value = p
}

function doJump() {
  const p = parseInt(jumpPage.value)
  if (isNaN(p) || p < 1 || p > totalPages.value) {
    alert(`请输入 1 ~ ${totalPages.value.toLocaleString()} 之间的页码`)
    return
  }
  page.value = p
  jumpPage.value = ''
}

function onSizeChange() {
  page.value = 1
}

watch([page, size], () => {
  fetchData()
})

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="news-container">
    <h1>新闻列表 — 深分页性能演示</h1>
    <p class="total-info">
      共 <strong>{{ total.toLocaleString() }}</strong> 条记录，
      <strong>{{ totalPages.toLocaleString() }}</strong> 页，
      当前第 <strong>{{ page }}</strong> 页（{{ offsetDesc }}分页）
    </p>

    <!-- 耗时面板 -->
    <div class="perf-panel" :class="speedLevel">
      <div class="perf-item">
        <span class="perf-label">服务端查询耗时</span>
        <span class="perf-value">{{ serverMs }} ms</span>
      </div>
      <div class="perf-divider"></div>
      <div class="perf-item">
        <span class="perf-label">客户端往返耗时</span>
        <span class="perf-value">{{ clientMs }} ms</span>
      </div>
      <div class="perf-divider"></div>
      <div class="perf-item">
        <span class="perf-label">OFFSET 偏移量</span>
        <span class="perf-value">{{ ((page - 1) * size).toLocaleString() }} 行</span>
      </div>
    </div>

    <div class="top-bar">
      <div class="size-selector">
        <label>每页显示：</label>
        <select v-model.number="size" @change="onSizeChange">
          <option :value="5">5 条</option>
          <option :value="10">10 条</option>
          <option :value="20">20 条</option>
          <option :value="50">50 条</option>
        </select>
      </div>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <table v-else class="news-table">
      <thead>
        <tr>
          <th class="col-id">#</th>
          <th class="col-title">标题</th>
          <th class="col-summary">摘要</th>
          <th class="col-source">来源</th>
          <th class="col-author">作者</th>
          <th class="col-time">发布时间</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(item, index) in records" :key="item.id">
          <td>{{ (page - 1) * size + index + 1 }}</td>
          <td class="title-cell">{{ item.title }}</td>
          <td class="summary-cell">{{ item.summary }}</td>
          <td>{{ item.source }}</td>
          <td>{{ item.author }}</td>
          <td>{{ item.publishTime?.replace('T', ' ') }}</td>
        </tr>
      </tbody>
    </table>

    <div v-if="totalPages > 0" class="pagination">
      <button :disabled="page === 1" @click="goPage(1)">首页</button>
      <button :disabled="page === 1" @click="goPage(page - 1)">上一页</button>

      <button
        v-for="p in visiblePages"
        :key="p"
        :class="{ active: p === page }"
        @click="goPage(p)"
      >
        {{ p }}
      </button>

      <button :disabled="page === totalPages" @click="goPage(page + 1)">下一页</button>
      <button :disabled="page === totalPages" @click="goPage(totalPages)">末页</button>

      <span class="jump-box">
        跳至
        <input
          v-model="jumpPage"
          type="number"
          :min="1"
          :max="totalPages"
          placeholder="页码"
          @keyup.enter="doJump"
        />
        页
        <button @click="doJump">GO</button>
      </span>
    </div>
  </div>
</template>

<style scoped>
.news-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  font-family: 'Microsoft YaHei', sans-serif;
}

h1 {
  text-align: center;
  margin-bottom: 4px;
  color: #333;
}

.total-info {
  text-align: center;
  color: #888;
  margin-bottom: 12px;
}

.total-info strong {
  color: #333;
}

/* 耗时面板 */
.perf-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  padding: 14px 24px;
  border-radius: 8px;
  margin-bottom: 16px;
  transition: background 0.3s;
}

.perf-panel.fast {
  background: #f0faf0;
  border: 1px solid #b7eb8f;
}

.perf-panel.medium {
  background: #fffbe6;
  border: 1px solid #ffe58f;
}

.perf-panel.slow {
  background: #fff1f0;
  border: 1px solid #ffa39e;
}

.perf-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.perf-label {
  font-size: 12px;
  color: #999;
}

.perf-value {
  font-size: 22px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.fast .perf-value { color: #52c41a; }
.medium .perf-value { color: #fa8c16; }
.slow .perf-value { color: #f5222d; }

.perf-divider {
  width: 1px;
  height: 32px;
  background: #d9d9d9;
}

/* 顶部工具栏 */
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.size-selector {
  color: #555;
  font-size: 14px;
}

.size-selector select {
  margin-left: 8px;
  padding: 4px 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
}

.loading {
  text-align: center;
  padding: 80px;
  font-size: 18px;
  color: #999;
}

.news-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.news-table th,
.news-table td {
  padding: 10px 12px;
  border-bottom: 1px solid #e8e8e8;
  text-align: left;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.news-table th {
  background: #f5f5f5;
  font-weight: 600;
  color: #333;
}

.news-table tbody tr:hover {
  background: #fafafa;
}

.col-id { width: 60px; text-align: center; }
.col-title { width: 220px; }
.col-summary { width: 320px; }
.col-source { width: 100px; }
.col-author { width: 90px; }
.col-time { width: 160px; }

.title-cell {
  font-weight: 500;
  color: #222;
}

.summary-cell {
  color: #666;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 24px;
  flex-wrap: wrap;
}

.pagination button {
  padding: 6px 14px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  transition: all 0.2s;
}

.pagination button:hover:not(:disabled) {
  border-color: #409eff;
  color: #409eff;
}

.pagination button.active {
  background: #409eff;
  border-color: #409eff;
  color: #fff;
}

.pagination button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.jump-box {
  margin-left: 16px;
  font-size: 14px;
  color: #555;
}

.jump-box input {
  width: 80px;
  padding: 5px 8px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  text-align: center;
  font-size: 14px;
  margin: 0 4px;
}

.jump-box input::-webkit-outer-spin-button,
.jump-box input::-webkit-inner-spin-button {
  -webkit-appearance: none;
}
</style>
