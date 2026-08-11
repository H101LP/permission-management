vue
<template>
  <svg aria-hidden="true" class="svg-icon">
    <use :href="iconName" :fill="color" />
  </svg>
</template>

<script setup>
import {computed} from "vue";

const props = defineProps({
iconClass:{
  type: String,
  required: true,
},
  color: {
    type: String,
    required: false,
    default: '',
  },
})
//生成完整图标引用ID
const iconName = computed(() => `#icon-${props.iconClass}`)

</script>



<style scoped>
.svg-icon {
  width: 1em;
  height: 1em;
  vertical-align: -2px;
  fill: currentColor;
  overflow: hidden;
  font-size: 25px;
  position: relative;
  fill: currentColor;
}
</style>
