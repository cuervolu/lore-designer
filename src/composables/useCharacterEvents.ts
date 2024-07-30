import {ref} from 'vue'

const bus = ref(new Map())

export function useCharacterEvents() {
  const emit = (event: string, data: any) => {
    bus.value.set(event, data)
  }

  const on = (event: string, callback: (data: any) => void) => {
    return watch(() => bus.value.get(event), (newValue) => {
      if (newValue !== undefined) {
        callback(newValue)
        bus.value.delete(event)
      }
    })
  }

  return {
    emit,
    on
  }
}