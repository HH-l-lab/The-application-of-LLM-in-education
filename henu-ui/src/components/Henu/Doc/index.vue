<template>
  <div>
    <svg-icon icon-class="question" @click="goto" />
  </div>
</template>

<script>
export default {
  name: 'HenuDoc',
  data() {
    return {
      url: 'http://doc.henu.vip/henu-vue'
    }
  },
  methods: {
    goto() {
      window.open(this.url)
    }
  }
}
</script>