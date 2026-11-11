package br.bybusca.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface TenantRepository : JpaRepository<Tenant, UUID> {
    fun findByCode(code: String): Tenant?
}

interface UserRepository : JpaRepository<AppUser, UUID> {
    fun findByTenantIdAndUsername(tenantId: UUID, username: String): AppUser?

    @Query(
        value = """
            SELECT r.code FROM roles r
            INNER JOIN user_roles ur ON ur.role_id = r.id
            WHERE ur.user_id = :userId
        """,
        nativeQuery = true
    )
    fun findRoleCodes(@Param("userId") userId: UUID): List<String>
}

interface DocumentRepository : JpaRepository<DocumentEntity, UUID> {
    fun findByTenantIdAndExternalId(tenantId: UUID, externalId: String): DocumentEntity?

    @Query(
        value = """
            SELECT COUNT(*) FROM documents d
            WHERE d.tenant_id = :tenantId AND d.deleted_at IS NULL
        """,
        nativeQuery = true
    )
    fun countActive(@Param("tenantId") tenantId: UUID): Long
}

interface DocumentAclRepository : JpaRepository<DocumentAcl, UUID>
