package br.bybusca.acl

/** Contexto do sujeito da busca (visitante ou autenticado). */
data class SearchPrincipal(
    val userId: java.util.UUID? = null,
    val tenantCode: String,
    val roles: Set<String> = setOf("GUEST"),
    val classIds: Set<String> = emptySet(),
    val subjectIds: Set<String> = emptySet()
) {
    val isGuest: Boolean get() = userId == null || roles == setOf("GUEST")
}

/**
 * Predicados SQL de ACL com placeholders JDBC (?).
 * Ordem dos arrays ao bind: roles, classIds, subjectIds.
 */
class AclService {
    fun sqlVisibilityClause(alias: String = "d"): String =
        """
        (
          $alias.visibility IN ('public', 'network')
          OR (
            $alias.visibility = 'restricted'
            AND EXISTS (
              SELECT 1 FROM document_acl a
              WHERE a.document_id = $alias.id
              AND (
                cardinality(a.allowed_roles) = 0
                OR a.allowed_roles && ?
              )
              AND (
                cardinality(a.allowed_class_ids) = 0
                OR a.allowed_class_ids && ?
              )
              AND (
                cardinality(a.allowed_subject_ids) = 0
                OR a.allowed_subject_ids && ?
              )
            )
          )
        )
        """.trimIndent()
}
