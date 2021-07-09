package com.api.cadastrapixkey.registrochavebacen

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import io.micronaut.retry.annotation.Retryable
import io.micronaut.http.MediaType
import java.time.LocalDateTime

@Client("\${bcb.pix.url}")
@Retryable(attempts = "1")
interface BancoCentralCliente {

    @Post("/api/v1/pix/keys",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML]
    )
    fun registraChaveBACEN(@Body request: GeraPixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Delete("/api/v1/pix/keys/{key}",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML]
    )
    fun deletaChaveBACEN(@Body request: DeletaPixKeyRequest, @PathVariable key: String): HttpResponse<DeletaPixKeyResponse>

    data class CreatePixKeyResponse (
        val keyType: PixKeyTipo,
        val key: String,
        val bankAccount: BankAccount,
        val owner: Owner,
        val createdAt: LocalDateTime
    )
}