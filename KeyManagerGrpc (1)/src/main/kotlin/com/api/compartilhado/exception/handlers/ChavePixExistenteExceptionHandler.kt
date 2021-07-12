package com.api.compartilhado.exception.handlers

import com.api.compartilhado.exceptionscustomizadas.ChavePixExistenteException
import com.api.compartilhado.exception.ExceptionHandler
import com.api.compartilhado.exception.ExceptionHandler.StatusWithDetails
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixExistenteExceptionHandler : ExceptionHandler<ChavePixExistenteException> {

    override fun handle(e: ChavePixExistenteException): StatusWithDetails {
        return StatusWithDetails(Status.ALREADY_EXISTS
            .withDescription(e.message)
            .withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is ChavePixExistenteException
    }
}