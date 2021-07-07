package com.api.compartilhado.exception.handlers

import com.api.compartilhado.ChavePixNaoExistenteException
import com.api.compartilhado.exception.ExceptionHandler
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixNaoExistenteExceptionHandler: ExceptionHandler<ChavePixNaoExistenteException> {

    override fun handle(e: ChavePixNaoExistenteException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.INVALID_ARGUMENT
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is ChavePixNaoExistenteException
    }
}