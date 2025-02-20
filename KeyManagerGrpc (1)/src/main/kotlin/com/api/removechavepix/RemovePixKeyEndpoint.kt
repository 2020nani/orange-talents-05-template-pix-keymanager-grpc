package com.api.removechavepix

import com.api.ExcluiPixKeyResponse
import com.api.PixKeyChaveRequest
import com.api.RemovePixKeyServiceGrpc
import com.api.compartilhado.exception.ErrorHandler
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RemovePixKeyEndpoint(
    @Inject val removePixKeyService: RemovePixKeyService,
) : RemovePixKeyServiceGrpc.RemovePixKeyServiceImplBase() {

    val logger = LoggerFactory.getLogger(RemovePixKeyEndpoint::class.java)

    override fun excluiPixKey(request: PixKeyChaveRequest?, responseObserver: StreamObserver<ExcluiPixKeyResponse>?) {
        logger.info("Iniciando requisicao")

        removePixKeyService.removeChavePix(request!!.pixId, request!!.idCliente)

        responseObserver?.onNext(
            ExcluiPixKeyResponse.newBuilder()
                .setMessage("Chave Pix Deletada com sucesso")
                .build()
        )

        responseObserver?.onCompleted()

    }
}