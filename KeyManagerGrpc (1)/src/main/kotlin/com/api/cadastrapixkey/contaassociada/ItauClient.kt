package com.api.cadastrapixkey.contaassociada
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import io.micronaut.retry.annotation.Retryable


@Client("\${itau.contas.url}")
@Retryable(attempts = "1")
interface ItauClient {
    @Get("/api/v1/clientes/{clienteId}/contas{?tipo}")
    fun buscaDadosCliente(@PathVariable("clienteId") clienteId: String, @QueryValue tipo: String): HttpResponse<ContaAssociadaForm>
}