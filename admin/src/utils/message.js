import { createApp, h } from 'vue'
import Message from '@/components/common/Message.vue'

let messageInstance = null

const createMessage = () => {
  if (messageInstance) {
    return messageInstance
  }
  
  const div = document.createElement('div')
  document.body.appendChild(div)
  
  const app = createApp({
    render: () => h(Message)
  })
  
  messageInstance = app.mount(div)
  return messageInstance
}

const message = {
  success: (msg, duration = 3000) => {
    const instance = createMessage()
    instance.show(msg, 'success', duration)
  },
  
  error: (msg, duration = 3000) => {
    const instance = createMessage()
    instance.show(msg, 'error', duration)
  },
  
  warning: (msg, duration = 3000) => {
    const instance = createMessage()
    instance.show(msg, 'warning', duration)
  },
  
  info: (msg, duration = 3000) => {
    const instance = createMessage()
    instance.show(msg, 'info', duration)
  }
}

export default message

