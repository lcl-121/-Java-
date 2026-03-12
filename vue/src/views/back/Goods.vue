<script setup>
import { ref, reactive ,shallowRef} from 'vue'
import {Search, Plus, Delete, Edit, UploadFilled} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { serverHost } from '../../../config/config.default'
import request from '../../utils/request'

//引入富文本组件
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import axios from "axios"

//定义富文本数据
const htmlContent = ref('');
const editorRefContent = shallowRef();

//富文本自定义上传方法
const customUpload = (file, insertFn) => {
  const formData = new FormData()
  formData.append('file', file)
  axios({
    url: `${serverHost}/web/upload`,
    method: 'post',
    data: formData,
    headers: {'Content-Type': 'multipart/form-data'},
  }).then(res => {
    insertFn(res.data)
  }).catch((error) => {
    console.error('上传失败:', error)
    ElMessage.error('上传失败')
  })
}

//wangEditor 配置
const editorConfig = {
  placeholder: '请输入内容...',
  MENU_CONF: {
    uploadImage: {
      customUpload: async (file, insertFn) => {
        customUpload(file, insertFn)
      },
    },
    uploadVideo: {
      customUpload: async (file, insertFn) => {
        customUpload(file, insertFn)
      },
    },
  }
}


// 表格数据
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

// 搜索条件
const searchForm = reactive({
  keyword: '',
})

// 表单数据
const form = ref({})
const dialogFormVisible = ref(false)
const multipleSelection = ref([])

// 加载数据
const load = () => {
  request.get("/goods/page", {
    params: {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchForm.keyword,
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

  form.value.content = htmlContent.value;

  request.post("/goods", form.value).then(res => {
    if (res.code === '200') {
      ElMessage.success("保存成功")
      dialogFormVisible.value = false
      load()
    } else {
      ElMessage.error("保存失败")
    }
  })
}

// 添加
const handleAdd = () => {

  htmlContent.value = '';

  form.value = {}
  dialogFormVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  form.value = JSON.parse(JSON.stringify(row))

  htmlContent.value = form.value.content || '';

  dialogFormVisible.value = true
}

// 删除
const del = (id) => {
  request.delete("/goods/" + id).then(res => {
    if (res.code === '200') {
      ElMessage.success("删除成功")
      load()
    } else {
      ElMessage.error("删除失败")
    }
  })
}

// 批量删除
const delBatch = () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning("请至少选择一条记录")
    return
  }

  const ids = multipleSelection.value.map(v => v.id)
  request.post("/goods/del/batch", ids).then(res => {
    if (res.code === '200') {
      ElMessage.success("批量删除成功")
      load()
    } else {
      ElMessage.error("批量删除失败")
    }
  })
}

// 重置搜索
const reset = () => {
  searchForm.keyword = ""
  load()
}

// 表格选择变化
const handleSelectionChange = (val) => {
  multipleSelection.value = val
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  load()
}

// 页码变化
const handleCurrentChange = (current) => {
  pageNum.value = current
  load()
}

// 确认删除
const confirmDelete = (id) => {
  ElMessageBox.confirm(
      '确定要删除这条数据吗？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        del(id)
      })
}

// 确认批量删除
const confirmBatchDelete = () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning("请至少选择一条记录")
    return
  }

  ElMessageBox.confirm(
      '确定要批量删除这些数据吗？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        delBatch()
      })
}


//图片上传成功的回调
const handleImgUploadSuccess = (res) => {
  form.value.img = res;
};

//修改状态方法
const changeState = (row) => {
  form.value = JSON.parse(JSON.stringify(row))
  request.post("/goods", form.value).then(res => {
    if (res.code === '200') {
      ElMessage.success("保存成功")
      dialogFormVisible.value = false
      load()
    } else {
      ElMessage.error("保存失败")
    }
  })
}

const types = ref([])
const loadType  =()=>{
  request.get('/type').then(res=>{
    types.value=res.data
  })
}
loadType()

const units = ref([])
const loadUnit  =()=>{
  request.get('/unit').then(res=>{
    units.value=res.data
  })
}
loadUnit()

//定义数据
const contentViewVisible = ref(false)
const currentViewContent = ref('')

const viewContent = (content) => {
  currentViewContent.value = content || ''
  contentViewVisible.value = true
}

// 用户信息
const account = ref(
    localStorage.getItem('account') ? JSON.parse(localStorage.getItem('account')) : {}
)

</script>

