<script setup lang="ts">
import { ref } from "vue";

const props = withDefaults(defineProps<{ initial?: number }>(), { initial: 0 });
const count = ref(props.initial);

function increment() {
  throw new Error("TODO");
}
</script>

<template>
  <button type="button" @click="increment">{{ count }}</button>
</template>
