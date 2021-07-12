package com.api.consultachavepix

import com.api.cadastrachavepix.ChavePix
import com.api.cadastrachavepix.TipoDeChave
import com.api.cadastrachavepix.TipoDeConta
import com.api.servicosexternos.operacoesItau.ContaAssociada
import java.time.LocalDateTime
import java.util.*

class ChavePixInfo(
    val pixId: UUID?,
    val clienteId: UUID?,
    val tipoChave: TipoDeChave,
    val tipoConta: TipoDeConta,
    val valorDaChave: String,
    val conta: ContaAssociada,
    val criadoEm: LocalDateTime
) {
    companion object {
        fun converte(chave: ChavePix): ChavePixInfo {
            return ChavePixInfo(
                pixId = chave.id,
                clienteId = chave.clienteId,
                tipoChave = chave.tipoChave,
                tipoConta= chave.tipoConta,
                valorDaChave = chave.chave,
                conta = chave.conta,
                criadoEm = chave.criadaEm
            )
        }
    }

    override fun toString(): String {
        return "ChavePixInfo(pixId=$pixId, clienteId=$clienteId, tipoChave=$tipoChave, tipoConta=$tipoConta, valorDaChave='$valorDaChave', conta=$conta, criadoEm=$criadoEm)"
    }

}
/*
* Pix ID (opcional - necessário somente para abordagem 1);
Identificador do cliente (opcional - necessário somente para abordagem 1);
Tipo da chave;
Valor da chave;
Nome e CPF do titular da conta;
Dados da conta vinculada a chave Pix:
nome da instituição financeira;
agência, número da conta e tipo da conta (Corrente ou Poupança);
Data/hora de registro ou criação da chave;
* */