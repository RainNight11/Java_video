<template>
  <div class="d-flex vh-100">
    <Sidebar />
    <main class="flex-grow-1 d-flex flex-column">
      <AppHeader />
      <div class="flex-grow-1 overflow-auto p-4">
        <RouterView />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router';
import Sidebar from './components/Sidebar.vue';
import AppHeader from './components/AppHeader.vue';
</script>

<style>
/* Add any global app styles here if needed */
.main-content {
  background-color: #f4f7f9;
  height: 100vh;
  overflow-y: auto;
}
</style>
