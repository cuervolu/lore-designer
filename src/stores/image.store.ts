import {defineStore} from 'pinia'
import {useDbStore} from './db.store'
import {error} from "@tauri-apps/plugin-log"

export const useImageStore = defineStore('image', () => {
  const dbStore = useDbStore()

  const saveImage = async (imageInfo: { id: string, path: string }): Promise<string> => {
    try {
      const result = await dbStore.executeQuery(
          'INSERT OR REPLACE INTO Images (UUID, Path) VALUES ($1, $2)',
          [imageInfo.id, imageInfo.path]
      )
      return imageInfo.id
    } catch (e) {
      await error(`Error saving image: ${e}`)
      throw e
    }
  }

  return {
    saveImage
  }
})