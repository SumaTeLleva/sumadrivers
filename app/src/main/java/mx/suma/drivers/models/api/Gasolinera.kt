package mx.suma.drivers.models.api

class Gasolinera {
    var nombre: String = ""
    var direccion: String = ""
    var habilitado: Boolean = true

    constructor(
        nombre: String,
        direccion: String,
        isHabilitado: Boolean = true
    ) {
        this.nombre = nombre
        this.direccion = direccion
        this.habilitado = isHabilitado
    }
}