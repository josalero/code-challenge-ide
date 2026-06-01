<script setup lang="ts">
import { ref } from "vue";

const props = defineProps<{ items: string[] }>();
const query = ref("");
</script>

<template>
  <input v-model="query" aria-label="filter" />
  <ul>
    <li v-for="item in props.items" :key="item">{{ item }}</li>
  </ul>
</template>
