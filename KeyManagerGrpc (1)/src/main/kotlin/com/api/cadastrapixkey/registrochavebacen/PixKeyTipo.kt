package com.api.cadastrapixkey.registrochavebacen

import com.api.cadastrapixkey.TipoDeChave

enum class PixKeyTipo(val domainType: TipoDeChave?) {

    CPF(TipoDeChave.CPF),
    CNPJ(null),
    PHONE(TipoDeChave.CELULAR),
    EMAIL(TipoDeChave.EMAIL),
    RANDOM(TipoDeChave.ALEATORIA);

    companion object {

        private val mapping = PixKeyTipo.values().associateBy(PixKeyTipo::domainType)

        fun by(domainType: TipoDeChave): PixKeyTipo {
            return mapping[domainType]
                ?: throw IllegalArgumentException("PixKeyType invalid or not found for $domainType")
        }
    }
}
