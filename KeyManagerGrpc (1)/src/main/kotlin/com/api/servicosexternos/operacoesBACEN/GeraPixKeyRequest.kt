package com.api.servicosexternos.operacoesBACEN

import com.api.cadastrachavepix.ChavePix
import com.api.servicosexternos.operacoesItau.ContaAssociada


data class GeraPixKeyRequest(
    val keyType: PixKeyTipo,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
) {

    companion object {

        fun converte(chave: ChavePix): GeraPixKeyRequest {
            return GeraPixKeyRequest(
                keyType = PixKeyTipo.by(chave.tipoChave),
                key = chave.chave,
                bankAccount = BankAccount(
                    participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
                    branch = chave.conta.agencia,
                    accountNumber = chave.conta.numeroDaConta,
                    accountType = BankAccount.AccountType.by(chave.tipoConta),
                ),
                owner = Owner(
                    type = Owner.OwnerType.NATURAL_PERSON,
                    name = chave.conta.nomeDoTitular,
                    taxIdNumber = chave.conta.cpfDoTitular
                )
            )
        }
    }
}