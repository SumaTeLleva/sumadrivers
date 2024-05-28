package mx.suma.drivers.models.api.utils

import mx.suma.drivers.models.api.*
import mx.suma.drivers.models.api.encuestas.Encuesta
import mx.suma.drivers.models.api.encuestas.Pregunta
import mx.suma.drivers.models.api.usuarios.Usuario
import mx.suma.drivers.models.db.*
import mx.suma.drivers.utils.md5
import mx.suma.drivers.utils.parseDateTimeString

fun Usuario.asDbModel(): UsuarioModel {
    return UsuarioModel(
        id = this.data.id,
        nombre = this.attr.nombre,
        email = this.attr.email,
        acceso = this.attr.acceso,
        activo = this.attr.activo,
        supervisor = this.attr.supervisor,
        idOperadorSuma = this.attr.idOperadorSuma,
        idEmpresa = this.attr.idEmpresa,
        idProveedor = this.attr.idProveedor,
        numeroTelefono = this.attr.numeroTelefono,
        fotografia = this.rel.operadorSuma.attr.fotografia,
        idUnidad = this.rel.operadorSuma.attr.idUnidad
    )
}

fun BitacoraDate.asDbModel(): BitacoraDate {
    return this
}

fun Bitacora.asDbModel(): BitacoraModel {
    return BitacoraModel(
        id = data.id,
        modalidad = attr.modalidad,
        terminado = attr.terminado,
        confirmado = attr.confirmado,
        motivoTransferencia = attr.motivoTransferencia,
        transferido = attr.transferido,
        transferidoAt = attr.transferidoAt,
        tiempoInicial = attr.tiempoInicial,
        tiempoFinal = attr.tiempoFinal,
        fecha = attr.fecha,
        alarmaNotificacion = attr.alarmaNotificacion,
        alarmaInicioRuta = attr.alarmaInicioRuta,
        comentarios = attr.comentarios,
        estatus = attr.estatus,
        folioBitacora = attr.folioBitacora,
        kilometrajeInicial = attr.kilometrajeInicial,
        kilometrajeFinal = attr.kilometrajeFinal,
        numeroPersonas = attr.numeroPersonas,
        tiempoCantidad = attr.tiempoCantidad,
        dia = attr.dia,
        semana = attr.semana,
        idProveedor = attr.idProveedor,
        idOperador = attr.idOperador,
        idUnidad = attr.idUnidad,
        idRuta = attr.idRuta,
        idEstructura = attr.idEstructura,
        idServicioEspecial = attr.idServicioEspecial,
        idMapa = rel.ruta.attr.idMapa,
        horaConfirmacion = attr.horaConfirmacion,
        horaBanderazo = attr.horaBanderazo,
        horaInicioRuta = attr.horaInicioRuta,
        horaFinalRuta = attr.horaFinalRuta,
        horaCierreRuta = attr.horaCierreRuta,
        tipo = attr.tipo,
        verificado = attr.verificado,
        pagarServicio = attr.pagarServicio,
        cancelado = attr.cancelado,
        canceladoAt = attr.canceladoAt,
        excepcion = attr.excepcion,
        excepcionAt = attr.excepcionAt,
        nombreRuta =  rel.ruta.attr.nombre,
        turno = rel.estructura.attr.turno,
        idCliente = rel.ruta.attr.idCliente,
        capturarAforo = rel.ruta.attr.clienteRequiereAforo && rel.unidad.attr.lectorHuella,
        nombreOperador = rel.operador.attr.nombre,
        letreroEspecial = attr.letreroEspecial,
    )
}

fun List<Bitacora>.asBitacoraDbModel(): List<BitacoraModel> {
    return map {
        it.asDbModel()
    }
}

fun Ticket.asDbModel(): TicketModel {
    return TicketModel(
        id = data.id,
        fecha = attr.fecha,
        tipoCombustible = attr.tipoCombustible,
        folio = attr.folio,
        monto = attr.monto,
        litros = attr.litros,
        precioCombustible = attr.precioCombustible,
        kilometraje = attr.kilometraje,
        viaCaptura = attr.viaCaptura,
        idUnidad = attr.idUnidad,
        idOperador = attr.idOperador,
        idGasolinera = attr.idGasolinera,
        nombreGasolinera = rel.gasolinera.attr.nombre
    )
}

fun List<Ticket>.asTicketDbModel(): List<TicketModel> {
    return map {
        it.asDbModel()
    }
}

fun Contacto.asDbModel(): ContactoModel {
    return ContactoModel(nombre.md5(), nombre, email, telefono, tipo)
}

fun List<Contacto>.asContactoDbModel(): List<ContactoModel> {
    return map {
        it.asDbModel()
    }
}

