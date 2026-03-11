<template>
  <div class="vu-wrap">
    <!-- Toggle: URL thủ công | Upload file -->
    <div class="vu-tabs">
      <button type="button" :class="['vu-tab', {active: mode==='url'}]"
              @click="mode='url'">
        <svg width="13" height="13" fill="none" stroke="currentColor"
             stroke-width="2" viewBox="0 0 24 24">
          <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
          <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
        </svg>
        URL (YouTube / link)
      </button>
      <button type="button" :class="['vu-tab', {active: mode==='file'}]"
              @click="mode='file'">
        <svg width="13" height="13" fill="none" stroke="currentColor"
             stroke-width="2" viewBox="0 0 24 24">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
          <polyline points="17 8 12 3 7 8"/>
          <line x1="12" y1="3" x2="12" y2="15"/>
        </svg>
        Upload file
      </button>
    </div>

    <!-- Mode: URL thủ công -->
    <div v-if="mode==='url'" class="vu-body">
      <input
          :value="modelValue"
          @input="$emit('update:modelValue', $event.target.value)"
          type="url"
          placeholder="https://youtube.com/watch?v=... hoặc link trực tiếp"
          class="vu-input"
      />
      <!-- Preview nếu có URL -->
      <div v-if="modelValue" class="vu-preview">
        <VideoPlayer :url="modelValue" />
      </div>
    </div>

    <!-- Mode: Upload file -->
    <div v-if="mode==='file'" class="vu-body">
      <!-- Drop zone -->
      <label
          class="vu-dropzone"
          :class="{ 'drag-over': dragging, 'has-file': !!uploadedUrl }"
          @dragover.prevent="dragging=true"
          @dragleave="dragging=false"
          @drop.prevent="onDrop"
      >
        <input type="file" accept="video/mp4,video/webm,video/quicktime"
               style="display:none" @change="onFileChange"/>

        <!-- Trạng thái: chưa upload -->
        <template v-if="!uploadedUrl && !uploading">
          <div class="dz-icon">🎬</div>
          <div class="dz-text">
            Kéo thả video vào đây hoặc <span class="dz-link">chọn file</span>
          </div>
          <div class="dz-hint">MP4, WebM, MOV · Tối đa 200MB</div>
        </template>

        <!-- Trạng thái: đang upload -->
        <template v-else-if="uploading">
          <div class="dz-icon">⏳</div>
          <div class="dz-text">Đang upload <strong>{{ fileName }}</strong>...</div>
          <div class="progress-bar">
            <div class="progress-fill" :style="{width: progress + '%'}"></div>
          </div>
          <div class="dz-hint">{{ progress }}% · Video lớn có thể mất vài phút</div>
        </template>

        <!-- Trạng thái: upload thành công -->
        <template v-else-if="uploadedUrl">
          <div class="dz-icon">✅</div>
          <div class="dz-text">Upload thành công!</div>
          <div class="dz-hint">{{ fileName }}</div>
          <button type="button" class="dz-change" @click.prevent="resetUpload">
            Đổi video khác
          </button>
        </template>
      </label>

      <!-- Error message -->
      <div v-if="uploadError" class="vu-error">⚠️ {{ uploadError }}</div>

      <!-- Preview sau khi upload -->
      <div v-if="uploadedUrl" class="vu-preview">
        <VideoPlayer :url="uploadedUrl" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import api from '../services/api.js'
import VideoPlayer from './VideoPlayer.vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  lessonId:   { type: [Number, String], default: null }
})
const emit = defineEmits(['update:modelValue'])

const mode        = ref('url')
const dragging    = ref(false)
const uploading   = ref(false)
const uploadedUrl = ref('')
const uploadError = ref('')
const progress    = ref(0)
const fileName    = ref('')

// Nếu modelValue đã là Cloudinary URL → chuyển sang tab file + hiện preview
watch(() => props.modelValue, (v) => {
  if (v && v.includes('cloudinary')) {
    mode.value = 'file'
    uploadedUrl.value = v
  }
}, { immediate: true })

function onDrop(e) {
  dragging.value = false
  const file = e.dataTransfer.files?.[0]
  if (file) handleFile(file)
}

function onFileChange(e) {
  const file = e.target.files?.[0]
  if (file) handleFile(file)
  e.target.value = ''
}

