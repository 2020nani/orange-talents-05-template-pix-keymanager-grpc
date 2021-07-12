package com.api.compartilhado.exception.handlers

import com.api.compartilhado.exception.ExceptionHandler
import com.api.compartilhado.exceptionscustomizadas.ServicoExternoException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ServicoExternoExceptionHandler : ExceptionHandler<ServicoExternoException> {

    override fun handle(e: ServicoExternoException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.UNAVAILABLE
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is ServicoExternoException
    }
}