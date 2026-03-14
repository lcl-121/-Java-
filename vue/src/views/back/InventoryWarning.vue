<script setup>
import { ref, onMounted } from 'vue'
import { Setting } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

// 库存预警阈值
const inventoryThreshold = ref(10)
const dialogVisible = ref(false)
const loading = ref(false)

// 加载当前阈值
const loadThreshold = () => {
  loading.value = true
  request.get('/inventory/threshold').then(res => {
    if (res.code === '200' && res.data !== null && res.data !== undefined) {
      inventoryThreshold.value = res.data
    }
  }).finally(() => {
    loading.value = false
  })
}

onMounted(() => {
  loadThreshold()
})

// 保存阈值
const saveThreshold = () => {
  if (inventoryThreshold.value < 0) {
    ElMessage.warning('阈值不能为负数')
    return
  }
  
  request.post('/inventory/threshold', inventoryThreshold.value, {
    headers: {
      'Content-Type': 'application/json'
    }
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('设置成功')
      dialogVisible.value = false
      loadThreshold()
    } else {
      ElMessage.error('设置失败：' + (res.msg || ''))
    }
  }).catch(err => {
    console.error('保存失败:', err)
    ElMessage.error('保存失败，请检查网络或联系管理员')
  })
}

// 打开设置对话框
const openSettings = () => {
  loadThreshold()
  dialogVisible.value = true
}
</script>

<template>
  <div class="inventory-settings">
    <el-card class="settings-card">
      <template #header>
        <div class="card-header">
          <span>库存预警设置</span>
          <el-button type="primary" :icon="Setting" @click="openSettings">配置阈值</el-button>
        </div>
      </template>
      
      <div class="settings-content">
        <div class="info-item">
          <el-icon class="info-icon"><Setting /></el-icon>
          <div class="info-text">
            <div class="info-title">当前库存预警阈值</div>
            <div class="info-value">{{ inventoryThreshold }}</div>
            <div class="info-desc">当商品库存低于此值时将显示预警提示</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 设置阈值对话框 -->
    <el-dialog v-model="dialogVisible" title="设置库存预警阈值" width="500px" center>
      <div class="dialog-content">
        <el-alert
          title="温馨提示"
          type="info"
          :closable="false"
          show-icon
          class="mb-4"
        >
          <p>当商品的库存数量低于设定的阈值时，商家在后台管理界面的商品列表中将看到该商品的库存预警提示。</p>
        </el-alert>
        
        <el-form label-width="120px">
          <el-form-item label="预警阈值">
            <el-input-number 
              v-model="inventoryThreshold" 
              :min="0" 
              :max="1000" 
              :step="1"
              placeholder="请输入阈值"
              style="width: 200px"
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveThreshold">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.inventory-settings {
  padding: 20px;
}

.settings-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.settings-content {
  padding: 10px 0;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 8px;
}

.info-icon {
  font-size: 40px;
  color: #409EFF;
}

.info-text {
  flex: 1;
}

.info-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.info-value {
  font-size: 32px;
  font-weight: bold;
  color: #E6A23C;
  margin-bottom: 5px;
}

.info-desc {
  font-size: 12px;
  color: #909399;
}

.dialog-content {
  padding: 10px 0;
}

.mb-4 {
  margin-bottom: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
