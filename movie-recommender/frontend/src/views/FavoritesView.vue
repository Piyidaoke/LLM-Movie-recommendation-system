<template>
  <div class="favorites-page">
    <div class="header">
      <el-button @click="$router.push('/chat')">返回对话</el-button>
      <h2>我的收藏</h2>
    </div>
    <div class="movie-grid">
      <el-card v-for="movie in favorites" :key="movie.movieId" class="movie-card">
        <img v-if="movie.posterUrl" :src="movie.posterUrl" class="poster" />
        <div class="movie-info">
          <h3>{{ movie.title }}</h3>
          <p>评分: {{ movie.voteAverage }}</p>
          <p>{{ movie.releaseDate }}</p>
          <el-button size="small" type="danger" @click="handleRemove(movie.movieId)">
            取消收藏
          </el-button>
        </div>
      </el-card>
      <el-empty v-if="!favorites.length" description="暂无收藏" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getFavorites, removeFavorite } from '../api/favorite'

const favorites = ref([])

onMounted(async () => {
  const res = await getFavorites()
  favorites.value = res.data
})

async function handleRemove(movieId) {
  await removeFavorite(movieId)
  favorites.value = favorites.value.filter(m => m.movieId !== movieId)
  ElMessage.success('已取消收藏')
}
</script>

<style scoped>
.favorites-page { padding: 24px; }
.header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
.movie-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }
.movie-card { overflow: hidden; }
.poster { width: 100%; height: 300px; object-fit: cover; }
.movie-info { padding: 12px; }
</style>
