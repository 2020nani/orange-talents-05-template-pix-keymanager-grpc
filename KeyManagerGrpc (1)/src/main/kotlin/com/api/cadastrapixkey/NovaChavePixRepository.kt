package com.api.cadastrapixkey

import com.api.cadastrapixkey.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface NovaChavePixRepository: JpaRepository<ChavePix, UUID> {
    fun existsByChave(chave: String?): Boolean

    fun findByIdAndClienteId(fromString: UUID?, fromString1: UUID?): Optional<ChavePix>

}
