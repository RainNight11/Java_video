<template>
  <div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h1 class="h3 mb-0 text-gray-800">Dashboard</h1>
      <button class="btn btn-primary d-flex align-items-center" @click="jobsStore.fetchAllJobs" :disabled="jobsStore.loading">
        <i class="bi bi-arrow-clockwise me-2"></i>
        <span v-if="!jobsStore.loading">Refresh</span>
        <span v-else>Loading...</span>
      </button>
    </div>

    <div v-if="jobsStore.error" class="alert alert-danger">{{ jobsStore.error }}</div>

    <div class="row">
      <div v-if="jobsStore.loading && jobsArray.length === 0" class="col-12">
        <p>Loading jobs...</p>
      </div>
      <div v-else-if="jobsArray.length === 0" class="col-12">
        <p>No jobs found. Create a new one to get started!</p>
      </div>
      <JobCard v-for="job in jobsArray" :key="job.jobId" :job="job" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue';
import { useJobsStore } from '../stores/jobs';
import JobCard from '../components/JobCard.vue';

const jobsStore = useJobsStore();

const jobsArray = computed(() => Object.values(jobsStore.jobs));

onMounted(() => {
  jobsStore.fetchAllJobs();
});
</script>

<style scoped>
/* Styles for the dashboard */
</style>
