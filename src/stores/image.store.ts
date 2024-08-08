import {defineStore} from 'pinia'
import {useDbStore} from './db.store'

export const useImageStore = defineStore('image', () => {
  const dbStore = useDbStore()

  const saveImage = async (imageInfo: { id: string, path: string }): Promise<string> => {
    try {
      await dbStore.executeQuery(
          'INSERT OR REPLACE INTO Images (UUID, Path) VALUES ($1, $2)',
          [imageInfo.id, imageInfo.path]
      )
      return imageInfo.id
    } catch (e) {
      throw e
    }
  }

  const revertImage = async (imageId: string, originalPath: string): Promise<void> => {
    try {
      await dbStore.executeQuery(
          'UPDATE Images SET Path = $2 WHERE UUID = $1',
          [imageId, originalPath]
      )
    } catch (e) {
      throw e
    }
  }

  const getImagePathById = async (imageId: string): Promise<string | null> => {
    try {
      const result = await dbStore.select<[{ Path: string }]>(
          'SELECT Path FROM Images WHERE UUID = $1',
          [imageId]
      )
      return result[0]?.Path || null
    } catch (e) {
      throw e
    }
  }

  return {
    saveImage,
    revertImage,
    getImagePathById
  }
})