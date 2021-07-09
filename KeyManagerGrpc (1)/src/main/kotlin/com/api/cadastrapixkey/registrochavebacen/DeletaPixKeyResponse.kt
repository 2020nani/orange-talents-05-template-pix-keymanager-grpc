package com.api.cadastrapixkey.registrochavebacen

import com.api.cadastrapixkey.contaassociada.ContaAssociada
import java.time.LocalDateTime

data class DeletaPixKeyResponse (
    val key: String,
    val participant: String = ContaAssociada.ITAU_UNIBANCO_ISPB,
    val deletedAt: LocalDateTime
)
