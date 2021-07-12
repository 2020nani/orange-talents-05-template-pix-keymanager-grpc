package com.api.servicosexternos.operacoesBACEN

import com.api.servicosexternos.operacoesItau.ContaAssociada

data class DeletaPixKeyRequest (
    val key: String,
    val participant: String = ContaAssociada.ITAU_UNIBANCO_ISPB
        )
