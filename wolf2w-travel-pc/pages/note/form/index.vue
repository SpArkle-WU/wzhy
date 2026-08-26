<template>
  <div class="form-container">
    <div class="ap-head">
      <h1>发表新话题/文章</h1>
      <dl class="on">
        <dt>游记</dt>
        <dd>关联至具体的旅行目的地</dd>
      </dl>
      <dl>
        <dt>随笔</dt>
        <dd>只在我的骡窝窝显示</dd>
      </dl>
      <div class="clearfix"></div>
    </div>
    <div class="ap-wrap">
      <ClientOnly>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
          <el-form-item label-width="50px" prop="title"> <el-input v-model="form.title" placeholder="请在这里输入标题" /> </el-form-item>
          <el-form-item label-width="50px" prop="coverUrl">
            <el-upload
              class="upload-demo"
              :show-file-list="false"
              :multiple="false"
              name="file"
              accept="image/jpeg,image/png,image/jpg,image/gif,image/bmp"
              style="width: 100%"
              :action="uploadAction"
              :headers="uploadHeaders"
              :data="uploadExtraData"
              :before-upload="beforeUpload"
              :on-success="uploadSuc"
              :on-error="uploadErr"
              :auto-upload="true"
              drag
            >
                <el-icon class="el-icon--upload" v-if="!form.coverUrl"><upload-filled /></el-icon>
                <img class="upload-img" :src="form.coverUrl" v-else alt="游记封面图片">
                <div class="el-upload__text">将文件拖放到此处或<em>单击上传</em></div>
              <template #tip>
                <div class="el-upload__tip">大小 小于500kb的jpg/png文件</div>
              </template>
            </el-upload>
          </el-form-item>
          <el-row>
            <el-col :span="8">
              <el-form-item label="出发时间" prop="travelTime">
                <el-date-picker v-model="form.travelTime" type="date" placeholder="请选择" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="人物" prop="person">
                <el-select v-model="form.person" placeholder="请选择">
                  <el-option label="一个人" value="1" />
                  <el-option label="情侣/夫妻" value="2" />
                  <el-option label="带孩子" value="3" />
                  <el-option label="家庭出游" value="4" />
                  <el-option label="和朋友" value="5" />
                  <el-option label="和同学" value="6" />
                  <el-option label="其它" value="7" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="是否公开" prop="isPublic">
                <el-switch v-model="form.isPublic" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="8">
              <el-form-item label="出行天数" prop="day"> <el-input v-model="form.day" placeholder="请输入" /> </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="人均花费/RMB" prop="avgConsume"> <el-input v-model="form.avgConsume" placeholder="请输入" /> </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="旅游地点" prop="destId">
                <el-select placeholder="请选择" v-model="form.destId">
                  <el-option :value="item.id" :label="item.name" v-for="(item,index) in RegiosData" :key="index"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label-width="50px" prop="summary"> <el-input v-model="form.summary" placeholder="请在这里输入摘要" /> </el-form-item>
          <el-form-item label-width="50px" prop="contentStr">
            <TEditor ref="editor" v-model="form.contentStr" v-if="TeditorStatus"/>
          </el-form-item>
          <el-form-item label-width="50px">
            <el-button type="primary" @click="onSubmit(formRef)">发表</el-button>
          </el-form-item>
        </el-form>
      </ClientOnly>
    </div>
  </div>
</template>
<script setup lang="ts">
import {setAllRegios, travelsAdd} from '@/composables/api/note'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'

const router=useRouter();

const TeditorStatus = ref(false)
const RegiosData = ref([])
const form = reactive({
  destId: '',
  title: '',
  travelTime: '',
  coverUrl: '',
  isPublic: false,
  person: '',
  day: '',
  avgConsume: '',
  summary: '',
  contentStr: ''
})

// 上传接口地址（走 nuxt devProxy /api -> gateway:9000）
const uploadAction = '/api/file/upload'

// 上传请求头：携带 token，避免网关/后端因未登录拒绝请求
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token') || ''
  return token ? { Authorization: token } : {}
})

// 上传表单额外数据（兼容后端若需要附加字段时预留）
const uploadExtraData = () => ({})

