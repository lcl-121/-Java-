<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  theme: {
    type: String,
    default: 'theme1' // 默认主题
  }
})

// 当前激活的背景类型
const bgType = ref('star') // star, sakura, bubble, light

// 背景粒子数组
const particles = ref([])

// 创建粒子
const createParticle = () => {
  const id = Math.random().toString(36).substr(2, 9)
  const particle = {
    id,
    left: Math.random() * 100,
    size: Math.random() * 20 + 10,
    duration: Math.random() * 3 + 2,
    delay: Math.random() * 5
  }
  particles.value.push(particle)
  
  // 保持粒子数量在合理范围
  if (particles.value.length > 50) {
    particles.value.shift()
  }
}

// 初始化粒子
const initParticles = () => {
  particles.value = []
  for (let i = 0; i < 30; i++) {
    createParticle()
  }
}

// 根据主题设置背景类型
const setBackgroundByTheme = () => {
  const themeBgMap = {
    'theme1': 'star',      // 星空
    'theme2': 'sakura',    // 樱花
    'theme3': 'bubble',    // 气泡
    'theme4': 'light',     // 光束
    'theme5': 'star',
    'theme6': 'sakura',
    'theme7': 'bubble',
    'theme8': 'light'
  }
  bgType.value = themeBgMap[props.theme] || 'star'
}

onMounted(() => {
  initParticles()
  setBackgroundByTheme()
  
  // 定期创建新粒子
  const interval = setInterval(createParticle, 500)
  
  onUnmounted(() => {
    clearInterval(interval)
  })
})
</script>

<template>
  <div class="dynamic-background" :class="[`bg-${bgType}`]">
    <!-- 星空背景 -->
    <div v-if="bgType === 'star'" class="stars">
      <div 
        v-for="particle in particles" 
        :key="particle.id"
        class="star"
        :style="{
          left: particle.left + '%',
          width: particle.size + 'px',
          height: particle.size + 'px',
          animationDuration: particle.duration + 's',
          animationDelay: particle.delay + 's'
        }"
      ></div>
    </div>
    
    <!-- 樱花背景 -->
    <div v-else-if="bgType === 'sakura'" class="sakuras">
      <div 
        v-for="particle in particles" 
        :key="particle.id"
        class="sakura"
        :style="{
          left: particle.left + '%',
          width: particle.size + 'px',
          height: particle.size + 'px',
          animationDuration: particle.duration + 's',
          animationDelay: particle.delay + 's'
        }"
      >🌸</div>
    </div>
    
    <!-- 气泡背景 -->
    <div v-else-if="bgType === 'bubble'" class="bubbles">
      <div 
        v-for="particle in particles" 
        :key="particle.id"
        class="bubble"
        :style="{
          left: particle.left + '%',
          width: particle.size + 'px',
          height: particle.size + 'px',
          animationDuration: particle.duration + 's',
          animationDelay: particle.delay + 's'
        }"
      ></div>
    </div>
    
    <!-- 光束背景 -->
    <div v-else-if="bgType === 'light'" class="lights">
      <div 
        v-for="particle in particles" 
        :key="particle.id"
        class="light"
        :style="{
          left: particle.left + '%',
          width: particle.size + 'px',
          height: particle.size * 3 + 'px',
          animationDuration: particle.duration + 's',
          animationDelay: particle.delay + 's'
        }"
      ></div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.dynamic-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: -1;
  overflow: hidden;
}

// 星空背景
.bg-star {
  background: linear-gradient(to bottom, #0f0c29, #302b63, #24243e);
  
  .stars {
    width: 100%;
    height: 100%;
    
    .star {
      position: absolute;
      background: white;
      border-radius: 50%;
      opacity: 0.8;
      animation: twinkle infinite ease-in-out;
      box-shadow: 0 0 10px rgba(255, 255, 255, 0.8);
    }
  }
}

// 樱花背景
.bg-sakura {
  background: linear-gradient(to bottom, #ffecd2, #fcb69f);
  
  .sakuras {
    width: 100%;
    height: 100%;
    
    .sakura {
      position: absolute;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: inherit;
      opacity: 0.6;
      animation: fall infinite linear;
    }
  }
}

// 气泡背景
.bg-bubble {
  background: linear-gradient(to bottom, #a1c4fd, #c2e9fb);
  
  .bubbles {
    width: 100%;
    height: 100%;
    
    .bubble {
      position: absolute;
      background: rgba(255, 255, 255, 0.3);
      border-radius: 50%;
      border: 1px solid rgba(255, 255, 255, 0.5);
      animation: rise infinite ease-in;
    }
  }
}

// 光束背景
.bg-light {
  background: linear-gradient(to bottom, #667eea, #764ba2);
  
  .lights {
    width: 100%;
    height: 100%;
    
    .light {
      position: absolute;
      background: linear-gradient(to bottom, rgba(255,255,255,0), rgba(255,255,255,0.8), rgba(255,255,255,0));
      transform: rotate(180deg);
      animation: beam infinite linear;
    }
  }
}

@keyframes twinkle {
  0%, 100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}

@keyframes fall {
  0% {
    top: -10%;
    transform: translateX(0) rotate(0deg);
  }
  100% {
    top: 110%;
    transform: translateX(100px) rotate(360deg);
  }
}

@keyframes rise {
  0% {
    bottom: -10%;
    transform: translateX(0);
  }
  100% {
    bottom: 110%;
    transform: translateX(-50px);
  }
}

@keyframes beam {
  0% {
    top: -100%;
    opacity: 0;
  }
  50% {
    opacity: 1;
  }
  100% {
    top: 200%;
    opacity: 0;
  }
}
</style>
