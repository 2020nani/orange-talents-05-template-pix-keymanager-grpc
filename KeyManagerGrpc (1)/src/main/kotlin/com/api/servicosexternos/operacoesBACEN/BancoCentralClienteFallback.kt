package com.api.servicosexternos.operacoesBACEN

import com.api.compartilhado.exceptionscustomizadas.ServicoExternoException
import io.micronaut.http.HttpResponse
import io.micronaut.retry.annotation.Fallback

@Fallback
class BancoCentralClienteFallback: BancoCentralCliente {
    override fun registraChaveBACEN(request: GeraPixKeyRequest): HttpResponse<BancoCentralCliente.CreatePixKeyResponse>? {
       return null
    }

    override fun deletaChaveBACEN(request: DeletaPixKeyRequest, key: String): HttpResponse<DeletaPixKeyResponse>? {
        return null
    }

    override fun findByKey(key: String): HttpResponse<BancoCentralCliente.PixKeyDetailsResponse>? {
        return null
    }

}