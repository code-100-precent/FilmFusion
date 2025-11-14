<template>
  <Teleport to="body">
    <TransitionGroup
      name="message"
      tag="div"
      class="message-container"
    >
      <div
        v-for="item in messages"
        :key="item.id"
        :class="['message-item', `message-${item.type}`]"
      >
        <span class="message-icon">{{ getIcon(item.type) }}</span>
        <span class="message-text">{{ item.message }}</span>
      </div>
    </TransitionGroup>
  </Teleport>
</template>

<script setup>
import { ref } from 'vue'

const messages = ref([])
let messageId = 0

const getIcon = (type) => {
  const icons = {
    success: '✓',
    error: '✕',
    warning: '⚠',
    info: 'ℹ'
  }
  return icons[type] || icons.info
}

const show = (message, type = 'info', duration = 3000) => {
  const id = messageId++
  const messageItem = {
    id,
    message,
    type
  }
  
  messages.value.push(messageItem)
  
  setTimeout(() => {
    const index = messages.value.findIndex(m => m.id === id)
    if (index > -1) {
      messages.value.splice(index, 1)
    }
  }, duration)
}

defineExpose({
  show
})
</script>

<style scoped lang="scss">
.message-container {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10000;
  pointer-events: none;
}

.message-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  margin-bottom: 12px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 300px;
  max-width: 500px;
  pointer-events: auto;
  animation: slideDown 0.3s ease;
  
  .message-icon {
    font-size: 18px;
    font-weight: bold;
    flex-shrink: 0;
  }
  
  .message-text {
    flex: 1;
    font-size: 14px;
    color: #1f2937;
    line-height: 1.5;
  }
  
  &.message-success {
    border-left: 4px solid #10b981;
    
    .message-icon {
      color: #10b981;
    }
  }
  
  &.message-error {
    border-left: 4px solid #ef4444;
    
    .message-icon {
      color: #ef4444;
    }
  }
  
  &.message-warning {
    border-left: 4px solid #f59e0b;
    
    .message-icon {
      color: #f59e0b;
    }
  }
  
  &.message-info {
    border-left: 4px solid #3b82f6;
    
    .message-icon {
      color: #3b82f6;
    }
  }
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-enter-active {
  transition: all 0.3s ease;
}

.message-leave-active {
  transition: all 0.3s ease;
}

.message-enter-from {
  opacity: 0;
  transform: translateY(-20px);
}

.message-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>

