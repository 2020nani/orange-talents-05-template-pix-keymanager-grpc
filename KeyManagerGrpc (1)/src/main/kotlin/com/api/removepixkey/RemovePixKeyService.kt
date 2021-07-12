package com.api.removepixkey

import com.api.cadastrapixkey.ChavePixRepository
import com.api.cadastrapixkey.registrochavebacen.BancoCentralCliente
import com.api.cadastrapixkey.registrochavebacen.DeletaPixKeyRequest
import com.api.compartilhado.ChavePixNaoExistenteException
import com.api.compartilhado.ValidUUID
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class RemovePixKeyService(
    @Inject val repository: ChavePixRepository,
    @Inject val bancoCentralCliente: BancoCentralCliente
) {
    val logger = LoggerFactory.getLogger(RemovePixKeyService::class.java)

    @Transactional
    fun removeChavePix(
        @NotBlank @ValidUUID pixId: String,
        @NotBlank @ValidUUID clienteId: String
    ) {
        //verifica se chave existe no sistema
        val chave = repository.findByIdAndClienteId(UUID.fromString(pixId), UUID.fromString(clienteId))
            .orElseThrow { ChavePixNaoExistenteException("Nao existe uma chave-pix cadastrada ou a chave nao pertence ao cliente ") }

        val request = DeletaPixKeyRequest(chave.chave)
        println(request)
        val bcbResponse = bancoCentralCliente.deletaChaveBACEN(key = chave.chave,request = request)
        if (bcbResponse.status != HttpStatus.OK) // 1
        throw IllegalStateException("Erro ao deletar chave Pix no Banco Central do Brasil (BCB)")
        logger.info("Chave (BCB) deletada com sucesso\n," +
                "key: ${bcbResponse.body().key.substring(0,4)}-*****-*****-${bcbResponse.body().key.substring(bcbResponse.body().key.length-4)}\n," +
                "participant: ****${bcbResponse.body().participant.substring(bcbResponse.body().participant.length-4)}\n," +
                "horario: ${bcbResponse.body().deletedAt} ")
        repository.deleteById(UUID.fromString(pixId))
        logger.info("Chave deletada com sucesso do banco de dados")
    }

}
