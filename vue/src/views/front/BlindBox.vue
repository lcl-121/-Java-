<script setup>
import { ref, onMounted } from 'vue'
import { ShoppingCart, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import { useRouter } from "vue-router"

const router = useRouter()

// 盲盒列表
const blindBoxList = ref([])
const selectedBox = ref(null)
const loading = ref(false)

// 抽取结果
const drawResult = ref(null)
const resultDialogVisible = ref(false)
const drawing = ref(false)

// 历史记录
const records = ref([])
const historyDialogVisible = ref(false)

// 加载盲盒列表
const loadBlindBoxes = () => {
  request.get('/blindbox/list').then(res => {
    if (res.code === '200') {
      blindBoxList.value = res.data || []
      if (blindBoxList.value.length > 0 && !selectedBox.value) {
        selectedBox.value = blindBoxList.value[0]
      }
    }
  })
}

// 选择盲盒
const selectBox = (box) => {
  selectedBox.value = box
}

// 抽取盲盒
const draw = () => {
  if (!selectedBox.value) {
    ElMessage.warning('请先选择一个盲盒')
    return
  }

  ElMessageBox.confirm(
    `本次抽取将花费 <span style="color: #f56c6c; font-weight: bold; font-size: 18px;">¥${selectedBox.value.price}</span>，确定要继续吗？`,
    '💰 支付确认',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确定支付',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    drawing.value = true
    request.post(`/blindbox/draw/${selectedBox.value.id}`).then(res => {
      if (res.code === '200') {
        drawResult.value = res.data.goods
        resultDialogVisible.value = true
        
        // 显示订单信息
        if (res.data.order) {
          console.log('订单已创建:', res.data.order)
        }
      } else {
        ElMessage.error(res.msg || '抽取失败')
      }
    }).catch(error => {
      ElMessage.error(error.response?.data?.msg || '抽取失败')
    }).finally(() => {
      drawing.value = false
    })
  }).catch(() => {
    // 用户取消
  })
}

// 查看历史记录
const viewHistory = () => {
  request.get('/blindbox/records', {
    params: {
      pageNum: 1,
      pageSize: 50
    }
  }).then(res => {
    if (res.code === '200') {
      records.value = res.data.records || []
      historyDialogVisible.value = true
    }
  })
}

// 跳转到订单页面
const goToOrders = () => {
  resultDialogVisible.value = false
  router.push('/front/orders')
}

onMounted(() => {
  loadBlindBoxes()
})
</script>

