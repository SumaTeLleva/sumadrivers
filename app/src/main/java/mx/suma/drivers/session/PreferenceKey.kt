package mx.suma.drivers.session

enum class PreferenceKey {
    /**
     * Enum representing your setting names or key for your setting.
     *
     * Recommended naming convention:
     * ints, floats, doubles, longs:
     * SAMPLE_NUM or SAMPLE_COUNT or SAMPLE_INT, SAMPLE_LONG etc.
     *
     * boolean: IS_SAMPLE, HAS_SAMPLE, CONTAINS_SAMPLE
     *
     * String: SAMPLE_KEY, SAMPLE_STR or just SAMPLE
     */

    IS_LOGGED_IN,
    FIREBASE_TOKEN_STR,
    HAS_FIREBASE_TOKEN,
    HAS_CACHE_DIRECTORIO,
    HAS_CACHE_UNIDADES,
    HAS_CACHE_GASOLINERAS,
    ULTIMO_KILOMETRAJE_LONG,
    ULTIMO_KILOMETRAJE_INICIAL_LONG,
    ULTIMO_FOLIO_BITACORA_LONG,
    DISPOSITIVO_ID_LONG,
    IS_DISPOSITIVO_SUSCRIBED,
    LATITUD_DBL,
    LONGITUD_DBL,
    LOCATION_TIME_DBL,
    ULTIMA_ENCUESTA_STR,
    IS_PRODUCCTION,
    DATE_START_SESSION,

    LASTVEHICLE,
    LASTDATE,

    GET_BITACORA,
    PATH_SERVER,
}