package com.api.servicosexternos.operacoesBACEN

import com.api.servicosexternos.operacoesItau.ContaAssociada
import java.time.LocalDateTime

data class DeletaPixKeyResponse (
    val key: String,
    val participant: String = ContaAssociada.ITAU_UNIBANCO_ISPB,
    val deletedAt: LocalDateTime
)
