<template>
  <Login
      v-if="currentPage === 'login'"
      @login-success="currentPage = 'home'"
      @switch-to-register="currentPage = 'register'"
  />
  <Register
      v-else-if="currentPage === 'register'"
      @register-success="currentPage = 'login'"
      @switch-to-login="currentPage = 'login'"
  />
  <Home
      v-else-if="currentPage === 'home'"
      @switch-to-publish="currentPage = 'publish'"
      @logout="handleLogout"
      @view-detail="goDetail"
      @go-user-center="currentPage = 'user'"
  />
  <Detail
      v-else-if="currentPage === 'detail'"
      :good-id="currentGoodId"
      @go-back="currentPage = 'home'"
      @buy-success="handleBuySuccess"
      @go-user-center="currentPage = 'user'"
  />
  <Publish
      v-else-if="currentPage === 'publish'"
      @publish-success="currentPage = 'home'"
      @go-home="currentPage = 'home'"
  />
  <UserCenter
      v-else-if="currentPage === 'user'"
      @go-home="currentPage = 'home'"
      @logout="handleLogout"
      @view-good="goDetail"
      @go-publish="currentPage = 'publish'"
  />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Login from './views/Login.vue'
import Register from './views/Register.vue'
import Home from './views/Home.vue'
import Detail from './views/Detail.vue'
import Publish from './views/Publish.vue'
import UserCenter from './views/UserCenter.vue'

const currentPage = ref('login')
const currentGoodId = ref(null)

onMounted(() => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    currentPage.value = 'home'
  }
})

const handleLogout = () => {
  localStorage.removeItem('userInfo')
  currentPage.value = 'login'
}

const goDetail = (id) => {
  currentGoodId.value = id
  currentPage.value = 'detail'
}

const handleBuySuccess = (order) => {
  console.log('购买成功，订单信息：', order)
  // 购买成功后跳转到个人中心查看订单
  setTimeout(() => {
    currentPage.value = 'user'
  }, 2000)
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: #f5f5f5;
}
</style>