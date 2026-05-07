<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import axios from 'axios'

const API_BASE = 'http://localhost:8080/api/news'

interface NewsArticle {
  id: number | null
  title: string
  summary: string
  source: string
  author: string
  publishTime: string
  createTime: string
}

interface PageResult {
  page: number
  size: number
  total: number
  totalPages: number
  elapsedMs: number
  records: NewsArticle[]
  pageDelta: number
  startArticle: NewsArticle | null
  lastArticle: NewsArticle | null
}

interface HistoryEntry {
  id: number
  mode: string
  page: number
  serverMs: number
  clientMs: number
  time: string
}

const mode = ref<'traditional' | 'hybrid'>('traditional')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const totalPages = ref(0)
const records = ref<NewsArticle[]>([])
const loading = ref(false)
const jumpPage = ref('')
const serverMs = ref(0)
const clientMs = ref(0)

const startArticle = ref<NewsArticle | null>(null)
const lastArticle = ref<NewsArticle | null>(null)

const history = reactive<HistoryEntry[]>([])
let historySeq = 0

const visiblePages = computed(() => {
  const pages: number[] = []
  const end = Math.min(page.value + 5, totalPages.value)
  for (let i = page.value; i <= end; i++) pages.push(i)
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
  if (offset < 500000) return '中层'
  if (offset < 3000000) return '深层'
  return '极深'
})

async function doFetch(params: Record<string, any>) {
  loading.value = true
  const t0 = performance.now()
  try {
    const url = mode.value === 'traditional' ? `${API_BASE}/page` : `${API_BASE}/mixpage`
    const res = await axios.get<PageResult>(url, { params })
    clientMs.value = Math.round(performance.now() - t0)
    const d = res.data
    serverMs.value = d.elapsedMs
    total.value = d.total
    totalPages.value = d.totalPages
    records.value = d.records
    if (d.pageDelta !== 0) {
      page.value = page.value + d.pageDelta
    } else if (d.page > 0) {
      page.value = d.page
    }
    startArticle.value = d.startArticle
    lastArticle.value = d.lastArticle
    addHistory()
  } finally {
    loading.value = false
  }
}

function addHistory() {
  historySeq++
  history.push({
    id: historySeq,
    mode: mode.value === 'traditional' ? '传统 LIMIT/OFFSET' : '混合 游标',
    page: page.value,
    serverMs: serverMs.value,
    clientMs: clientMs.value,
    time: new Date().toLocaleTimeString(),
  })
  while (history.length > 30) history.shift()
}

function fetchTraditional(p: number) {
  doFetch({ page: p, size: size.value })
}

function fetchHybridJump(p: number) {
  startArticle.value = null
  lastArticle.value = null
  doFetch({ page: p, size: size.value })
}

function fetchHybridNext() {
  const la = lastArticle.value
  if (!la || !la.id) { console.warn('fetchHybridNext: lastArticle 为空，无法翻页'); return }
  const sp = new URLSearchParams()
  sp.set('page', '0')
  sp.set('size', String(size.value))
  sp.set('lastArticle.id', String(la.id))
  sp.set('lastArticle.publishTime', la.publishTime)
  console.log('fetchHybridNext 请求参数:', sp.toString())
  doFetch(sp)
}

function fetchHybridPrev() {
  const sa = startArticle.value
  if (!sa || !sa.id) { console.warn('fetchHybridPrev: startArticle 为空，无法翻页'); return }
  const sp = new URLSearchParams()
  sp.set('page', '0')
  sp.set('size', String(size.value))
  sp.set('startArticle.id', String(sa.id))
  sp.set('startArticle.publishTime', sa.publishTime)
  console.log('fetchHybridPrev 请求参数:', sp.toString())
  doFetch(sp)
}

function goPage(p: number) {
  if (p < 1 || p > totalPages.value) return
  if (mode.value === 'traditional') {
    fetchTraditional(p)
    return
  }
  if (p === page.value + 1) {
    fetchHybridNext()
  } else if (p === page.value - 1) {
    fetchHybridPrev()
  } else {
    fetchHybridJump(p)
  }
}

function doJump() {
  const p = parseInt(jumpPage.value)
  if (isNaN(p) || p < 1 || p > totalPages.value) {
    alert(`请输入 1 ~ ${totalPages.value.toLocaleString()} 之间的页码`)
    return
  }
  jumpPage.value = ''
  if (mode.value === 'traditional') {
    fetchTraditional(p)
  } else {
    fetchHybridJump(p)
  }
}

function onModeChange() {
  page.value = 1
  startArticle.value = null
  lastArticle.value = null
  if (mode.value === 'traditional') fetchTraditional(1)
  else fetchHybridJump(1)
}

function onSizeChange() {
  page.value = 1
  startArticle.value = null
  lastArticle.value = null
  if (mode.value === 'traditional') fetchTraditional(1)
  else fetchHybridJump(1)
}

function clearHistory() {
  history.length = 0
  historySeq = 0
}

