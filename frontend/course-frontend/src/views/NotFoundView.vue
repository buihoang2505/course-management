<template>
  <div class="nf-wrap">
    <div class="nf-box">
      <div class="nf-code">404</div>
      <h1 class="nf-title">Trang không tồn tại</h1>
      <p class="nf-sub">Đường dẫn bạn truy cập không tồn tại hoặc đã bị xóa.</p>
      <div class="nf-actions">
        <router-link to="/courses" class="btn btn-accent">← Về trang khóa học</router-link>
        <button class="btn btn-ghost" @click="$router.back()">Quay lại</button>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.nf-wrap {
  min-height: 70vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}
.nf-box {
  text-align: center;
  max-width: 420px;
}
.nf-code {
  font-size: 7rem;
  font-weight: 800;
  line-height: 1;
  background: linear-gradient(135deg, var(--accent), var(--purple));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: .5rem;
}
.nf-title {
  font-size: 1.6rem;
  font-weight: 700;
  margin: 0 0 .6rem;
  color: var(--text);
}
.nf-sub {
  font-size: .95rem;
  color: var(--muted);
  margin: 0 0 2rem;
  line-height: 1.6;
}
.nf-actions {
  display: flex;
  gap: .75rem;
  justify-content: center;
  flex-wrap: wrap;
}
</style>