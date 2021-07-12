package com.api.consultachavepix

import com.api.CarregaChavePixRequest
import com.api.CarregaChavePixResponse
import com.api.KeymanagerCarregaGrpcServiceGrpc
import com.api.cadastrachavepix.ChavePixRepository
import com.api.cadastrachavepix.NovaPixKeyEndpoint
import com.api.servicosexternos.operacoesBACEN.BancoCentralCliente
import com.api.compartilhado.exception.ErrorHandler
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@ErrorHandler
@Singleton
class NovaConsultaChavePixEndpoint(
    @Inject private val repository: ChavePixRepository,
    @Inject private val bancoCentralCliente: BancoCentralCliente,
    @Inject private val validator: Validator
) : KeymanagerCarregaGrpcServiceGrpc.KeymanagerCarregaGrpcServiceImplBase() {

    val logger = LoggerFactory.getLogger(NovaPixKeyEndpoint::class.java)

    override fun carrega(request: CarregaChavePixRequest?, responseObserver: StreamObserver<CarregaChavePixResponse>?) {
        logger.info("Inicio requisicao")
        val tipoConsulta = request.filtraPorTipoRequisicao(validator = validator)
        val dadosChave = tipoConsulta!!.filtra(repository, bancoCentralCliente)
        logger.info("dados da chave pix recebido")

        responseObserver?.onNext(CarregaChavePixConverter().convert(dadosChave))
        responseObserver?.onCompleted()
    }
}


