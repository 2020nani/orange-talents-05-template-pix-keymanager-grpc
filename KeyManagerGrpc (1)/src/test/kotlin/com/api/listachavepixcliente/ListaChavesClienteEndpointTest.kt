package com.api.listachavepixcliente

import com.api.ListaPixKeyServiceGrpc
import com.api.ListaChavesPixRequest
import com.api.TipoChave
import com.api.cadastrachavepix.ChavePix
import com.api.cadastrachavepix.ChavePixRepository
import com.api.cadastrachavepix.TipoDeChave
import com.api.cadastrachavepix.TipoDeConta
import com.api.servicosexternos.operacoesItau.ContaAssociada
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ListaChavesClienteEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: ListaPixKeyServiceGrpc.ListaPixKeyServiceBlockingStub
) {

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.save(chave(tipo = TipoDeChave.EMAIL, chave = "hernani.teste@zup.com.br", clienteId = CLIENTE_ID))
        repository.save(chave(tipo = TipoDeChave.ALEATORIA, chave = "randomkey-2", clienteId = UUID.randomUUID()))
        repository.save(chave(tipo = TipoDeChave.ALEATORIA, chave = "randomkey-3", clienteId = CLIENTE_ID))
    }

    @Test
    fun `deve listar as chaves pix de um usuario pelo seu identificador ERP Itau`() {
        //acao
        //acao
        val response = grpcClient.listar(
            ListaChavesPixRequest
                .newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .build()
        )
        //validacao
        with(response) {
            assertEquals(response.chavesList.size, 2)
            assertThat(
                response.chavesList.map { Pair(it.tipoChave, it.chave) }.toList(),
                containsInAnyOrder(
                    Pair(TipoChave.EMAIL, "hernani.teste@zup.com.br"),
                    Pair(TipoChave.ALEATORIA, "randomkey-3")
                )
            )
        }
    }

    @Test
    fun `nao deve listar as chaves se clienteId invalido`() {
        //acao
        val response = assertThrows<StatusRuntimeException> {
            grpcClient.listar(
                ListaChavesPixRequest
                    .newBuilder()
                    .build()
            )
        }
        //validacao
        with(response) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Cliente ID não pode ser nulo ou vazio", status.description)
        }
    }

    @Test
    fun `deve retornar lista vazia se clienteId nao existir ou nao possuir chaves`() {
        //acao
        val response = grpcClient.listar(
            ListaChavesPixRequest
                .newBuilder()
                .setClienteId("571b770d-409d-42d3-bec2-37461de24e11")
                .build()
        )

        //validacao
        with(response) {
            assertEquals(response.chavesList.size, 0)
        }
    }


    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Factory
    class Clients {
        @Singleton
        fun blockStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ListaPixKeyServiceGrpc.ListaPixKeyServiceBlockingStub {
            return ListaPixKeyServiceGrpc.newBlockingStub((channel))

        }
    }

    private fun chave(
        tipo: TipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipoChave = tipo,
            chave = chave,
            tipoConta = TipoDeConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Hernani Almeida",
                cpfDoTitular = "12345678900",
                agencia = "1218",
                numeroDaConta = "123456"
            )
        )
    }
}