package com.api.consultachavepix

import com.api.CarregaChavePixRequest
import com.api.CarregaPixKeyServiceGrpc
import com.api.cadastrachavepix.ChavePix
import com.api.cadastrachavepix.ChavePixRepository
import com.api.cadastrachavepix.TipoDeChave
import com.api.cadastrachavepix.TipoDeConta
import com.api.servicosexternos.operacoesBACEN.BancoCentralCliente
import com.api.servicosexternos.operacoesBACEN.BankAccount
import com.api.servicosexternos.operacoesBACEN.Owner
import com.api.servicosexternos.operacoesBACEN.PixKeyTipo
import com.api.servicosexternos.operacoesItau.ContaAssociada
import com.api.utils.violations
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class NovaConsultaChavePixEndpointTest (
    val repository: ChavePixRepository,
    val grpcClient: CarregaPixKeyServiceGrpc.CarregaPixKeyServiceBlockingStub
        ){

    @Inject
    lateinit var bancoCentralCliente: BancoCentralCliente

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup(){
        repository.deleteAll()
        repository.save(chave(tipo = TipoDeChave.EMAIL, chave = "her.al@zup.com.br", clienteId = CLIENTE_ID))
        repository.save(chave(tipo = TipoDeChave.CPF, chave = "12345678900", clienteId = UUID.randomUUID()))
        repository.save(chave(tipo = TipoDeChave.ALEATORIA, chave = "randomkey-3", clienteId = CLIENTE_ID))
        repository.save(chave(tipo = TipoDeChave.CELULAR, chave = "+551155554321", clienteId = CLIENTE_ID))
    }


    @Test
    fun `deve listar dados da chave por cliente id e pixId`(){
        //cenario
        val chave = repository.findByChave("her.al@zup.com.br").get()

        //acao
        val response = grpcClient.carrega(CarregaChavePixRequest.newBuilder()
            .setPixId(CarregaChavePixRequest.FiltroPorPixId.newBuilder()
                .setPixId(chave.id.toString())
                .setClienteId(chave.clienteId.toString()))
            .build()
        )

        //validacao
        with(response) {
            assertEquals(chave.id.toString(), this.pixId)
            assertEquals(chave.clienteId.toString(), this.clienteId)
            assertEquals(chave.tipoChave.name, this.chavePix.tipo.name)
            assertEquals(chave.chave, this.chavePix.chave)
        }
    }

    @Test
    fun `deve listar dados da chave por chave`(){
        //cenario
        val bcbresponse = pixKeyDetailsResponse()
        `when`(bancoCentralCliente.findByKey("exist@gmail.com"))
            .thenReturn(HttpResponse.ok(pixKeyDetailsResponse()))


        //acao
        val response = grpcClient.carrega(CarregaChavePixRequest.newBuilder()
            .setChave("exist@gmail.com")
            .build())

        //validacao
        with(response) {
            assertEquals("", this.pixId)
            assertEquals("", this.clienteId)
            assertEquals(bcbresponse.keyType.name, this.chavePix.tipo.name)
            assertEquals(bcbresponse.key, this.chavePix.chave)

        }
    }

    @Test
    fun `nao deve carregar chave por pixId e clienteId quando dados nao pertencer ao usuario`() {

        //cenario
        val chave = repository.findByChave("her.al@zup.com.br").get()

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.carrega(CarregaChavePixRequest.newBuilder()
                .setPixId(
                    CarregaChavePixRequest.FiltroPorPixId.newBuilder()
                        .setPixId(chave.id.toString())
                        .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                        .build()
                ).build())
        }


        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }
    }

    @Test
    fun `nao deve carregar chave por pixId e clienteId quando dados invalido`() {
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.carrega(CarregaChavePixRequest.newBuilder()
                .setPixId(
                    CarregaChavePixRequest.FiltroPorPixId.newBuilder()
                        .setPixId("")
                        .setClienteId("")
                        .build()
                ).build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertThat(violations(), containsInAnyOrder(
                Pair("pixId", "não deve estar em branco"),
                Pair("clienteId", "não deve estar em branco"),
                Pair("pixId", "não é um formato válido de UUID"),
                Pair("clienteId", "não é um formato válido de UUID"),
            ))
        }
    }

    @Test
    fun `nao deve carregar chave por valor da chave quando nao existir no banco no local nem no BACEN`() {
        //cenario
        `when`(bancoCentralCliente.findByKey(key = "not.existing@gmail.com.br"))
            .thenReturn(HttpResponse.notFound())

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.carrega(CarregaChavePixRequest.newBuilder()
                .setChave("not.existing@gmail.com.br")
                .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix nao encontrada", status.description)
        }
    }

    @Test
    fun `nao deve carregar chave por valor da chave quando sistema BACEN estiver off`() {
        //cenario
        `when`(bancoCentralCliente.findByKey(key = "not.existing@gmail.com.br"))
            .thenReturn(null)

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.carrega(CarregaChavePixRequest.newBuilder()
                .setChave("not.existing@gmail.com.br")
                .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.UNAVAILABLE.code, status.code)
            assertEquals("Falha ao acessar o sistema do BACEN", status.description)
        }
    }

    @Test
    fun `nao deve carregar chave quando filtro invalido`() {

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.carrega(CarregaChavePixRequest.newBuilder().build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Chave Pix inválida ou não informada", status.description)
        }
    }

    @Test
    fun `nao deve carregar chave por valor da chave quando filtro invalido`() {
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.carrega(CarregaChavePixRequest.newBuilder().setChave("").build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertThat(violations(), containsInAnyOrder(
                Pair("chave", "não deve estar em branco"),
            ))
        }
    }
    @MockBean(BancoCentralCliente::class)
    fun bcbClientMock(): BancoCentralCliente? {
        return Mockito.mock(BancoCentralCliente::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): CarregaPixKeyServiceGrpc.CarregaPixKeyServiceBlockingStub {
            return CarregaPixKeyServiceGrpc.newBlockingStub((channel))

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

    private fun pixKeyDetailsResponse(): BancoCentralCliente.PixKeyDetailsResponse {
        return BancoCentralCliente.PixKeyDetailsResponse(
            keyType = PixKeyTipo.EMAIL,
            key = "user.from.another.bank@santander.com.br",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = "90400888",
            branch = "9871",
            accountNumber = "987654",
            accountType = BankAccount.AccountType.SVGS
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = Owner.OwnerType.NATURAL_PERSON,
            name = "Another User",
            taxIdNumber = "12345678901"
        )
    }
}