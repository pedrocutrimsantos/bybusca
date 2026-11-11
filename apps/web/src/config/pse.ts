/** Google Programmable Search — mecanismo da escola (ID público). */
export const GOOGLE_PSE_CX =
  import.meta.env.VITE_GOOGLE_PSE_CX || 'e54b7047fc7e64e10'

/** Página pública do engine (Google CSE). */
export const GOOGLE_PSE_PUBLIC_URL = `https://cse.google.com/cse?cx=${GOOGLE_PSE_CX}`
