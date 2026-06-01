<script setup lang="ts">
import { ref } from "vue";

const text = ref("");
const todos = ref<string[]>([]);

function addTodo() {
  throw new Error("TODO");
}
</script>

<template>
  <input v-model="text" aria-label="todo" />
  <button type="button" @click="addTodo">Add</button>
  <ul><li v-for="(t, i) in todos" :key="i">{{ t }}</li></ul>
</template>
