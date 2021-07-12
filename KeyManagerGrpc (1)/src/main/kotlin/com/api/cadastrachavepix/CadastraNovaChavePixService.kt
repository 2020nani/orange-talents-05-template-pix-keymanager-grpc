package com.api.cadastrachavepix

import com.api.servicosexternos.operacoesItau.ItauClient
import com.api.servicosexternos.operacoesBACEN.BancoCentralCliente
import com.api.servicosexternos.operacoesBACEN.GeraPixKeyRequest
import com.api.compartilhado.exceptionscustomizadas.ChavePixExistenteException
import com.api.compartilhado.exceptionscustomizadas.ServicoExternoException
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
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val itauClient: ItauClient,
    @Inject val bcbClient: BancoCentralCliente
) {
    val logger = LoggerFactory.getLogger(CadastraNovaChavePixService::class.java)

    @Transactional
    fun registraChave(@Valid chavePix: NovaChavePix): ChavePix {
        //verifica se chave existe no sistema
        if (chavePixRepository.existsByChave(chavePix.chave)) {
            logger.info("Ja existe uma chave-pix cadastrada para a chave passada na requisicao")
            throw ChavePixExistenteException("Ja existe uma chave-pix cadastrada para a chave ${chavePix.chave} ")

        }
        //busca dados do cliente no ERP do ITAU

        val dadosCliente = itauClient.buscaDadosCliente(chavePix.clienteId!!, chavePix.tipoConta!!.name)
            ?: throw ServicoExternoException(" Falha ao acessar o ERP do ITAU ")

        val conta =
            dadosCliente.body()?.converte() ?: throw IllegalStateException("Dados da conta nao foram encontrado")
        logger.info("Busca dos dados ERP ITAU concluida com sucesso")

        //cadastra ChavePix
        val chave = chavePix.converte(conta)
        logger.info("Chave cadastrada com sucesso no banco de dados")
        chavePixRepository.save(chave)

        val bcbRequest = GeraPixKeyRequest.converte(chave).also { // 1
            logger.info("Registrando chave Pix no Banco Central do Brasil (BCB): $it")
        }

        val bcbResponse = bcbClient.registraChaveBACEN(bcbRequest)
            ?: throw ServicoExternoException(" Falha ao acessar o sistema do BACEN ")

        if (bcbResponse?.status != HttpStatus.CREATED) // 1
            throw IllegalStateException("Erro ao registrar chave Pix no Banco Central do Brasil (BCB)")

        chave.atualizaChave(bcbResponse.body().key)
        logger.info("Chave cadastrada com sucesso no banco de dados")
        return chave
    }

}
