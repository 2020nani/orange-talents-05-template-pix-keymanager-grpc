package com.api.cadastrapixkey.registrochavebacen

import com.api.cadastrapixkey.contaassociada.ContaAssociada

data class DeletaPixKeyRequest (
    val key: String,
    val participant: String = ContaAssociada.ITAU_UNIBANCO_ISPB
        )