function msClass(ms: number) {
  if (ms < 100) return 'ms-fast'
  if (ms < 500) return 'ms-medium'
  return 'ms-slow'
}

onMounted(() => fetchTraditional(1))
</script>

<template>
  <div class="news-container">
    <h1>新闻列表 — 深分页性能演示（700 万条）</h1>

    <div class="top-bar">
      <div class="mode-switch">
        <span class="mode-label left" :class="{ dim: mode !== 'traditional' }">传统 LIMIT</span>
        <label class="toggle">
          <input
            type="checkbox"
            :checked="mode === 'hybrid'"
            @change="mode = ($event.target as HTMLInputElement).checked ? 'hybrid' : 'traditional'; onModeChange()"
          />
          <span class="slider"></span>
        </label>
        <span class="mode-label right" :class="{ dim: mode !== 'hybrid' }">混合游标</span>
      </div>
      <div class="size-selector">
        <label>每页：</label>
        <select v-model.number="size" @change="onSizeChange">
          <option :value="5">5 条</option>
          <option :value="10">10 条</option>
          <option :value="20">20 条</option>
          <option :value="50">50 条</option>
        </select>
      </div>
    </div>

    <p class="total-info">
      共 <strong>{{ total.toLocaleString() }}</strong> 条，
      <strong>{{ totalPages.toLocaleString() }}</strong> 页，
      当前第 <strong>{{ page }}</strong> 页
      <span class="offset-tag" :class="speedLevel">{{ offsetDesc }}分页</span>
    </p>

    <div class="perf-panel" :class="speedLevel">
      <div class="perf-item">
        <span class="perf-label">服务端查询</span>
        <span class="perf-value">{{ serverMs }} ms</span>
      </div>
      <div class="perf-divider"></div>
      <div class="perf-item">
        <span class="perf-label">客户端往返</span>
        <span class="perf-value">{{ clientMs }} ms</span>
      </div>
      <div class="perf-divider"></div>
      <div class="perf-item">
        <span class="perf-label">OFFSET 偏移</span>
        <span class="perf-value">{{ ((page - 1) * size).toLocaleString() }} 行</span>
      </div>
      <div class="perf-divider"></div>
      <div class="perf-item">
        <span class="perf-label">当前模式</span>
        <span class="perf-value mode-text">{{ mode === 'traditional' ? '传统' : '游标' }}</span>
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
        <tr v-for="(item, index) in records" :key="item.id ?? index">
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
      <button v-for="p in visiblePages" :key="p" :class="{ active: p === page }" @click="goPage(p)">{{ p }}</button>
      <button :disabled="page === totalPages" @click="goPage(page + 1)">下一页</button>
      <button :disabled="page === totalPages" @click="goPage(totalPages)">末页</button>
      <span class="jump-box">
        跳至
        <input v-model="jumpPage" type="number" :min="1" :max="totalPages" placeholder="页码" @keyup.enter="doJump" />
        页
        <button @click="doJump">GO</button>
      </span>
    </div>

    <div v-if="history.length > 0" class="history-section">
      <div class="history-header">
        <h2>耗时历史对比</h2>
        <button class="clear-btn" @click="clearHistory">清空</button>
      </div>
      <div class="history-table-wrap">
        <table class="history-table">
          <thead>
            <tr>
              <th>#</th><th>时间</th><th>模式</th><th>页码</th><th>服务端</th><th>客户端</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="h in [...history].reverse()" :key="h.id"
              :class="{ hybrid: h.mode.includes('游标'), traditional: h.mode.includes('传统') }">
              <td>{{ h.id }}</td>
              <td>{{ h.time }}</td>
              <td><span class="mode-badge" :class="h.mode.includes('游标') ? 'hybrid' : 'trad'">{{ h.mode }}</span></td>
              <td class="num">{{ h.page.toLocaleString() }}</td>
              <td class="num" :class="msClass(h.serverMs)">{{ h.serverMs }} ms</td>
              <td class="num">{{ h.clientMs }} ms</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.news-container { max-width: 1200px; margin: 0 auto; padding: 24px; font-family: 'Microsoft YaHei', sans-serif; }
