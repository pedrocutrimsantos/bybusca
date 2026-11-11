package br.bybusca.domain

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "tenants")
data class Tenant(
    @Id val id: UUID = UUID.randomUUID(),
    @Column(nullable = false, unique = true) val code: String = "",
    @Column(nullable = false) val name: String = "",
    @Column(name = "created_at", nullable = false) val createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "users")
data class AppUser(
    @Id val id: UUID = UUID.randomUUID(),
    @Column(name = "tenant_id", nullable = false) val tenantId: UUID = UUID.randomUUID(),
    @Column(nullable = false) val username: String = "",
    @Column(name = "password_hash", nullable = false) val passwordHash: String = "",
    @Column(name = "display_name", nullable = false) val displayName: String = "",
    @Column(nullable = false) val active: Boolean = true,
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "class_ids", columnDefinition = "text[]")
    val classIds: Array<String> = emptyArray(),
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "subject_ids", columnDefinition = "text[]")
    val subjectIds: Array<String> = emptyArray()
) {
    override fun equals(other: Any?) = other is AppUser && id == other.id
    override fun hashCode() = id.hashCode()
}

@Entity
@Table(name = "roles")
data class Role(
    @Id val id: UUID = UUID.randomUUID(),
    @Column(nullable = false, unique = true) val code: String = "",
    @Column(nullable = false) val name: String = ""
)

@Entity
@Table(name = "documents")
data class DocumentEntity(
    @Id val id: UUID = UUID.randomUUID(),
    @Column(name = "tenant_id", nullable = false) val tenantId: UUID = UUID.randomUUID(),
    @Column(name = "external_id", nullable = false) val externalId: String = "",
    @Column(nullable = false) val type: String = "",
    @Column(nullable = false) val title: String = "",
    @Column(nullable = false) val body: String = "",
    @Column(nullable = false) val url: String = "",
    @Column(nullable = false) val visibility: String = "network",
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    val metadata: Map<String, Any?> = emptyMap(),
    @Column(name = "indexed_at", nullable = false) val indexedAt: Instant = Instant.now(),
    @Column(name = "deleted_at") val deletedAt: Instant? = null
)

@Entity
@Table(name = "document_acl")
data class DocumentAcl(
    @Id @Column(name = "document_id") val documentId: UUID = UUID.randomUUID(),
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_roles", columnDefinition = "text[]")
    val allowedRoles: Array<String> = emptyArray(),
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_class_ids", columnDefinition = "text[]")
    val allowedClassIds: Array<String> = emptyArray(),
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_subject_ids", columnDefinition = "text[]")
    val allowedSubjectIds: Array<String> = emptyArray(),
    @Column(name = "owner_user_id") val ownerUserId: UUID? = null
) {
    override fun equals(other: Any?) = other is DocumentAcl && documentId == other.documentId
    override fun hashCode() = documentId.hashCode()
}
