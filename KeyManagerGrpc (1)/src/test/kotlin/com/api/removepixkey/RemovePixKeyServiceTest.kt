package com.api.removepixkey

import com.api.PixKeyChaveRequest
import com.api.RemovePixKeyServiceGrpc
import com.api.cadastrapixkey.*
import com.api.cadastrapixkey.CadastraNovaChavePixServiceTest
import com.api.cadastrapixkey.contaassociada.ContaAssociada
import com.api.cadastrapixkey.registrochavebacen.BancoCentralCliente
import com.api.cadastrapixkey.registrochavebacen.DeletaPixKeyRequest
import com.api.cadastrapixkey.registrochavebacen.DeletaPixKeyResponse
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RemovePixKeyServiceTest(
    val repository: NovaChavePixRepository,
    val grpcClient: RemovePixKeyServiceGrpc.RemovePixKeyServiceBlockingStub
) {

    @Inject
    lateinit var bcbClient: BancoCentralCliente

    companion object {
        val CLIENT_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()

    }

    @Test
    fun `nao deve excluir se ClienteId e pixId nao existirem ou nao pertencer ao mesmo usuario`() {
        //cenario
        val chavePix = repository.save(
            chave(
                tipo = TipoDeChave.CPF,
                chave = "63657520325",
                clienteId = CadastraNovaChavePixServiceTest.CLIENT_ID
            )
        )
        //acao
        val thrown = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.excluiPixKey(
                PixKeyChaveRequest.newBuilder()
                    .setIdCliente("8e5379fe-2ccd-42e6-9ce5-afbf0008df03")
                    .setPixId(chavePix.id.toString())
                    .build()
            )
        }
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Nao existe uma chave-pix cadastrada ou a chave nao pertence ao cliente ", status.description)
        }
    }

    @Test
    fun `deve excluir se ClienteId e pixId existirem `() {
        // cenário
        val chavePix = repository.save(
            chave(
                tipo = TipoDeChave.CPF,
                chave = "63657520325",
                clienteId = CadastraNovaChavePixServiceTest.CLIENT_ID
            )
        )

        // cenário
        `when`(bcbClient.deletaChaveBACEN(DeletaPixKeyRequest("63657520325"), "63657520325"))
            .thenReturn(
                HttpResponse.ok(
                    DeletaPixKeyResponse(
                        key = "63657520325",
                        participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
                        deletedAt = LocalDateTime.now()
                    )
                )
            )

        //acao
        val response = grpcClient.excluiPixKey(
            PixKeyChaveRequest.newBuilder()
                .setIdCliente(chavePix.clienteId.toString())
                .setPixId(chavePix.id.toString())
                .build()
        )
        //validacao

        with(response) {
            assertFalse(repository.existsById(chavePix.id))
        }
    }

    @Test
    fun `nao deve excluir se chave pix nao for excluida no BACEN `() {
        // cenário
        val chavePix = repository.save(
            chave(
                tipo = TipoDeChave.CPF,
                chave = "63657520325",
                clienteId = CadastraNovaChavePixServiceTest.CLIENT_ID
            )
        )

        // cenário
        `when`(bcbClient.deletaChaveBACEN(DeletaPixKeyRequest("63657520325"), "63657520325"))
            .thenReturn(
                HttpResponse.notFound()
            )

        //acao
        val thrown = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.excluiPixKey(
                PixKeyChaveRequest.newBuilder()
                    .setIdCliente(chavePix.clienteId.toString())
                    .setPixId(chavePix.id.toString())
                    .build()
            )
        }
        // validação
        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao deletar chave Pix no Banco Central do Brasil (BCB)", status.description)
            assertTrue(repository.existsById(chavePix.id))
        }

    }



    @Test
    fun `nao deve excluir se dados invalidos`() {
        //cenario

        //acao
        val thrown = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.excluiPixKey(
                PixKeyChaveRequest.newBuilder()
                    .setIdCliente("teste-5546352-rteste")
                    .setPixId("teste-teste-ghgr56647")
                    .build()
            )
        }
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }

    @MockBean(BancoCentralCliente::class)
    fun bcbClientMock(): BancoCentralCliente? {
        return Mockito.mock(BancoCentralCliente::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RemovePixKeyServiceGrpc.RemovePixKeyServiceBlockingStub {
            return RemovePixKeyServiceGrpc.newBlockingStub((channel))

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
                nomeDoTitular = "Rafael Ponte",
                cpfDoTitular = "63657520325",
                agencia = "1218",
                numeroDaConta = "291900"
            )
        )
    }
}