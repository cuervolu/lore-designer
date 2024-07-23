import { useToast } from '@/components/ui/toast/use-toast'
import { ErrorBase } from '~/exceptions/errorBase'

export function useErrorHandler() {
  const { toast } = useToast()

  function handleError(error: unknown) {
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