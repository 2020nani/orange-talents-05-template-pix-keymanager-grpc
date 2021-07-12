package com.api.servicosexternos.operacoesItau

import io.micronaut.http.HttpResponse
import io.micronaut.retry.annotation.Fallback

@Fallback
class ItauClientFallback: ItauClient {

    override fun buscaDadosCliente(clienteId: String, tipo: String): HttpResponse<ContaAssociadaForm>? {
        return null
    }

}