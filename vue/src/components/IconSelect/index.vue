<template>
  <div style="height: 200px;">
    <div style="display: flex; flex-wrap: wrap;">
      <div
          v-for="(item, index) in iconList"
          class="icon-item-wrapper"
          :key="index"
          @click="selectedIcon(item)"
      >
        <div class="icon-item">
          <svg-icon :icon-class="item" style="height: 25px; width: 16px;" />
          <span>{{ item }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";

const modules = import.meta.glob('@/assets/icons/svg/*.svg');

const icons = Object.keys(modules).map(path =>
    path.split('assets/icons/svg/')[1].split('.svg')[0]
);

const iconList = ref(icons);

const emit = defineEmits(['selected']);

const selectedIcon = (name) => {
  emit('selected', name);
  document.body.click();
};
</script>

<style scoped>
.icon-item-wrapper {
  width: calc(100% / 3);
  height: 25px;
  line-height: 25px;
  cursor: pointer;
  display: flex;
}

.icon-item {
  display: flex;
  max-width: 100%;
  height: 100%;
  padding: 0 5px;
}

.icon-item:hover {
  background: #ececec;
}
</style>