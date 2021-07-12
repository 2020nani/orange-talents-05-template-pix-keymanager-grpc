package com.api.consultachavepix

import com.api.CarregaChavePixResponse
import com.api.TipoChave
import com.api.TipoConta
import com.google.protobuf.Timestamp
import java.time.ZoneId

class CarregaChavePixConverter {

    fun convert(chaveInfo: ChavePixInfo): CarregaChavePixResponse {
        return CarregaChavePixResponse.newBuilder()
            .setClienteId(chaveInfo.clienteId?.toString() ?: "") // Protobuf usa "" como default value para String
            .setPixId(chaveInfo.pixId?.toString() ?: "") // Protobuf usa "" como default value para String
            .setChavePix(
                CarregaChavePixResponse.ChavePix
                    .newBuilder()
                    .setTipo(TipoChave.valueOf(chaveInfo.tipoChave.name))
                    .setChave(chaveInfo.valorDaChave)
                    .setConta(
                        CarregaChavePixResponse.ChavePix.ContaInfo.newBuilder()
                            .setTipo(TipoConta.valueOf(chaveInfo.tipoConta.name))
                            .setInstituicao(chaveInfo.conta.instituicao)
                            .setNomeDoTitular(chaveInfo.conta.nomeDoTitular)
                            .setCpfDoTitular(chaveInfo.conta.cpfDoTitular)
                            .setAgencia(chaveInfo.conta.agencia)
                            .setNumeroDaConta(chaveInfo.conta.numeroDaConta)
                            .build()
                    )
                    .setCriadaEm(chaveInfo.criadoEm.let {
                        val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                        Timestamp.newBuilder()
                            .setSeconds(createdAt.epochSecond)
                            .setNanos(createdAt.nano)
                            .build()
                    })
            )
            .build()
    }
}