package mx.suma.drivers.models.api.usuarios.validators

import mx.suma.drivers.models.api.usuarios.Usuario

class UsuarioValidator(val usuarioApi: Usuario) {

    val isValid: Boolean
    val attributes: Usuario.Data.Attributes = usuarioApi.data.attributes

    init {
        val acceso = attributes.acceso
        val activo = attributes.activo
        val empresa = attributes.idEmpresa
        val proveedor = attributes.idProveedor
        val operador = attributes.idOperadorSuma

        isValid = (acceso && activo && empresa > 0 && proveedor > 0 && operador > 0)
    }

    fun reason(): String {
        return if(!attributes.activo) {
            "El usuario no está activo"
        } else if (!attributes.acceso) {
            "El usuario no tiene acceso al sistema"
        } else if (attributes.idEmpresa <= 0) {
            "El usuario no está ligado a ninguna empresa"
        } else if (attributes.idProveedor <= 0) {
            "El usuario no está ligado a algún proveedor"
        } else {
            "El usuario no tiene un perfil de operador asignado"
        }
    }
}