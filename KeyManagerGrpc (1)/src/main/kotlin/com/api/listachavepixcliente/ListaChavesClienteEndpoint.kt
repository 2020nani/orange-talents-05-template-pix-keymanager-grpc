package com.api.listachavepixcliente

import com.api.*
import com.api.cadastrachavepix.ChavePixRepository
import com.api.compartilhado.exception.ErrorHandler
import com.api.removechavepix.RemovePixKeyEndpoint
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import com.google.protobuf.Timestamp
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListaChavesClienteEndpoint(
    @Inject val chavePixRepository: ChavePixRepository
) : ListaPixKeyServiceGrpc.ListaPixKeyServiceImplBase() {
    val logger = LoggerFactory.getLogger(RemovePixKeyEndpoint::class.java)

    override fun listar(
        request: ListaChavesPixRequest?,
        responseObserver: StreamObserver<ListaChavesPixResponse>?
    ) {
        logger.info("Iniciando requisicao")

        if (request?.clienteId.isNullOrBlank()) throw IllegalArgumentException("Cliente ID não pode ser nulo ou vazio")

        val chaves = chavePixRepository.findAllByClienteId(UUID.fromString(request?.clienteId))
            .map {
                ListaChavesPixResponse.ChavePix.newBuilder()
                    .setPixId(it.id.toString())
                    .setTipoChave(TipoChave.valueOf(it.tipoChave.name))
                    .setChave(it.chave)
                    .setTipoConta(TipoConta.valueOf(it.tipoConta.name))
                    .setCriadaEm(it.criadaEm.let {
                        val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                        Timestamp.newBuilder()
                            .setSeconds(createdAt.epochSecond)
                            .setNanos(createdAt.nano)
                            .build()
                    }).build()
            }
        logger.info("lista gerada com sucesso, tamanho da lista: ${chaves.size}")

        responseObserver?.onNext(
            ListaChavesPixResponse.newBuilder()
                .setClienteId(request?.clienteId.toString())
                .addAllChaves(chaves)
                .build()
        )
        responseObserver?.onCompleted()
    }
}