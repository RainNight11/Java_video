<template>
  <div class="col-xl-3 col-md-6 mb-4">
    <div :class="['card', 'border-left-' + statusColor, 'shadow', 'h-100', 'py-2']">
      <div class="card-body">
        <div class="row no-gutters align-items-center">
          <div class="col mr-2">
            <div :class="['text-xs', 'font-weight-bold', 'text-' + statusColor, 'text-uppercase', 'mb-1']">
              {{ job.status }}
            </div>
            <div class="h5 mb-0 font-weight-bold text-gray-800">Job #{{ job.id }}</div>
          </div>
          <div class="col-auto">
            <i :class="['bi', icon, 'fa-2x', 'text-gray-300']"></i>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps({
  job: {
    type: Object,
    required: true
  }
});

const statusColor = computed(() => {
  switch (props.job.status) {
    case 'RUNNING':
      return 'primary';
    case 'COMPLETED':
      return 'success';
    case 'FAILED':
      return 'danger';
    default:
      return 'secondary';
  }
});

const icon = computed(() => {
  switch (props.job.status) {
    case 'RUNNING':
      return 'bi-gear-fill';
    case 'COMPLETED':
      return 'bi-check-circle-fill';
    case 'FAILED':
      return 'bi-x-circle-fill';
    default:
      return 'bi-question-circle-fill';
  }
});
</script>

<style scoped>
.card {
  border-left-width: 0.25rem;
}
.border-left-primary {
  border-left-color: #FF7A00 !important;
}
.text-primary {
  color: #FF7A00 !important;
}
.border-left-success {
  border-left-color: #1cc88a !important;
}
.border-left-danger {
  border-left-color: #e74a3b !important;
}
.border-left-secondary {
  border-left-color: #858796 !important;
}
.text-xs {
    font-size: .7rem;
}
</style>
