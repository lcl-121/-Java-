<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus, Delete, Edit, View, Setting } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

// 表格数据
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

// 搜索条件
const searchForm = reactive({
  keyword: ''
})

// 表单数据
const form = ref({})
const dialogVisible = ref(false)

// 商品配置对话框
const goodsDialogVisible = ref(false)
const currentBoxId = ref(null)
const goodsList = ref([])
const allGoods = ref([])

// 统计数据对话框
const statsDialogVisible = ref(false)
const currentStats = ref({})

// 加载数据
const load = () => {
  request.get('/blindbox/page', {
    params: {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchForm.keyword
    }
  }).then(res => {
    if (res.data) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  })
}
load()

// 保存
const save = () => {
  // 检查必填字段
  if (!form.value.name) {
    ElMessage.warning('请输入盲盒名称')
    return
  }
  if (!form.value.startTime || !form.value.endTime) {
    ElMessage.warning('请选择开始和结束时间')
    return
  }
  
  // 准备提交的数据
  const submitData = {
    ...form.value,
    // 确保价格转换为数字
    price: parseFloat(form.value.price),
    // 确保状态是整数
    status: parseInt(form.value.status),
    totalCount: parseInt(form.value.totalCount || 0),
    usedCount: parseInt(form.value.usedCount || 0)
  }
  
  console.log('保存数据:', submitData)
  
  request.post('/blindbox', submitData).then(res => {
    console.log('返回结果:', res)
    if (res.code === '200') {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      load()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  }).catch(error => {
    console.error('保存失败:', error)
    console.error('错误详情:', error.response?.data)
    ElMessage.error(error.response?.data?.msg || '网络错误：' + (error.message || '保存失败'))
  })
}

// 添加
const handleAdd = () => {
  form.value = {
    status: 1,
    totalCount: 0,
    usedCount: 0,
    price: 9.90
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  form.value = JSON.parse(JSON.stringify(row))
  dialogVisible.value = true
}

// 删除
const del = (id) => {
  request.delete('/blindbox/' + id).then(res => {
    if (res.code === '200') {
      ElMessage.success('删除成功')
      load()
    } else {
      ElMessage.error('删除失败')
    }
  })
}

// 查看商品配置
const viewGoods = (row) => {
  currentBoxId.value = row.id
  loadGoods(row.id)
  goodsDialogVisible.value = true
}

// 加载商品列表
const loadGoods = (boxId) => {
  request.get('/blindbox/items/' + boxId).then(res => {
    goodsList.value = res.data || []
  })
  
  // 加载所有商品
  request.get('/goods').then(res => {
    allGoods.value = res.data || []
  })
}

// 添加商品到盲盒
const addGoods = () => {
  goodsList.value.push({
    boxId: currentBoxId.value,
    goodsId: null,
    weight: 1,
    stock: 0,
    sort: goodsList.value.length
  })
}

// 删除商品
const deleteGoods = (index) => {
  const item = goodsList.value[index]
  if (item.id) {
    request.delete('/blindbox/item/' + item.id).then(res => {
      if (res.code === '200') {
        ElMessage.success('删除成功')
        loadGoods(currentBoxId.value)
      }
    })
  } else {
    goodsList.value.splice(index, 1)
  }
}

// 保存商品配置
const saveGoods = () => {
  const items = goodsList.value.filter(item => item.goodsId != null)
  request.post('/blindbox/items/batch', items).then(res => {
    if (res.code === '200') {
      ElMessage.success('保存成功')
      goodsDialogVisible.value = false
    } else {
      ElMessage.error('保存失败')
    }
  })
}

// 查看统计数据
const viewStats = (row) => {
  request.get('/blindbox/stats/' + row.id).then(res => {
    if (res.code === '200') {
      currentStats.value = res.data
      statsDialogVisible.value = true
    }
  })
}

// 更新状态
const updateStatus = (row, status) => {
  request.put(`/blindbox/${row.id}/status?status=${status}`).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      load()
    } else {
      ElMessage.error('操作失败')
    }
  })
}

// 重置搜索
const reset = () => {
  searchForm.keyword = ''
  load()
}

// 分页
const handleSizeChange = (size) => {
  pageSize.value = size
  load()
}

const handleCurrentChange = (current) => {
  pageNum.value = current
  load()
}