fun Mantenimiento.asDbModel(): MantenimientoModel {
    return MantenimientoModel(
        data.id, attr.fecha, attr.idUsuario, attr.idUnidad, attr.titulo,
        attr.notas, attr.solucion, attr.concluido
    )
}

fun List<Mantenimiento>.asMantenimientoDbModel(): List<MantenimientoModel> {
    return map {
        it.asDbModel()
    }
}

fun Unidad.asDbModel(): UnidadModel {
    return UnidadModel(
        id = data.id,
        descripcion = attr.descripcion,
        pasajeros = attr.pasajeros,
        numeroEconomico = attr.numeroEconomico,
        tipo = attr.tipo,
        fotografia = attr.fotografia,
        gps = attr.gps,
        modelo = attr.modelo,
        isActiva = attr.isActiva,
        locatorWialon = attr.locatorWialon,
        itemIdWialon = attr.itemIdWialon,
        iconoUnidad = attr.iconoUnidad,
        placasFederales = attr.placasFederales
    )
}

fun List<Unidad>.asUnidadDbModel(): List<UnidadModel> {
    return map {
        it.asDbModel()
    }
}

fun List<Unidad>.filterUnidadLibre(): List<Unidad> {
    return filter {
        it.rel.operador == null
    }
}

fun Proveedor.asDbModel(): ProveedorModel {
    return ProveedorModel(
        id = data.id,
        nombre = attr.nombre,
        email = attr.email,
        numeroTelefono = attr.numeroTelefono,
        tipoContacto = attr.tipoContacto,
        idCategoria = attr.idCategoria,
        isActivo = attr.isActivo
    )
}

fun List<Proveedor>.asProveedorDbModel(): List<ProveedorModel> {
    return map {
        it.asDbModel()
    }
}

fun Sanitizacion.asDbModel(): SanitizacionModel {
    return SanitizacionModel(
        id = data.id,
        fecha = attr.fecha,
        tiempoInicial = attr.tiempoInicial,
        tiempoFinal = attr.tiempoFinal,
        coordenadas = attr.coordenadas,
        clienteId = attr.clienteId,
        operadorId = attr.operadorId,
        unidadId = attr.unidadId,
        nombreCliente = rel.cliente.attr.nombreEmpresa
    )
}

fun List<Sanitizacion>.asSanitizacionModel(): List<SanitizacionModel> {
    return map {
        it.asDbModel()
    }
}

fun Cliente.asDbModel(): ClienteModel {
    return ClienteModel(
        id = data.id,
        nombreEmpresa = attr.nombreEmpresa,
        logoEmpresa = attr.logoEmpresa,
        activo = attr.activo,
        requiereCentroCostos = attr.requiereCentroCostos,
        politicaTaxis = attr.politicaTaxis,
        politicaServicio = attr.politicaServicio,
        mostrarTelefonoOperador = attr.mostrarTelefonoOperador,
        ubicacionEmpresa = attr.ubicacionEmpresa,
        idCategoria = attr.idCategoria,
        idCoordinadorServicio = attr.idCoordinadorServicio,
        idSupervisorServicio = attr.idSupervisorServicio,
        idJefeOperaciones = attr.idJefeOperaciones,
        iniciales = attr.iniciales,
        nipMapas = attr.nipMapas,
        idUsuarioCoordinador = attr.idUsuarioCoordinador
    )
}

fun List<Cliente>.asClienteModel(): List<ClienteModel> {
    return map {
        it.asDbModel()
    }
}

fun Encuesta.asDbModel(): EncuestaModel {
    val fInicial = parseDateTimeString(attr.fechaInicial)
    val fFinal = parseDateTimeString(attr.fechaFinal)

    return EncuestaModel(
        id = data.id,
        nombre = attr.nombre,
        proposito = attr.proposito,
        fechaInicial = fInicial,
        fechaFinal = fFinal,
        frecuencia = attr.frecuencia,
        ponderada = attr.ponderada
    )
}

fun List<Encuesta>.asEncuestaModel(): List<EncuestaModel> {
    return map {
        it.asDbModel()
    }
}

fun Pregunta.asDbModel(): PreguntaModel {
    return PreguntaModel(
        id = data.id,
        pregunta = attr.pregunta,
        puntaje = attr.puntaje,
        tipo = attr.tipo,
        imagenUrl = attr.imagenUrl,
        encuestaId = attr.encuestaId
    )
}

fun List<Pregunta>.asPreguntaModel(): List<PreguntaModel> {
    return map {
        it.asDbModel()
    }
}

fun Unidades.asIds(): List<Int> {
    return data.map {
        it.ID
    }
}