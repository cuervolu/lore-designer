export type FieldType =
  | 'text'
  | 'number'
  | 'textarea'
  | 'select'
  | 'multiselect'
  | 'image'
  | 'date'
  | 'CustomProperties'

export interface FieldDefinition {
  key: string
  label: string
  type: FieldType
  required?: boolean
  default?: unknown
  options?: string[]
  placeholder?: string
  rows?: number
}

export interface FormConfig {
  sections: {
    key: string
    fields: FieldDefinition[]
  }[]
}

export interface CharacterFormData {
  frontmatter: Record<string, unknown>
  content: string
}
