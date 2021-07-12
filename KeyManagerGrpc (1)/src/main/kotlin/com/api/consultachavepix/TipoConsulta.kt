package com.api.consultachavepix

import com.api.cadastrachavepix.ChavePixRepository
import com.api.cadastrachavepix.NovaPixKeyEndpoint
import com.api.compartilhado.exceptionscustomizadas.ChavePixNaoEncontradaException
import com.api.servicosexternos.operacoesBACEN.BancoCentralCliente
import com.api.compartilhado.exceptionscustomizadas.ChavePixNaoExistenteException
import com.api.compartilhado.exceptionscustomizadas.ServicoExternoException
import com.api.compartilhado.validationscustomizadas.ValidUUID
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class TipoConsulta {

    val logger = LoggerFactory.getLogger(NovaPixKeyEndpoint::class.java)

    /**
     * Deve retornar chave encontrada ou lançar um exceção de erro de chave não encontrada
     */
    abstract fun filtra(repository: ChavePixRepository, bancoCentralCliente: BancoCentralCliente): ChavePixInfo // 3

    @Introspected
    data class PorPixId(
        @field:NotBlank @field:ValidUUID val clienteId: String, // 1
        @field:NotBlank @field:ValidUUID val pixId: String,
    ) : TipoConsulta() { // 1
        override fun filtra(repository: ChavePixRepository, bancoCentralCliente: BancoCentralCliente): ChavePixInfo {
            return repository.findByIdAndClienteId(UUID.fromString(pixId), UUID.fromString(clienteId))
                .filter { it.pertenceAoCliente(UUID.fromString(clienteId)) }
                .map { ChavePixInfo.converte(it) }
                .orElseThrow { ChavePixNaoEncontradaException("Chave Pix não encontrada") }

        }

    }

    @Introspected
    data class PorChave(
        @field:NotBlank @Size(max = 77) val chave: String
    ) : TipoConsulta() { // 1
        override fun filtra(repository: ChavePixRepository, bancoCentralCliente: BancoCentralCliente): ChavePixInfo {
            return repository.findByChave(chave)
                .map { ChavePixInfo.converte(it) }
                .orElseGet {
                    logger.info("Buscando dados no BACEN para a chave ${chave.substring(0, 4)}*****")
                    val response = bancoCentralCliente.findByKey(chave)
                        ?: throw ServicoExternoException("Falha ao acessar o sistema do BACEN")

                    when (response?.status) { // 1
                        HttpStatus.OK -> response?.body()?.converte()
                        else -> throw ChavePixNaoEncontradaException("Chave pix nao encontrada")
                    }
                }
        }

    }

    @Introspected
    class Invalido() : TipoConsulta() {
        override fun filtra(repository: ChavePixRepository, bancoCentralCliente: BancoCentralCliente): ChavePixInfo {
            throw IllegalArgumentException("Chave Pix inválida ou não informada")

        }

    }

}
