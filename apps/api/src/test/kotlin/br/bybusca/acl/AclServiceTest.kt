package br.bybusca.acl

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AclServiceTest {
    private val acl = AclService()

    @Test
    fun `clausula de visibilidade inclui public network e restricted com ACL`() {
        val sql = acl.sqlVisibilityClause("d")
        assertTrue(sql.contains("public"))
        assertTrue(sql.contains("network"))
        assertTrue(sql.contains("restricted"))
        assertTrue(sql.contains("document_acl"))
        // três placeholders JDBC para roles, classIds, subjectIds
        assertTrue(sql.count { it == '?' } >= 3)
    }

    @Test
    fun `guest e identificado pelo principal`() {
        val guest = SearchPrincipal(tenantCode = "demo-escola", roles = setOf("GUEST"))
        assertTrue(guest.isGuest)

        val student = SearchPrincipal(
            userId = java.util.UUID.randomUUID(),
            tenantCode = "demo-escola",
            roles = setOf("STUDENT"),
            classIds = setOf("8A")
        )
        assertFalse(student.isGuest)
    }
}
