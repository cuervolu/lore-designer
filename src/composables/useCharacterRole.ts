export function useCharacterRole() {
  const { t } = useI18n()

  const translateRole = (role: string) => {
    switch (role) {
      case 'Primary':
        return t('characters.role.primary')
      case 'Secondary':
        return t('characters.role.secondary')
      case 'Tertiary':
        return t('characters.role.tertiary')
      case 'Undefined':
        return t('characters.role.undefined')
      default:
        return t('characters.role.unknown')
    }
  }

  return {
    translateRole
  }
}