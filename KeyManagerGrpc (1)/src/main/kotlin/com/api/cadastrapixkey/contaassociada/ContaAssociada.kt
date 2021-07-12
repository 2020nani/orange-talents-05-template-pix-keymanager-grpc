package com.api.cadastrapixkey.contaassociada

import javax.persistence.Embeddable

@Embeddable
class ContaAssociada(
    val instituicao: String,
    val nomeDoTitular: String,
    val cpfDoTitular: String,
    val agencia: String,
    val numeroDaConta: String
) {
    companion object {
        public val ITAU_UNIBANCO_ISPB: String = "60701190"
    }

    override fun toString(): String {
        return "ContaAssociada(instituicao='$instituicao', nomeDoTitular='$nomeDoTitular', cpfDoTitular='$cpfDoTitular', agencia='$agencia', numeroDaConta='$numeroDaConta')"
    }


}