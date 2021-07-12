package com.api.consultachavepix

import com.api.CarregaChavePixRequest
import com.api.CarregaChavePixRequest.FiltroCase.*
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun CarregaChavePixRequest?.filtraPorTipoRequisicao(validator: Validator): TipoConsulta? {

    val filtro = when(this?.filtroCase!!) { // 1
        PIXID -> this.pixId?.let { // 1
            TipoConsulta.PorPixId(clienteId = it.clienteId, pixId = it.pixId) // 1
        }
        CHAVE -> TipoConsulta.PorChave(chave)// 2
        FILTRO_NOT_SET -> TipoConsulta.Invalido() // 2
    }

    val violations = validator.validate(filtro)
    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations);
    }

    return filtro

}