h1 { text-align: center; margin-bottom: 16px; color: #333; }

.top-bar { display: flex; align-items: center; justify-content: center; gap: 40px; margin-bottom: 12px; }
.mode-switch { display: flex; align-items: center; gap: 10px; }
.mode-label { font-size: 14px; font-weight: 600; color: #333; transition: color 0.2s; }
.mode-label.dim { color: #bbb; }
.mode-label.left { color: #409eff; }
.mode-label.left.dim { color: #bbb; }
.mode-label.right { color: #67c23a; }
.mode-label.right.dim { color: #bbb; }

.toggle { position: relative; display: inline-block; width: 48px; height: 26px; }
.toggle input { opacity: 0; width: 0; height: 0; }
.slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background: #409eff; transition: 0.3s; border-radius: 26px; }
.slider::before { content: ""; position: absolute; height: 20px; width: 20px; left: 3px; bottom: 3px; background: white; transition: 0.3s; border-radius: 50%; }
.toggle input:checked + .slider { background: #67c23a; }
.toggle input:checked + .slider::before { transform: translateX(22px); }

.size-selector { color: #555; font-size: 14px; }
.size-selector select { margin-left: 6px; padding: 4px 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px; }

.total-info { text-align: center; color: #888; margin-bottom: 10px; }
.total-info strong { color: #333; }
.offset-tag { display: inline-block; margin-left: 8px; padding: 1px 8px; border-radius: 10px; font-size: 12px; font-weight: 600; }
.offset-tag.fast { background: #f0faf0; color: #52c41a; }
.offset-tag.medium { background: #fffbe6; color: #fa8c16; }
.offset-tag.slow { background: #fff1f0; color: #f5222d; }

.perf-panel { display: flex; align-items: center; justify-content: center; gap: 24px; padding: 14px 24px; border-radius: 8px; margin-bottom: 16px; transition: background 0.3s; }
.perf-panel.fast { background: #f0faf0; border: 1px solid #b7eb8f; }
.perf-panel.medium { background: #fffbe6; border: 1px solid #ffe58f; }
.perf-panel.slow { background: #fff1f0; border: 1px solid #ffa39e; }
.perf-item { display: flex; flex-direction: column; align-items: center; gap: 2px; }
.perf-label { font-size: 12px; color: #999; }
.perf-value { font-size: 22px; font-weight: 700; font-variant-numeric: tabular-nums; }
.perf-value.mode-text { font-size: 18px; }
.fast .perf-value { color: #52c41a; }
.medium .perf-value { color: #fa8c16; }
.slow .perf-value { color: #f5222d; }
.perf-divider { width: 1px; height: 32px; background: #d9d9d9; }

.loading { text-align: center; padding: 80px; font-size: 18px; color: #999; }
.news-table { width: 100%; border-collapse: collapse; table-layout: fixed; }
.news-table th, .news-table td { padding: 10px 12px; border-bottom: 1px solid #e8e8e8; text-align: left; font-size: 14px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.news-table th { background: #f5f5f5; font-weight: 600; color: #333; }
.news-table tbody tr:hover { background: #fafafa; }
.col-id { width: 60px; text-align: center; }
.col-title { width: 220px; }
.col-summary { width: 320px; }
.col-source { width: 100px; }
.col-author { width: 90px; }
.col-time { width: 160px; }
.title-cell { font-weight: 500; color: #222; }
.summary-cell { color: #666; }

.pagination { display: flex; align-items: center; justify-content: center; gap: 6px; margin-top: 24px; flex-wrap: wrap; }
.pagination button { padding: 6px 14px; border: 1px solid #d9d9d9; border-radius: 4px; background: #fff; cursor: pointer; font-size: 14px; color: #333; transition: all 0.2s; }
.pagination button:hover:not(:disabled) { border-color: #409eff; color: #409eff; }
.pagination button.active { background: #409eff; border-color: #409eff; color: #fff; }
.pagination button:disabled { cursor: not-allowed; opacity: 0.5; }
.jump-box { margin-left: 16px; font-size: 14px; color: #555; }
.jump-box input { width: 80px; padding: 5px 8px; border: 1px solid #d9d9d9; border-radius: 4px; text-align: center; font-size: 14px; margin: 0 4px; }
.jump-box input::-webkit-outer-spin-button, .jump-box input::-webkit-inner-spin-button { -webkit-appearance: none; }

.history-section { margin-top: 40px; }
.history-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.history-header h2 { font-size: 18px; color: #333; margin: 0; }
.clear-btn { padding: 4px 14px; border: 1px solid #d9d9d9; border-radius: 4px; background: #fff; cursor: pointer; font-size: 13px; color: #999; }
.clear-btn:hover { border-color: #f5222d; color: #f5222d; }
.history-table-wrap { max-height: 420px; overflow-y: auto; border: 1px solid #e8e8e8; border-radius: 6px; }
.history-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.history-table th { position: sticky; top: 0; background: #fafafa; padding: 8px 12px; text-align: left; font-weight: 600; border-bottom: 2px solid #e8e8e8; z-index: 1; }
.history-table td { padding: 7px 12px; border-bottom: 1px solid #f0f0f0; }
.history-table .num { text-align: right; font-variant-numeric: tabular-nums; }
.history-table tbody tr.traditional { background: #f6f9ff; }
.history-table tbody tr.hybrid { background: #f6fff0; }
.mode-badge { display: inline-block; padding: 1px 8px; border-radius: 10px; font-size: 12px; font-weight: 600; }
.mode-badge.trad { background: #e6f0ff; color: #409eff; }
.mode-badge.hybrid { background: #edfae6; color: #67c23a; }
.ms-fast { color: #52c41a; font-weight: 600; }
.ms-medium { color: #fa8c16; font-weight: 600; }
.ms-slow { color: #f5222d; font-weight: 600; }
</style>