<template>
  <div class="content-container">

    <!-- 搜索区域 -->
    <div class="header-section">
      <el-input v-model="searchForm.keyword" placeholder="请输入昵称" class="filter-input" :prefix-icon="Search" clearable/>
      <el-button class="ml-10" plain type="primary" @click="load">搜索</el-button>
      <el-button plain type="info" @click="reset">重置</el-button>
    </div>

    <!-- 操作按钮区域 -->
    <div class="toolbar-section">
      <el-button plain type="primary" @click="handleAdd" :icon="Plus" v-if="account.role==='ROLE_UNIT'">新增</el-button>
      <el-button plain type="danger" @click="confirmBatchDelete" :icon="Delete">批量删除</el-button>
    </div>

    <!-- 表格区域 -->
    <el-card>
      <el-table :data="tableData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="商品名称"/>

        <el-table-column label="分类名称">
          <template #default="scope">
            <span v-if="scope.row.typeId">{{types.find(item=>item.id===scope.row.typeId).name}}</span>
          </template>
        </el-table-column>

        <el-table-column prop="info" label="商品简介"/>

        <el-table-column label="商品详情">
          <template #default="scope">
            <el-button type="primary" @click="viewContent(scope.row.content)">查看详情</el-button>
          </template>
        </el-table-column>

        <el-table-column label="商品图" width="120" align="center">
          <template #default="scope">
            <el-image style="width: 80px; height: 80px" :src="scope.row.img" :preview-src-list="[scope.row.img]" :preview-teleported=true></el-image>
          </template>
        </el-table-column>

        <el-table-column prop="inventory" label="库存数量" width="70"></el-table-column>
        <el-table-column prop="price" label="价格" width="70"></el-table-column>
        <el-table-column prop="unit" label="价格计量单位" width="70"></el-table-column>
        <el-table-column prop="date" label="上架日期" width="70"></el-table-column>
        <el-table-column label="商家">
          <template #default="scope">
            <span v-if="scope.row.unitId">{{units.find(item=>item.id===scope.row.unitId).nickname}}</span>
          </template>
        </el-table-column>

        <el-table-column prop="sales" label="销量"width="70"></el-table-column>

        <el-table-column label="是否上架" width="70" align="center">
          <template #default="scope">
            <el-switch v-model="scope.row.status" @change="changeState(scope.row)"/>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="scope">
            <el-tooltip content="编辑" placement="top" :effect="'light'" v-if="account.role==='ROLE_UNIT'">
              <el-button circle type="primary" :icon="Edit" @click="handleEdit(scope.row)"/>
            </el-tooltip>
            <el-tooltip content="删除" placement="top" :effect="'light'">
              <el-button circle type="danger" :icon="Delete" @click="confirmDelete(scope.row.id)"/>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
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

    <!-- 表单对话框 -->
    <el-dialog v-model="dialogFormVisible" :title="form.id ? '编辑' : '新增'" width="70%" destroy-on-close center>
      <el-form :model="form" label-width="100px">

        <el-form-item label="商品名称" required>
          <el-input v-model="form.name" placeholder="商品名称" />
        </el-form-item>

        <el-form-item label="商品分类" required>
          <el-select v-model="form.typeId" placeholder="请选择商品分类" style="width: 240px" >
              <el-option
                v-for="item in types"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
          </el-select>
        </el-form-item>

        <el-form-item label="商品简介" required>
          <el-input v-model="form.info" placeholder="商品简介" />
        </el-form-item>

        <!--富文本的编辑-->
        <el-form-item label="商品详情">
          <div style="border: 1px solid #ccc; z-index: 100;">
            <Toolbar style="border-bottom: 1px solid #ccc" :editor="editorRefContent" :defaultConfig="editorConfig" mode="default" />
            <Editor style="height: 300px; overflow-y: hidden;" v-model="htmlContent" :defaultConfig="editorConfig" mode="default" @onCreated="editorRefContent = $event" />
          </div>
        </el-form-item>


        <!--图片上传组件-->
        <el-form-item label="图片上传">
          <div class="upload-container">
            <el-avatar v-if="form.img" :src="form.img" :size="80" />
            <el-upload :action="`${serverHost}/web/upload`" :on-success="handleImgUploadSuccess" :show-file-list="false">
              <el-button type="primary" :icon="UploadFilled">{{ form.img ? '更换图片' : '上传图片' }}</el-button>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="库存数量" required>
          <el-input type="number" v-model="form.inventory" placeholder="库存数量" />
        </el-form-item>

        <el-form-item label="价格" required>
          <el-input type="number" v-model="form.price" placeholder="价格" />
        </el-form-item>

        <el-form-item label="价格计量单位" required>
          <el-input v-model="form.unit" placeholder="价格计量单位" />
        </el-form-item>


        <!--开关组件-->
        <el-form-item label="是否上架">
          <el-switch v-model="form.status"></el-switch>
        </el-form-item>


      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false">取消</el-button>
          <el-button type="primary" @click="save">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="contentViewVisible" title="详情" width="40%" center>
      <div v-html="currentViewContent"></div>
    </el-dialog>

  </div>
</template>

<style scoped>

</style>