<template>
  <div class="blindbox-page">
    <!-- 顶部标题 -->
    <div class="page-header">
      <h1 class="page-title">🎁 盲盒活动</h1>
      <p class="page-desc">超值惊喜，等你来抽！</p>
    </div>

    <!-- 盲盒选择区 -->
    <div class="box-selection">
      <div 
        v-for="box in blindBoxList" 
        :key="box.id"
        class="box-card"
        :class="{ active: selectedBox && selectedBox.id === box.id }"
        @click="selectBox(box)"
      >
        <div class="box-icon">🎁</div>
        <h3 class="box-name">{{ box.name }}</h3>
        <p class="box-price">¥{{ box.price }}/次</p>
      </div>
    </div>

    <!-- 盲盒详情区 -->
    <div v-if="selectedBox" class="box-detail">
      <div class="detail-card">
        <div class="detail-header">
          <span style="font-size: 40px;">🎁</span>
          <h2 class="detail-title">{{ selectedBox.name }}</h2>
        </div>
        
        <div class="detail-content">
          <p class="detail-desc">{{ selectedBox.description }}</p>
          
          <div class="detail-info">
            <div class="info-item">
              <span class="info-label">活动时间</span>
              <span class="info-value">{{ selectedBox.startTime }} 至 {{ selectedBox.endTime }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">单次价格</span>
              <span class="info-value price-highlight">¥{{ selectedBox.price }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">参与次数</span>
              <span class="info-value">{{ selectedBox.usedCount || 0 }} / {{ selectedBox.totalCount || '∞' }}</span>
            </div>
          </div>
        </div>

        <div class="detail-actions">
          <el-button 
            type="primary" 
            size="large" 
            :loading="drawing"
            @click="draw"
            class="draw-btn"
          >
            <span style="font-size: 18px; margin-right: 5px;">🎁</span>
            立即抽取
          </el-button>
          <el-button size="large" @click="viewHistory">
            <el-icon><ShoppingCart /></el-icon>
            我的记录
          </el-button>
        </div>
      </div>
    </div>

    <!-- 抽取结果对话框 -->
    <el-dialog v-model="resultDialogVisible" title="🎉 恭喜获得" width="400px" center>
      <div v-if="drawResult" class="result-content">
        <div class="result-goods">
          <img :src="drawResult.goodsImg" alt="商品" class="goods-img">
          <div class="goods-info">
            <h3 class="goods-name">{{ drawResult.goodsName }}</h3>
            <p class="goods-price">价值 ¥{{ drawResult.goodsPrice }}</p>
          </div>
        </div>
        <div style="margin-top: 20px; padding: 15px; background: #f5f7fa; border-radius: 8px;">
          <p style="margin: 0; color: #666; font-size: 14px;">
            📦 请前往 <b>我的订单</b> 填写收货信息
          </p>
        </div>
      </div>
      <template #footer>
        <el-button @click="resultDialogVisible = false">开心收下</el-button>
        <el-button type="primary" @click="goToOrders">去填写收货信息</el-button>
      </template>
    </el-dialog>

    <!-- 历史记录对话框 -->
    <el-dialog v-model="historyDialogVisible" title="📜 抽取记录" width="700px">
      <el-table :data="records">
        <el-table-column prop="createTime" label="抽取时间" width="180" />
        <el-table-column label="获得商品">
          <template #default="scope">
            <div class="record-goods">
              <img :src="scope.row.goodsImg" class="record-img">
              <span>{{ scope.row.goodsName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '已完成' : '待支付' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<style scoped>
.blindbox-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-title {
  font-size: 36px;
  color: white;
  margin: 0;
  font-weight: bold;
}

.page-desc {
  color: rgba(255, 255, 255, 0.9);
  font-size: 16px;
  margin-top: 10px;
}

.box-selection {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.box-card {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
  min-width: 150px;
  text-align: center;
}

.box-card:hover {
  transform: translateY(-5px);
  background: rgba(255, 255, 255, 0.3);
}

.box-card.active {
  border-color: white;
  background: rgba(255, 255, 255, 0.3);
}

.box-icon {
  font-size: 40px;
  margin-bottom: 10px;
}

.box-name {
  color: white;
  font-size: 16px;
  margin: 10px 0;
}

.box-price {
  color: #ffd700;
  font-size: 18px;
  font-weight: bold;
}

.box-detail {
  max-width: 800px;
  margin: 0 auto;
}

.detail-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.detail-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30px;
  text-align: center;
}

.detail-title {
  color: white;
  font-size: 28px;
  margin: 15px 0 0 0;
}

.detail-content {
  padding: 30px;
}

.detail-desc {
  color: #666;
  line-height: 1.6;
  margin-bottom: 30px;
}

.detail-info {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.info-item {
  text-align: center;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 10px;
}

.info-label {
  display: block;
  color: #999;
  font-size: 14px;
  margin-bottom: 8px;
}

.info-value {
  display: block;
  color: #333;
  font-size: 16px;
  font-weight: bold;
}

.price-highlight {
  color: #f56c6c;
  font-size: 20px;
}

.detail-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.draw-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  padding: 15px 40px;
  font-size: 18px;
}

.result-content {
  text-align: center;
  padding: 20px;
}

.result-goods {
  display: flex;
  align-items: center;
  gap: 20px;
  justify-content: center;
}

.goods-img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 10px;
}

.goods-info {
  text-align: left;
}

.goods-name {
  font-size: 18px;
  margin: 0 0 10px 0;
}

.goods-price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
  margin: 0;
}

.record-goods {
  display: flex;
  align-items: center;
  gap: 10px;
}

.record-img {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 5px;
}
</style>
