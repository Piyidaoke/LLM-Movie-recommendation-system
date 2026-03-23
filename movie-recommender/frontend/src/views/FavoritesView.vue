<template>
  <div class="fav-page">
    <div class="film-grain-bg"></div>

    <div class="fav-content">
      <div class="fav-header">
        <button class="back-btn" @click="$router.push('/chat')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/>
          </svg>
          返回放映厅
        </button>
        <h1>我的片库</h1>
        <span class="fav-badge" v-if="favorites.length">{{ favorites.length }} 部</span>
      </div>

      <div v-if="favorites.length" class="fav-grid">
        <div v-for="movie in favorites" :key="movie.movieId" class="fav-film">
          <div class="fav-frame">
            <div class="ff-holes top"><i/><i/><i/></div>
            <div class="ff-poster">
              <img v-if="movie.posterUrl" :src="movie.posterUrl" />
              <div v-else class="ff-empty">NO POSTER</div>
              <button class="ff-remove" @click="handleRemove(movie.movieId)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            </div>
            <div class="ff-holes bottom"><i/><i/><i/></div>
          </div>
          <div class="fav-info">
            <h3>{{ movie.title }}</h3>
            <div class="fav-detail">
              <span v-if="movie.voteAverage" class="fav-score">{{ movie.voteAverage }}</span>
              <span v-if="movie.genres" class="fav-genres">{{ movie.genres }}</span>
            </div>
            <div v-if="movie.releaseDate" class="fav-date">{{ movie.releaseDate }}</div>
          </div>
        </div>
      </div>

      <div v-else class="fav-empty">
        <div class="empty-reel">
          <div class="reel-outer"><div class="reel-inner"></div></div>
        </div>
        <h3>片库空空如也</h3>
        <p>在对话中发现好电影，点击收藏加入你的片库</p>
        <button class="goto-btn" @click="$router.push('/chat')">去发现电影</button>
      </div>
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
  ElMessage({ message: '已从片库移除', type: 'success', duration: 1500 })
}
</script>

<style scoped>
.fav-page {
  min-height: 100vh; background: var(--bg-void);
  position: relative;
}
.film-grain-bg {
  position: fixed; inset: 0; pointer-events: none; z-index: 0;
  opacity: 0.15; mix-blend-mode: overlay;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='g'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23g)' opacity='0.6'/%3E%3C/svg%3E");
  background-size: 100px;
}

.fav-content {
  position: relative; z-index: 1;
  max-width: 1100px; margin: 0 auto;
  padding: 36px 44px;
}

.fav-header {
  display: flex; align-items: center; gap: 16px;
  margin-bottom: 36px;
}
.back-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 16px; background: var(--bg-frame);
  border: 1px solid var(--border); color: var(--text-2);
  font-size: 13px; cursor: pointer; transition: all 0.2s;
  font-family: 'Instrument Sans', sans-serif;
}
.back-btn:hover { border-color: var(--film-gold); color: var(--film-gold); }
.fav-header h1 { flex: 1; font-size: 24px; font-weight: 700; }
.fav-badge {
  font-size: 12px; color: var(--film-gold);
  background: rgba(219,161,72,0.1);
  padding: 4px 10px; border-radius: 2px;
  font-weight: 600;
}

.fav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
  gap: 24px;
}

.fav-film { transition: transform 0.25s; }
.fav-film:hover { transform: translateY(-4px); }

.fav-frame {
  background: var(--bg-void); border: 1px solid var(--border-bright);
}
.ff-holes { display: flex; justify-content: space-around; padding: 4px 10px; }
.ff-holes i {
  display: block; width: 12px; height: 7px;
  border: 1px solid var(--sprocket); border-radius: 1px;
}
.ff-poster {
  position: relative; width: 100%; aspect-ratio: 2/3; overflow: hidden;
}
.ff-poster img { width: 100%; height: 100%; object-fit: cover; display: block; }
.ff-empty {
  width: 100%; height: 100%; display: flex;
  align-items: center; justify-content: center;
  background: var(--bg-frame); color: var(--text-3);
  font-size: 11px; letter-spacing: 1px;
}
.ff-remove {
  position: absolute; top: 6px; right: 6px;
  width: 28px; height: 28px; border-radius: 50%;
  background: rgba(0,0,0,0.6); border: none; color: #fff;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; opacity: 0; transition: all 0.2s;
}
.ff-remove:hover { background: #c44; }
.fav-film:hover .ff-remove { opacity: 1; }

.fav-info { padding: 10px 4px; }
.fav-info h3 {
  font-size: 13px; font-weight: 600; color: var(--text-1);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  margin-bottom: 4px;
}
.fav-detail { display: flex; align-items: center; gap: 6px; }
.fav-score {
  font-size: 11px; font-weight: 700; color: var(--film-gold);
  background: rgba(219,161,72,0.1); padding: 0 5px; border-radius: 2px;
}
.fav-genres { font-size: 10px; color: var(--text-3); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.fav-date { font-size: 10px; color: var(--text-3); margin-top: 2px; }

/* Empty State */
.fav-empty {
  display: flex; flex-direction: column;
  align-items: center; padding: 80px 0; color: var(--text-3);
}
.empty-reel { margin-bottom: 24px; }
.reel-outer {
  width: 72px; height: 72px; border-radius: 50%;
  border: 2px solid var(--border-bright);
  display: flex; align-items: center; justify-content: center;
  animation: reel-spin 8s linear infinite;
}
.reel-inner {
  width: 20px; height: 20px; border-radius: 50%;
  border: 2px solid var(--text-3);
}
@keyframes reel-spin { to { transform: rotate(360deg); } }
.fav-empty h3 { font-size: 18px; color: var(--text-2); margin-bottom: 6px; }
.fav-empty p { font-size: 13px; margin-bottom: 24px; }
.goto-btn {
  padding: 10px 28px;
  background: transparent; border: 1px solid var(--film-gold);
  color: var(--film-gold); font-size: 13px; font-weight: 600;
  cursor: pointer; transition: all 0.2s;
  font-family: 'Syne', sans-serif;
}
.goto-btn:hover { background: var(--film-gold); color: var(--bg-void); }
</style>
