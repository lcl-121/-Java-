<script setup>
import { ref, computed, onMounted } from 'vue'
import { ArrowRight, ShoppingCart, StarFilled, Bell, Location, Picture } from '@element-plus/icons-vue'
import request from '../../utils/request'

import {useRouter} from "vue-router";
const router = useRouter()

// State
const types = ref([])
const category = ref([])
const banners = ref([])
const notices = ref([])
const recommend = ref([])

// 背景主题相关
const bgThemes = ref([
  { id: 0, name: '默认', url: '' },
  { id: 1, name: '咒术回战', url: new URL('../../assets/bg1.jpg', import.meta.url).href },
  { id: 2, name: '动漫 2', url: new URL('../../assets/bg2.jpg', import.meta.url).href },
  { id: 3, name: '动漫 3', url: new URL('../../assets/bg3.jpg', import.meta.url).href },
  { id: 4, name: '动漫 4', url: new URL('../../assets/bg4.jpg', import.meta.url).href },
  { id: 5, name: '动漫 5', url: new URL('../../assets/bg5.jpg', import.meta.url).href }
])
const currentBgIndex = ref(parseInt(localStorage.getItem('homeBgTheme') || '0'))
const showBgSelector = ref(false)

// 切换背景主题
const changeBgTheme = (index) => {
  currentBgIndex.value = index
  localStorage.setItem('homeBgTheme', index.toString())
}

// 用户信息
const account = ref(localStorage.getItem('account') ? JSON.parse(localStorage.getItem('account')) : {})

const isGuest = computed(() => !account.value.id)

const load = () => {
  request.get("/goods/recommend").then(res => {
    recommend.value = res.data
  })
}

const loadType = () => {
  request.get("/type/front").then(res => {
    types.value = res.data
  })
}

const loadCategory = () => {
  request.get("/type/hot").then(res => {
    category.value = res.data || []
  })
}

const loadNotices = () => {
  request.get("/notice").then(res => {
    notices.value = res.data
  })
}

const loadBanner = () => {
  request.get("/banner").then(res => {
    banners.value = res.data
  })
}




onMounted(() => {
  load()
  loadType()
  loadBanner()
  loadCategory()
  loadNotices()
})
</script>


