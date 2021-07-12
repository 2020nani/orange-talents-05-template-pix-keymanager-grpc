package com.api.cadastrapixkey.registrochavebacen

import com.api.cadastrapixkey.TipoDeConta
import com.api.cadastrapixkey.contaassociada.ContaAssociada
import com.api.cadastrapixkey.contaassociada.InstituicaoResponse
import com.api.consultachavepix.ChavePixInfo
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

    @Get("/api/v1/pix/keys/{key}",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML]
    )
    fun findByKey(@PathVariable key: String): HttpResponse<PixKeyDetailsResponse>

    data class CreatePixKeyResponse (
        val keyType: PixKeyTipo,
        val key: String,
        val bankAccount: BankAccount,
        val owner: Owner,
        val createdAt: LocalDateTime
    )

    data class PixKeyDetailsResponse (
        val keyType: PixKeyTipo,
        val key: String,
        val bankAccount: BankAccount,
        val owner: Owner,
        val createdAt: LocalDateTime
    ) {
        fun converte(): ChavePixInfo? {
            return ChavePixInfo(
                pixId = null,
                clienteId = null,
                tipoChave = this.keyType.domainType!!,
                tipoConta= when (this.bankAccount.accountType){
                    BankAccount.AccountType.CACC -> TipoDeConta.CONTA_CORRENTE
                    BankAccount.AccountType.SVGS -> TipoDeConta.CONTA_POUPANCA
                },
                valorDaChave = this.key,
                conta = ContaAssociada(
                    instituicao = InstituicaoResponse.nome(bankAccount.participant),
                    nomeDoTitular = owner.name,
                    cpfDoTitular = owner.taxIdNumber,
                    agencia = bankAccount.branch,
                    numeroDaConta = bankAccount.accountNumber,
                ),
                criadoEm = createdAt
            )
        }
    }
}
/* Pix ID (opcional - necessário somente para abordagem 1);
Identificador do cliente (opcional - necessário somente para abordagem 1);
Tipo da chave;
Valor da chave;
Nome e CPF do titular da conta;
Dados da conta vinculada a chave Pix:
nome da instituição financeira;
agência, número da conta e tipo da conta (Corrente ou Poupança);
Data/hora de registro ou criação da chave;
* */