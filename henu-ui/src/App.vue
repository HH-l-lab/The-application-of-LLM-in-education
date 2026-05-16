<template>
  <div id="app">
    <router-view />
    <theme-picker />
  </div>
</template>

<script>
import ThemePicker from "@/components/ThemePicker"

export default {
  name: "App",
  components: { ThemePicker }
}
</script>
<style>
body {
  margin: 0;
  /* An elegant ambient glowing background for frosted glass contrast */
  background: 
    radial-gradient(at 0% 0%, rgba(74, 144, 226, 0.15) 0px, transparent 50%),
    radial-gradient(at 100% 100%, rgba(108, 92, 231, 0.1) 0px, transparent 50%),
    url('~@/assets/images/henu.jpg') no-repeat center center / cover;
  background-attachment: fixed;
  background-blend-mode: overlay;
}

/* Ensure #app does not block the background */
#app {
  background-color: rgba(244, 246, 249, 0.7);
  min-height: 100vh;
}

#app .theme-picker {
  display: none;
}
</style>
