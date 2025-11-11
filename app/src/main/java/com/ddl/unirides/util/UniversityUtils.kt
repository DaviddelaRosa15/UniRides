package com.ddl.unirides.util

/**
 * Utilidad para extraer el nombre de la universidad a partir del email institucional
 */
object UniversityUtils {

    /**
     * Extrae el nombre de la universidad del email
     *
     * @param email Email institucional del usuario
     * @return Nombre corto de la universidad o "Universidad" si no se reconoce
     */
    fun getUniversityFromEmail(email: String): String {
        return when {
            email.endsWith("@pucmm.edu.do", ignoreCase = true) -> "PUCMM"
            email.endsWith("@intec.edu.do", ignoreCase = true) -> "INTEC"
            email.endsWith("@unphu.edu.do", ignoreCase = true) -> "UNPHU"
            email.endsWith("@uasd.edu.do", ignoreCase = true) -> "UASD"
            email.endsWith("@unibe.edu.do", ignoreCase = true) -> "UNIBE"
            email.endsWith("@ucsd.edu.do", ignoreCase = true) -> "UCSD"
            email.endsWith("@utesa.edu", ignoreCase = true) -> "UTESA"
            email.endsWith("@ufhec.edu.do", ignoreCase = true) -> "UFHEC"
            email.endsWith("@ucne.edu", ignoreCase = true) -> "UCNE"
            email.endsWith("@unicda.edu.do", ignoreCase = true) -> "UNICDA"
            email.endsWith("@itla.edu.do", ignoreCase = true) -> "ITLA"
            email.endsWith("@o-m.edu.do", ignoreCase = true) -> "O&M"
            else -> "Universidad"
        }
    }
}

