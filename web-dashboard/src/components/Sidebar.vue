<template>
  <div class="sidebar d-flex flex-column flex-shrink-0 p-3 text-white bg-dark vh-100" style="width: 280px;">
    <a href="/" class="d-flex align-items-center mb-3 mb-md-0 me-md-auto text-white text-decoration-none">
      <i class="bi bi-robot me-2" style="font-size: 2rem;"></i>
      <span class="fs-4">Virtual Presenter</span>
    </a>
    <hr>
    <ul class="nav nav-pills flex-column mb-auto">
      <li class="nav-item">
        <RouterLink to="/" class="nav-link text-white" active-class="active">
          <i class="bi bi-grid-fill me-2"></i>
          Dashboard
        </RouterLink>
      </li>
      <li>
        <RouterLink to="/create" class="nav-link text-white" active-class="active">
          <i class="bi bi-plus-circle-fill me-2"></i>
          Create Story
        </RouterLink>
      </li>
      <li>
        <RouterLink to="/clone" class="nav-link text-white" active-class="active">
          <i class="bi bi-soundwave me-2"></i>
          Voice Clone
        </RouterLink>
      </li>
    </ul>
    <hr>
    <div class="dropdown">
      <a href="#" class="d-flex align-items-center text-white text-decoration-none dropdown-toggle" id="dropdownUser1" data-bs-toggle="dropdown" aria-expanded="false">
        <img src="https://github.com/mdo.png" alt="" width="32" height="32" class="rounded-circle me-2">
        <strong>User</strong>
      </a>
      <ul class="dropdown-menu dropdown-menu-dark text-small shadow" aria-labelledby="dropdownUser1">
        <li><a class="dropdown-item" href="#">Settings</a></li>
        <li><a class="dropdown-item" href="#">Profile</a></li>
        <li><hr class="dropdown-divider"></li>
        <li><a class="dropdown-item" href="#">Sign out</a></li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router';
</script>

<style scoped>
.sidebar .nav-link.active {
  background-color: #FF7A00 !important; /* Orange for active link */
}

.sidebar .nav-link:hover {
  background-color: #495057;
}

.sidebar .fs-4 {
    font-weight: bold;
}
</style>