// 获取商品名称
const getGoodsName = (goodsId) => {
  const goods = allGoods.value.find(g => g.id === goodsId)
  return goods ? goods.name : '未知商品'
}
</script>

<template>
  <div class="content-container">
    <!-- 搜索区域 -->
    <div class="header-section">
      <el-input v-model="searchForm.keyword" placeholder="请输入盲盒名称" class="filter-input" :prefix-icon="Search" clearable />
      <el-button class="ml-10" plain type="primary" @click="load">搜索</el-button>
      <el-button plain type="info" @click="reset">重置</el-button>
    </div>

    <!-- 操作按钮 -->
    <div class="toolbar-section">
      <el-button plain type="primary" @click="handleAdd" :icon="Plus">新增盲盒</el-button>
    </div>

    <!-- 表格 -->
    <el-card>
      <el-table :data="tableData">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="盲盒名称" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="price" label="单次价格" width="100">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column label="时间" width="350">
          <template #default="scope">
            <div>{{ scope.row.startTime }}</div>
            <div style="color: #999; font-size: 12px;">至 {{ scope.row.endTime }}</div>
          </template>
        </el-table-column>
        <el-table-column label="次数限制" width="120">
          <template #default="scope">
            {{ scope.row.usedCount }} / {{ scope.row.totalCount || '∞' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="scope">
            <el-tooltip content="商品配置" placement="top">
              <el-button circle type="primary" :icon="Setting" @click="viewGoods(scope.row)" />
            </el-tooltip>
            <el-tooltip content="数据统计" placement="top">
              <el-button circle type="success" :icon="View" @click="viewStats(scope.row)" />
            </el-tooltip>
            <el-tooltip content="编辑" placement="top">
              <el-button circle type="primary" :icon="Edit" @click="handleEdit(scope.row)" />
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button circle type="danger" :icon="Delete" @click="del(scope.row.id)" />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑盲盒' : '新增盲盒'" width="50%">
      <el-form :model="form" label-width="120px">
        <el-form-item label="盲盒名称" required>
          <el-input v-model="form.name" placeholder="请输入盲盒名称" />
        </el-form-item>
        <el-form-item label="描述" required>
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入活动描述" />
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss.sssZ"
          />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss.sssZ"
          />
        </el-form-item>
        <el-form-item label="单次价格" required>
          <el-input-number v-model="form.price" :precision="2" :min="0.01" :step="0.1" />
        </el-form-item>
        <el-form-item label="总次数限制">
          <el-input-number v-model="form.totalCount" :min="0" :step="1" />
          <div style="color: #999; font-size: 12px; margin-top: 5px;">0 表示无限制</div>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </template>
    </el-dialog>

    <!-- 商品配置对话框 -->
    <el-dialog v-model="goodsDialogVisible" title="盲盒商品配置" width="70%">
      <div style="margin-bottom: 10px;">
        <el-button type="primary" @click="addGoods" :icon="Plus">添加商品</el-button>
      </div>
      <el-table :data="goodsList">
        <el-table-column label="商品" width="300">
          <template #default="scope">
            <el-select v-model="scope.row.goodsId" placeholder="请选择商品" style="width: 100%;">
              <el-option
                v-for="goods in allGoods"
                :key="goods.id"
                :label="goods.name"
                :value="goods.id"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="权重" width="150">
          <template #default="scope">
            <el-input-number v-model="scope.row.weight" :min="1" :max="100" />
          </template>
        </el-table-column>
        <el-table-column label="库存限制" width="150">
          <template #default="scope">
            <el-input-number v-model="scope.row.stock" :min="0" :step="1" />
            <div style="color: #999; font-size: 12px;">0 表示无限制</div>
          </template>
        </el-table-column>
        <el-table-column label="已抽取" width="100">
          <template #default="scope">
            {{ scope.row.usedStock || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button circle type="danger" :icon="Delete" @click="deleteGoods(scope.$index)" />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="goodsDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveGoods">保存</el-button>
      </template>
    </el-dialog>

    <!-- 统计对话框 -->
    <el-dialog v-model="statsDialogVisible" title="活动统计" width="50%">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="已抽取次数">{{ currentStats.usedCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="参与人数">{{ currentStats.userCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="总收益">¥{{ currentStats.totalRevenue || 0 }}</el-descriptions-item>
        <el-descriptions-item label="总次数限制">{{ currentStats.totalCount || '∞' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<style scoped>
</style>
