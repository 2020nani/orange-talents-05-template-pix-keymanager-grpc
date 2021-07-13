package com.api.cadastrachavepix

import com.api.cadastrachavepix.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository: JpaRepository<ChavePix, UUID> {
    fun existsByChave(chave: String?): Boolean

    fun findByIdAndClienteId(fromString: UUID?, fromString1: UUID?): Optional<ChavePix>
    fun findByChave(chave: String?): Optional<ChavePix>
    abstract fun findAllByClienteId(fromString: UUID?): List<ChavePix>

}
