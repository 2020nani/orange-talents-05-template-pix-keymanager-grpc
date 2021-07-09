package com.api.cadastrapixkey.contaassociada

import io.micronaut.http.HttpResponse
import io.micronaut.retry.annotation.Fallback

@Fallback
class ItauClientFallback: ItauClient {

    override fun buscaDadosCliente(clienteId: String, tipo: String): HttpResponse<ContaAssociadaForm> {
        return HttpResponse.ok(null)
    }

}