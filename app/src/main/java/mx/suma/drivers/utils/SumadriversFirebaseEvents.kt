package mx.suma.drivers.utils

object SumadriversFirebaseEvents {
    const val LOGIN_START = "login_view"
    const val LOGIN_ATTEMPT = "login_attempt"
    const val LOGIN_SUCCESS = "login_success"
    const val LOGIN_FAILED = "login_failed"

    const val TICKETS_VIEW = "tickets_view"
    const val CAPTURA_TICKET_VIEW = "captura_ticket_view"
    const val BITACORAS_VIEW = "bitacoras_view"
    const val CAPTURA_BITACORA_VIEW = "captura_bitacora_view"
    const val MANTENIMIENTOS_VIEW = "mantenimientos_view"
    const val CAPTURA_MANTENIMIENTO_VIEW = "captura_mantenimiento_view"
    const val MI_UNIDAD_VIEW = "mi_unidad_view"
    const val DIRECTORIO_VIEW = "directorio_view"
    const val MAP_VIEW = "map_view"

    const val KIT_LIMPIEZA_LAUNCH = "kit_limpieza_launch"
    const val KIT_LIMPIEZA_FAILED = "kit_limpieza_failed"

    const val ADBLUE_LAUNCH = "adblue_launch"
    const val ADBLUE_FAILED = "adblue_failed"

    const val REFACCIONES_LAUNCH = "refaccciones_launch"
    const val REFACCIONES_FAILED = "refacciones_failed"

    const val SOLICITUD_ID_LAUNCH = "solicitud_id_launch"
    const val SOLICITUD_ID_FAILED = "solicitud_id_failed"

    const val SOLICITUD_TH_LAUNCH = "solicitud_th_launch"
    const val SOLICITUD_TH_FAILED = "solicitud_th_failed"

    const val SOLICITUD_ADMIN_LAUNCH = "solicitud_admin_launch"
    const val SOLICITUD_ADMIN_FAILED = "solicitud_admin_failed"

    const val SOLICITUD_MANTE_LAUNCH = "solicitud_mante_launch"
    const val SOLICITUD_MANTE_FAILED = "solicitud_mante_failed"

    const val SOLICITUD_STORE_LAUNCH = "solicitud_almacen_launch"
    const val SOLICITUD_STORE_FAILED = "solicitud_almacen_failed"

    const val EXIT_APP = "salir_app"
    const val EXIT_APP_EXPIRE = "sesion_expirado_app"
    const val UPDATE_APP = "actualizar_app"
}