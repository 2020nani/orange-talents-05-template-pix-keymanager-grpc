package com.api.cadastrachavepix


import com.api.servicosexternos.operacoesItau.ContaAssociada
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class ChavePix(
    @field:NotNull
    @Column(nullable = false)
    val clienteId: UUID,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoChave: TipoDeChave,

    @field:NotBlank
    @Column(unique = true, nullable = false)
    var chave: String,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoConta: TipoDeConta,

    @field:Valid
    @Embedded
    val conta: ContaAssociada
) {

    @Id
    @GeneratedValue
    val id : UUID? = null

    @CreationTimestamp
    val criadaEm : LocalDateTime = LocalDateTime.now()

    /**
     * Verifica se esta chave pertence a este cliente
     */
    fun pertenceAoCliente(clienteId: UUID) = this.clienteId.equals(clienteId)


    /**
     * Atualiza a valor da chave. Somente chave do tipo ALEATORIA pode
     * ser alterado.
     */
    fun atualizaChave(chave: String): Boolean {
        if (tipoChave == TipoDeChave.ALEATORIA) {
            this.chave = chave
            return true
        }
        return false
    }

    override fun toString(): String {
        return "ChavePix(clienteId=$clienteId, tipoChave=$tipoChave, chave='$chave', tipoConta=$tipoConta, conta=$conta, id=$id, criadaEm=$criadaEm)"
    }
}
