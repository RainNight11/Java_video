<template>
  <header class="app-header bg-white shadow-sm p-3 d-flex justify-content-between align-items-center">
    <h1 class="h4 mb-0 text-dark">{{ $route.name ? $route.name.toString().charAt(0).toUpperCase() + $route.name.toString().slice(1) : 'Dashboard' }}</h1>
    <!-- Future elements like search bar or user profile can go here -->
  </header>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router';

const route = useRoute();
</script>

<style scoped>
.app-header {
  border-bottom: 1px solid #dee2e6; /* A light border for separation */
}
</style>
