/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL?: string
  readonly VITE_TENANT?: string
  readonly VITE_GOOGLE_PSE_CX?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
