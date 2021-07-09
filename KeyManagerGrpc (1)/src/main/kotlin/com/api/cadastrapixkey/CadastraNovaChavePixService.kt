package com.api.cadastrapixkey

import com.api.cadastrapixkey.contaassociada.ItauClient
import com.api.cadastrapixkey.registrochavebacen.BancoCentralCliente
import com.api.cadastrapixkey.registrochavebacen.GeraPixKeyRequest
import com.api.compartilhado.ChavePixExistenteException
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class CadastraNovaChavePixService(
    @Inject val novaChavePixRepository: NovaChavePixRepository,
    @Inject val itauClient: ItauClient,
    @Inject val bcbClient: BancoCentralCliente
) {
    val logger = LoggerFactory.getLogger(CadastraNovaChavePixService::class.java)

    @Transactional
    fun registraChave(@Valid chavePix: NovaChavePix): ChavePix {
        //verifica se chave existe no sistema
        if (novaChavePixRepository.existsByChave(chavePix.chave)) {
            logger.info("Ja existe uma chave-pix cadastrada para a chave passada na requisicao")
            throw ChavePixExistenteException("Ja existe uma chave-pix cadastrada para a chave ${chavePix.chave} ")

        }
        //busca dados do cliente no ERP do ITAU

        val dadosCliente = itauClient.buscaDadosCliente(chavePix.clienteId!!, chavePix.tipoConta!!.name)
        val conta =
            dadosCliente.body()?.converte() ?: throw IllegalStateException("Dados da conta nao foram encontrado")
        logger.info("Busca dos dados ERP ITAU concluida com sucesso")

        //cadastra ChavePix
        val chave = chavePix.converte(conta)
        logger.info("Chave cadastrada com sucesso no banco de dados")
        novaChavePixRepository.save(chave)

        val bcbRequest = GeraPixKeyRequest.converte(chave).also { // 1
            logger.info("Registrando chave Pix no Banco Central do Brasil (BCB): $it")
        }

        val bcbResponse = bcbClient.registraChaveBACEN(bcbRequest) // 1

        if (bcbResponse.status != HttpStatus.CREATED) // 1
            throw IllegalStateException("Erro ao registrar chave Pix no Banco Central do Brasil (BCB)")

        chave.atualizaChave(bcbResponse.body().key)
        logger.info("Chave cadastrada com sucesso no banco de dados")
        return chave
    }

}
