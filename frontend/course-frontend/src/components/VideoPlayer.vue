<template>
  <div class="vp-wrap">

    <!-- YouTube embed -->
    <iframe
        v-if="isYoutube"
        :src="youtubeEmbedUrl"
        class="vp-iframe"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
        allowfullscreen
        loading="lazy"
    ></iframe>

    <!-- Cloudinary / direct video file — HTML5 player, browser tự handle range request -->
    <video
        v-else-if="isVideoFile"
        :src="url"
        class="vp-video"
        controls
        preload="metadata"
        controlsList="nodownload"
    >
      Trình duyệt của bạn không hỗ trợ thẻ video.
    </video>

    <!-- Fallback: link bình thường -->
    <a v-else :href="url" target="_blank" class="vp-link">
      <svg width="15" height="15" fill="none" stroke="currentColor"
           stroke-width="2" viewBox="0 0 24 24">
        <polygon points="23 7 16 12 23 17 23 7"/>
        <rect x="1" y="5" width="15" height="14" rx="2" ry="2"/>
      </svg>
      Xem video bài học
    </a>

  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  url: { type: String, required: true }
})

// Detect loại URL
const isYoutube = computed(() =>
    /youtube\.com|youtu\.be/.test(props.url)
)

const isVideoFile = computed(() =>
    /cloudinary\.com/.test(props.url) ||
    /\.(mp4|webm|mov|avi|mkv)(\?.*)?$/i.test(props.url)
)

// Chuyển URL YouTube thành embed URL
// Hỗ trợ: youtube.com/watch?v=xxx, youtu.be/xxx, youtube.com/shorts/xxx
const youtubeEmbedUrl = computed(() => {
  if (!isYoutube.value) return ''
  let videoId = ''
  const url = props.url

  if (url.includes('youtu.be/')) {
    videoId = url.split('youtu.be/')[1]?.split('?')[0]
  } else if (url.includes('youtube.com/shorts/')) {
    videoId = url.split('shorts/')[1]?.split('?')[0]
  } else {
    videoId = new URLSearchParams(new URL(url).search).get('v') || ''
  }

  return videoId
      ? `https://www.youtube.com/embed/${videoId}?rel=0&modestbranding=1`
      : ''
})
</script>

<style scoped>
.vp-wrap {
  width: 100%;
  border-radius: 10px;
  overflow: hidden;
  background: #000;
  aspect-ratio: 16 / 9;
  position: relative;
}

.vp-iframe,
.vp-video {
  width: 100%;
  height: 100%;
  border: none;
  display: block;
}

.vp-link {
  display: inline-flex; align-items: center; gap: .45rem;
  font-size: .83rem; font-weight: 700; color: var(--accent);
  background: var(--accent-light); border: 1px solid #bfdbfe;
  border-radius: 8px; padding: .45rem 1rem;
  text-decoration: none; transition: background .15s;
}
.vp-link:hover { background: #bfdbfe; }
</style>