async function handleFile(file) {
  // Validate
  const allowed = ['video/mp4','video/webm','video/quicktime','video/x-msvideo']
  if (!allowed.includes(file.type)) {
    uploadError.value = 'Chỉ hỗ trợ MP4, WebM, MOV!'
    return
  }
  if (file.size > 200 * 1024 * 1024) {
    uploadError.value = 'Video không được vượt quá 200MB!'
    return
  }
  if (!props.lessonId) {
    uploadError.value = 'Cần lưu bài học trước khi upload video!'
    return
  }

  uploadError.value = ''
  uploading.value   = true
  fileName.value    = file.name
  progress.value    = 0

  // Simulate progress (Cloudinary không trả real progress qua REST)
  const timer = setInterval(() => {
    if (progress.value < 85) progress.value += Math.random() * 8
  }, 600)

  try {
    const fd = new FormData()
    fd.append('file', file)
    const r = await api.post(
        `/upload/video?lessonId=${props.lessonId}`, fd
        // Không set header — api.js interceptor tự detect FormData
    )
    progress.value   = 100
    uploadedUrl.value = r.data.videoUrl
    emit('update:modelValue', r.data.videoUrl)
  } catch (err) {
    uploadError.value = err.response?.data?.error || 'Upload thất bại!'
  } finally {
    clearInterval(timer)
    uploading.value = false
  }
}

function resetUpload() {
  uploadedUrl.value = ''
  uploadError.value = ''
  progress.value    = 0
  fileName.value    = ''
  emit('update:modelValue', '')
}
</script>

<style scoped>
.vu-wrap { display: flex; flex-direction: column; gap: .6rem; }

/* Tabs */
.vu-tabs {
  display: flex; gap: .4rem;
  background: var(--surface2);
  border: 1.5px solid var(--border);
  border-radius: 10px; padding: 4px;
}
.vu-tab {
  flex: 1; display: flex; align-items: center; justify-content: center;
  gap: .4rem; padding: .45rem .7rem; border-radius: 7px;
  border: none; background: transparent;
  font-size: .78rem; font-weight: 600; color: var(--muted);
  cursor: pointer; transition: all .15s;
  font-family: 'Plus Jakarta Sans', sans-serif;
}
.vu-tab:hover { color: var(--text2); }
.vu-tab.active {
  background: var(--surface); color: var(--accent);
  box-shadow: 0 1px 4px rgba(0,0,0,.08);
}

/* Input URL */
.vu-input {
  width: 100%; padding: .65rem .9rem;
  background: var(--surface); border: 1.5px solid var(--border);
  border-radius: 9px; color: var(--text); font-size: .86rem;
  outline: none; font-family: 'Plus Jakarta Sans', sans-serif;
  transition: border-color .15s, box-shadow .15s; box-sizing: border-box;
}
.vu-input:focus { border-color: var(--accent); box-shadow: 0 0 0 3px var(--accent-light); }

/* Drop zone */
.vu-dropzone {
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; gap: .4rem;
  border: 2px dashed var(--border); border-radius: 12px;
  padding: 2rem 1rem; cursor: pointer;
  text-align: center; transition: all .2s;
  background: var(--surface2);
}
.vu-dropzone:hover, .vu-dropzone.drag-over {
  border-color: var(--accent); background: var(--accent-light);
}
.vu-dropzone.has-file { border-style: solid; border-color: var(--green); }

.dz-icon  { font-size: 2rem; line-height: 1; }
.dz-text  { font-size: .85rem; font-weight: 600; color: var(--text2); }
.dz-link  { color: var(--accent); text-decoration: underline; }
.dz-hint  { font-size: .74rem; color: var(--muted); }
.dz-change {
  margin-top: .4rem; font-size: .75rem; font-weight: 700;
  color: var(--accent); background: var(--accent-light);
  border: 1px solid #bfdbfe; border-radius: 7px;
  padding: .3rem .8rem; cursor: pointer;
  font-family: 'Plus Jakarta Sans', sans-serif;
}
.dz-change:hover { background: #bfdbfe; }

/* Progress */
.progress-bar {
  width: 100%; max-width: 260px; height: 6px;
  background: var(--border); border-radius: 100px; overflow: hidden;
  margin-top: .3rem;
}
.progress-fill {
  height: 100%; background: var(--accent);
  border-radius: 100px; transition: width .4s ease;
}

.vu-error {
  font-size: .78rem; color: var(--red); font-weight: 600;
  background: var(--red-light); border: 1px solid #fca5a5;
  border-radius: 8px; padding: .45rem .8rem;
}

/* Preview */
.vu-preview { margin-top: .4rem; }
.vu-body { display: flex; flex-direction: column; gap: .5rem; }
</style>