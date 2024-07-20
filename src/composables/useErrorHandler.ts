import { useToast } from '@/components/ui/toast/use-toast'
import { ErrorBase } from '~/exceptions/errorBase'
import { TauriError } from '~/exceptions/tauri.error'
import { DatabaseError } from '~/exceptions/db.error'

export function useErrorHandler() {
  const { toast } = useToast()

  function handleError(error: unknown) {
    console.error('An error occurred:', error)

    let title = 'Error'
    let description = 'An unexpected error occurred.'

    if (error instanceof ErrorBase) {
      title = error.name
      description = error.message
    } else if (error instanceof Error) {
      title = error.name
      description = error.message
    } else if (typeof error === 'string') {
      description = error
    }

    toast({
      title,
      description,
      variant: 'destructive',
    })
  }

  return {
    handleError
  }
}