// 允许上传的图片类型
const ALLOW_TYPES = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/bmp']
const MAX_SIZE_KB = 500

// 上传前校验：类型 + 大小
const beforeUpload = (file: File) => {
  const isTypeAllowed = ALLOW_TYPES.includes(file.type)
  if (!isTypeAllowed) {
    ElMessage.error('只允许上传 jpg / png / gif / bmp 格式的图片')
    return false
  }
  const isLt = file.size / 1024 <= MAX_SIZE_KB
  if (!isLt) {
    ElMessage.error(`图片大小不能超过 ${MAX_SIZE_KB}KB`)
    return false
  }
  return true
}

// 上传成功回调：校验响应结构
const uploadSuc = (response: any, uploadFile: any, uploadItems: any) => {
  // 兼容 R 返回结构：{ code, msg, data: { name, url } }
  if (!response || response.code !== 200) {
    ElMessage.error((response && response.msg) || '封面上传失败，请重试')
    return
  }
  const url = response && response.data && response.data.url
  if (!url) {
    ElMessage.error('封面上传失败：返回数据异常')
    return
  }
  form.coverUrl = url
  ElMessage.success('封面上传成功')
}

// 上传失败回调：HTTP/网络异常
const uploadErr = (err: any, uploadFile: any, uploadItems: any) => {
  console.error('cover upload error:', err)
  ElMessage.error('封面上传失败，请检查登录状态或网络后重试')
}


const rules = reactive<FormRules>({
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  coverUrl:[{ required: true, message: '请上传封面', trigger: 'blur' }],
  travelTime: [{required: true, message: '请输入出发时间', trigger: 'blur'}],
  destId: [{ required: true, message: '请选择旅游地点', trigger: 'blur' }],
  avgConsume: [{required: true, message: '请输入人均花销', trigger: 'blur'}],
  summary: [{required: true, message: '请输入游记摘要', trigger: 'blur'}],
  contentStr: [{ required: true, message: '请输入内容', trigger: 'blur' }]
})
// 加载目的地
const initRegios =function () {

  setAllRegios().then((res) => {
    RegiosData.value = res.data
  })

}

const formRef = ref<FormInstance>()

const onSubmit = async function (formEl: FormInstance | undefined) {
  console.log(formEl.contentStr);

  formEl.validate(async valid => {
    if (valid) {
      try {
        const res = await travelsAdd(form)
        ElMessage.success('提交日志成功')
        router.push('/personal/travels');
      } catch (err) {
        console.log(err)
        ElMessage.error(err.msg);
      }
    } else {
      return false
    }
  })
}


onMounted(() => {
  // 解决teDitor文本编辑框加载异常问题
  setTimeout(() => {
    TeditorStatus.value = true
  }, 200)

  if(!localStorage.getItem('token')){
    ElMessage.error('请先登录');
    navigateTo('/login');
  }

  nextTick(()=>{
    initRegios();
  })

})
</script>
<style lang="scss" scoped>
.form-container {
  position: relative;
  width: 980px;
  margin: 0 auto;
  color: #666;
  .ap-head {
    height: 70px;
    border-bottom: 3px solid #e5e5e5;
    margin-top: 20px;
    h1 {
      width: 178px;
      float: left;
      font-size: 18px;
      line-height: 70px;
      font-weight: normal;
      padding-left: 3px;
    }
    dl {
      width: 158px;
      height: 70px;
      border-bottom: 3px solid #e5e5e5;
      float: left;
      padding-left: 62px;
      position: relative;
      margin: 0 20px -3px 0;
      background: url(/_nuxt/assets/images/ap_sprite2.gif) 23px -80px no-repeat;
      cursor: pointer;
      &.on {
        background-position: 23px 20px;
        border-bottom: 3px solid #ffa800;
      }
      dt {
        font-size: 18px;
        line-height: 22px;
        margin-top: 13px;
      }
      dd {
        font-size: 12px;
        color: #999;
        line-height: 20px;
      }
    }
  }
  .ap-wrap {
    clear: both;
    padding: 46px 0 50px;
  }
}

.upload-img{
  width: 100%;
}
</style>