<template>
  <div class="homepage" :style="{ backgroundImage: currentBgIndex > 0 ? `url(${bgThemes[currentBgIndex].url})` : 'none', backgroundSize: 'cover', backgroundPosition: 'center', backgroundAttachment: 'fixed' }">
    <!-- 背景主题切换按钮 -->
    <div class="bg-theme-toggle">
      <el-button 
        circle 
        :icon="Picture" 
        @click="showBgSelector = !showBgSelector"
        title="切换背景"
      />
    </div>
    
    <!-- 背景主题选择器 -->
    <transition name="fade">
      <div v-if="showBgSelector" class="bg-theme-selector">
        <div class="selector-header">
          <span>选择背景主题</span>
          <el-icon @click="showBgSelector = false"><Close /></el-icon>
        </div>
        <div class="selector-content">
          <div 
            v-for="theme in bgThemes" 
            :key="theme.id" 
            class="theme-item"
            :class="{ active: currentBgIndex === theme.id }"
            @click="changeBgTheme(theme.id)"
          >
            <div class="theme-name">{{ theme.name }}</div>
          </div>
        </div>
      </div>
    </transition>
    <!-- 公告轮播 -->
    <div class="notice-section">
      <div class="notice-content">
        <el-icon class="notice-icon"><Bell /></el-icon>
        <el-carousel height="30px" direction="vertical" :autoplay="true" :interval="3000">
          <el-carousel-item v-for="item in notices" :key="item.id">
            <span class="notice-text">{{ item.name }}</span>
          </el-carousel-item>
        </el-carousel>
      </div>
    </div>

    <!-- 顶部三栏布局 -->
    <div class="top-section">
      <!-- 左侧分类 -->
      <div class="category-sidebar">
        <ul class="category-list" >
          <li v-for="type in types" :key="type.id" @click="router.push('/front/goods?typeId=' + type.id)">
            <span class="category-text">{{ type.name }}</span>
            <el-icon><ArrowRight /></el-icon>
          </li>
          <li @click="$router.push('/front/goods?typeId=')">
            <span class="category-text">更多分类</span>
            <el-icon><ArrowRight /></el-icon>
          </li>
        </ul>
      </div>

      <!-- 中间轮播图 -->
      <div class="carousel-section">
        <el-carousel height="380px" class="main-carousel">
          <el-carousel-item v-for="banner in banners" :key="banner.id">
            <div class="banner-wrapper">
              <img :src="banner.img" class="banner-image">
              <div class="banner-content">
                <div class="banner-title">{{banner.name}}</div>
                <el-button type="primary" size="small" class="banner-btn" @click="router.push('/front/goodsDetail?id='+banner.goodsId)">
                  立即查看
                </el-button>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>

      <!-- 右侧个人信息栏 -->
      <div class="user-sidebar">
        <div class="user-info">
          <div class="user-avatar">
            <img v-if="!isGuest" :src="account.avatarUrl" alt="用户头像" class="avatar-img">
            <img v-else src="../../assets/用户头像.svg" alt="用户头像" class="avatar-img">
          </div>
          <div class="username">{{ isGuest ? '游客' : account.nickname }}</div>
        </div>

        <el-button type="primary" size="small" class="login-btn" @click="isGuest ? router.push('/login') : router.push('/front/orders')">
          {{ isGuest ? '立即登录' : '查看订单' }}
        </el-button>

        <div class="user-tools">
          <div class="tool-item" @click="router.push('/front/cart')">
            <el-icon><ShoppingCart /></el-icon>
            <div class="tool-name">购物车</div>
          </div>
          <div class="tool-item" @click="router.push('/front/collect')">
            <el-icon><StarFilled /></el-icon>
            <div class="tool-name">点赞</div>
          </div>
          <div class="tool-item" @click="router.push('/front/address')">
            <el-icon><Location /></el-icon>
            <div class="tool-name">地址</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 猜你喜欢推荐区域 -->
    <div class="recommend-section">
      <div class="section-header">
        <img src="../../assets/喜欢.png" alt="喜欢" class="section-icon">
        <h3 class="section-title">猜你喜欢</h3>
      </div>
      <div class="product-showcase">
        <div v-for="product in recommend" :key="product.id" class="product-card"
             @click="router.push('/front/goodsDetail?id=' + product.id)">
          <img :src="product.img" class="recommend-image">
          <div class="recommend-info">
            <h3 class="recommend-name">{{ product.name }}</h3>
            <div class="like-count">
              <el-icon><StarFilled /></el-icon>
              {{ product.likeCount || 0 }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分类商品展示区域 -->
    <div class="category-goods-section">
      <div v-for="item in category" :key="item.id" class="category-block">
        <div class="category-layout">
          <div class="category-info">
            <div class="category-header-row">
              <h3 class="category-name">{{ item.name }}</h3>
              <span class="view-more" @click="router.push('/front/goods?typeId=' + item.id)">
                查看更多 →
              </span>
            </div>
            <div class="category-image-container">
              <img :src="item.img" class="category-main-image">
            </div>
          </div>

          <div class="category-goods">
            <div class="goods-grid">
              <div v-for="goods in item.goodsList" :key="goods.id" class="goods-item"
                   @click="router.push('/front/goodsDetail?id=' + goods.id)">
                <img :src="goods.img" class="goods-image">
                <div class="goods-name">{{ goods.name }}</div>
                <div class="like-count">
                  <el-icon><StarFilled /></el-icon>
                  {{ goods.likeCount || 0 }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- 页脚 -->
    <footer class="front-footer">
      <p>© {{ new Date().getFullYear() }} {{ projectName }}. 保留所有权利</p>
    </footer>
  </div>
</template>



<style scoped>
.homepage {
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  width: 1400px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.notice-section {
  background-color: rgba(255, 255, 255, 0.85);
  padding: 10px;
  border-radius: 8px;
  margin-bottom: 15px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.medium {
  margin: 0;
  color: #666;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.medium .el-icon {
  margin-right: 5px;
  color: #ff4400;
}

/* 顶部三栏布局 */
.top-section {
  display: flex;
  gap: 10px;
  margin-bottom: 25px;
  margin-top: 15px;
}

/* 左侧分类样式 */
.category-sidebar {
  width: 200px;
  background-color: rgba(60, 60, 70, 0.92);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
}

.category-header {
  padding: 15px;
  background: linear-gradient(135deg, #444 0%, #2a2a2a 100%);
  display: flex;
  align-items: center;
}

.category-title {
  font-weight: bold;
  color: white;
  font-size: 16px;
}

.category-list {
  list-style-type: none;
  padding: 0;
  margin: 0;
}

.category-list li {
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  color: white;
  font-size: 16px;
  transition: background-color 0.2s;
}

.category-list li:hover {
  background-color: #ec6938;
}

/* 中间轮播图样式 */
.carousel-section {
  flex: 1;
  position: relative;
  border-radius: 4px;
}

.main-carousel {
  border-radius: 4px;
}

.banner-wrapper {
  position: relative;
  height: 100%;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-content {
  position: absolute;
  bottom: 10%;
  left: 50px;
  color: white;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.banner-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
  color: white;
}

.banner-desc {
  font-size: 16px;
  margin-bottom: 20px;
  color: white;
}

.banner-btn {
  background-color: #409EFF;
  border-color: #409EFF;
  cursor: pointer;
}

/* 右侧个人信息栏样式 */
.user-sidebar {
  width: 260px;
  background-color: rgba(255, 255, 255, 0.95);
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  align-items: center;
  text-align: center;
  transition: all 0.3s ease;
}

.user-sidebar:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 20px;
}

.user-avatar {
  margin-bottom: 10px;
}

.avatar-img {
  width: 50px;
  height: 50px;
  border-radius: 50%;
}

.user-details {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.username {
  font-weight: bold;
  font-size: 16px;
  color: #333;
  margin-bottom: 5px;
}

.user-actions {
  font-size: 12px;
  color: #666;
}

.register, .login {
  cursor: pointer;
  color: #409EFF;
}

.divider {
  margin: 0 5px;
  color: #ddd;
}

.login-prompt {
  font-size: 14px;
  color: #333;
  line-height: 1.4;
  text-align: center;
  width: 100%;
}

.login-desc {
  font-size: 12px;
  color: #999;
}

.login-btn {
  background-color: #409EFF;
  border-color: #409EFF;
  width: 80%;
}

.user-tools {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-around;
  width: 100%;
  margin-top: 10px;
}

.tool-item {
  width: 33.33%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 0;
  cursor: pointer;
}

.tool-item .el-icon {
  font-size: 24px;
  color: #666;
  margin-bottom: 5px;
}

.tool-name {
  font-size: 12px;
  color: #666;
}

/* 推荐区域 */
.recommend-section {
  background-color: rgba(255, 255, 255, 0.9);
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 25px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.section-icon {
  width: 20px;
  height: 20px;
  margin-right: 10px;
}

.section-title {
  margin: 0;
  font-size: 1.5em;
  font-weight: bold;
  color: #333;
  letter-spacing: 1px;
}

.product-showcase {
  display: flex;
  justify-content: space-between;
  gap: 15px;
}

.product-card {
  flex: 1;
  background-color: rgba(250, 250, 252, 0.95);
  padding: 15px;
  display: flex;
  align-items: center;
  cursor: pointer;
  border-radius: 10px;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid rgba(200, 200, 210, 0.3);
}

.product-card:hover {
  transform: translateY(-2px);
}

.recommend-image {
  width: 100px;
  max-height: 100px;
  border-radius: 10px;
  margin-right: 15px;
  object-fit: cover;
}

.recommend-info {
  flex: 1;
}

.recommend-name {
  margin: 0;
  font-size: 1.2em;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
}

.like-count {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #ff4757;
  font-size: 14px;
  margin-top: 8px;
}

/* 分类商品区域 */
.category-goods-section {
  background-color: rgba(255, 255, 255, 0.9);
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.category-block {
  margin-bottom: 40px;
}

.category-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.category-info {
  width: 360px;
  flex-shrink: 0;
}

.category-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.category-name {
  margin: 0;
  font-size: 1.8em;
  font-weight: bold;
  color: #333;
}

.view-more {
  color: #409EFF;
  cursor: pointer;
  font-size: 14px;
}

.view-more:hover {
  text-decoration: underline;
}

.category-image-container {
  width: 100%;
}

.category-main-image {
  width: 100%;
  height: 220px;
  object-fit: cover;
  border-radius: 8px;
}

.category-goods {
  flex: 1;
  margin-top: 0;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.goods-item {
  border: 1px solid rgba(204, 204, 204, 0.3);
  padding-bottom: 10px;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s;
  background-color: rgba(255, 255, 255, 0.0);
}

.goods-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.15);
}

.goods-image {
  width: 100%;
  height: 240px;
  object-fit: cover;
  border-radius: 10px 10px 0 0;
}

.goods-name {
  color: #000;
  margin: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
}
</style>