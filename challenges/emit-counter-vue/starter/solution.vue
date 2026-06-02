<script setup lang="ts">
import { ref } from "vue";

const count = ref(0);

function increment() {
  throw new Error("TODO");
}
</script>

<template>
  <p aria-live="polite">{{ count }}</p>
  <button type="button" @click="increment">+1</button>
</template>
