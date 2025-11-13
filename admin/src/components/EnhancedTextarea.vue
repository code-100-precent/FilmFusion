<template>
  <div class="enhanced-textarea-wrapper" :class="{ 'is-focused': isFocused, 'has-content': hasContent }">
    <div class="textarea-label" v-if="label">
      <span class="label-text">{{ label }}</span>
      <span class="char-count" v-if="showCount">
        {{ currentLength }}{{ maxLength ? ` / ${maxLength}` : '' }}
      </span>
    </div>
    <div class="textarea-container">
      <el-input
        :model-value="modelValue"
        type="textarea"
        :rows="rows"
        :maxlength="maxLength"
        :placeholder="placeholder"
        :disabled="disabled"
        @focus="handleFocus"
        @blur="handleBlur"
        @input="handleInput"
        class="enhanced-textarea"
        resize="vertical"
      />
      <div class="textarea-overlay" v-if="showPlaceholder && !modelValue && !isFocused">
        <span class="placeholder-text">{{ placeholder }}</span>
      </div>
    </div>
    <div class="textarea-footer" v-if="showHint || showCount">
      <span class="hint-text" v-if="showHint && hint">{{ hint }}</span>
      <span class="footer-spacer"></span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  label: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请输入内容...'
  },
  rows: {
    type: Number,
    default: 4
  },
  maxLength: {
    type: Number,
    default: null
  },
  showCount: {
    type: Boolean,
    default: true
  },
  showHint: {
    type: Boolean,
    default: false
  },
  hint: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  },
  showPlaceholder: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'focus', 'blur', 'input'])

const isFocused = ref(false)
const currentLength = computed(() => props.modelValue?.length || 0)
const hasContent = computed(() => props.modelValue && props.modelValue.length > 0)

const handleFocus = (e) => {
  isFocused.value = true
  emit('focus', e)
}

const handleBlur = (e) => {
  isFocused.value = false
  emit('blur', e)
}

const handleInput = (value) => {
  emit('update:modelValue', value)
  emit('input', value)
}
</script>

<style scoped lang="scss">
.enhanced-textarea-wrapper {
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fadeInUp 0.4s ease-out;

  &.is-focused {
    .textarea-container {
      border-color: #9c88ff;
      box-shadow: 0 0 0 3px rgba(156, 136, 255, 0.1);
    }

    .textarea-label {
      .label-text {
        color: #9c88ff;
        transform: translateY(0);
      }
    }
  }

  &.has-content {
    .textarea-label {
      .label-text {
        opacity: 1;
      }
    }
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.textarea-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  transition: all 0.3s ease;

  .label-text {
    font-size: 14px;
    font-weight: 500;
    color: #606266;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .char-count {
    font-size: 12px;
    color: #909399;
    font-weight: 400;
    transition: all 0.3s ease;

    .enhanced-textarea-wrapper.is-focused & {
      color: #9c88ff;
    }
  }
}

.textarea-container {
  position: relative;
  border-radius: 8px;
  border: 2px solid #e4e7ed;
  background: #fff;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;

  &:hover {
    border-color: #c4b5ff;
  }

  :deep(.el-textarea__inner) {
    border: none;
    padding: 12px 16px;
    font-size: 14px;
    line-height: 1.6;
    color: #303133;
    background: transparent;
    resize: vertical;
    transition: all 0.3s ease;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;

    &:focus {
      outline: none;
      box-shadow: none;
    }

    &::placeholder {
      color: #c0c4cc;
      transition: color 0.3s ease;
    }

    &:focus::placeholder {
      color: #e4e7ed;
    }
  }
}

.textarea-overlay {
  position: absolute;
  top: 12px;
  left: 16px;
  pointer-events: none;
  z-index: 1;

  .placeholder-text {
    font-size: 14px;
    color: #c0c4cc;
    font-style: italic;
    animation: fadeIn 0.3s ease-out;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.textarea-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 6px;
  min-height: 20px;

  .hint-text {
    font-size: 12px;
    color: #909399;
    animation: slideInLeft 0.3s ease-out;
  }

  .footer-spacer {
    flex: 1;
  }
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

// 禁用状态
.enhanced-textarea-wrapper.disabled {
  opacity: 0.6;
  cursor: not-allowed;

  .textarea-container {
    background: #f5f7fa;
  }
}

// 字符计数警告
.enhanced-textarea-wrapper.has-warning {
  .char-count {
    color: #e6a23c;
  }
}

.enhanced-textarea-wrapper.has-error {
  .textarea-container {
    border-color: #f56c6c;
  }

  .char-count {
    color: #f56c6c;
  }
}
</style>

